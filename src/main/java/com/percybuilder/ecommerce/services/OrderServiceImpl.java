package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CreateOrderRequest;
import com.percybuilder.ecommerce.dtos.OrderItemResponse;
import com.percybuilder.ecommerce.dtos.OrderResponse;
import com.percybuilder.ecommerce.dtos.OrderShippingAddressResponse;
import com.percybuilder.ecommerce.dtos.OrderStatusUpdateRequest;
import com.percybuilder.ecommerce.dtos.PaymentRequest;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.*;
import com.percybuilder.ecommerce.repositories.AddressRepository;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.CartItemRepository;
import com.percybuilder.ecommerce.repositories.OrderRepository;
import com.percybuilder.ecommerce.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final AppUserRepository appUserRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            AppUserRepository appUserRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.appUserRepository = appUserRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest createOrderRequest) {
        AppUser appUser = findUserByUsername(username);

        Address address = addressRepository
                .findByAppUserUsernameAndId(username, createOrderRequest.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + createOrderRequest.getAddressId()
                ));

        List<CartItem> cartItems = cartItemRepository.findByAppUserUsername(username);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot create order from an empty cart");
        }

        validateCartStock(cartItems);

        BigDecimal totalAmount = calculateTotalAmount(cartItems);

        CustomerOrder customerOrder = CustomerOrder.builder()
                .appUser(appUser)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingFullName(address.getFullName())
                .shippingPhoneNumber(address.getPhoneNumber())
                .shippingAddressLine1(address.getAddressLine1())
                .shippingAddressLine2(address.getAddressLine2())
                .shippingCity(address.getCity())
                .shippingStateOrProvince(address.getStateOrProvince())
                .shippingPostalCode(address.getPostalCode())
                .shippingCountry(address.getCountry())
                .build();

        cartItems.forEach(cartItem -> {
            Product product = cartItem.getProduct();

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build();

            customerOrder.addOrderItem(orderItem);
        });

        CustomerOrder savedOrder = orderRepository.save(customerOrder);

        cartItemRepository.deleteAll(cartItems);

        return toResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getMyOrders(String username) {
        return orderRepository.findByAppUserUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getMyOrderById(String username, Long orderId) {
        CustomerOrder customerOrder = orderRepository.findByIdAndAppUserUsername(orderId, username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        return toResponse(customerOrder);
    }

    @Override
    @Transactional
    public OrderResponse payMyOrder(String username, Long orderId, PaymentRequest paymentRequest) {
        CustomerOrder customerOrder = orderRepository.findByIdAndAppUserUsername(orderId, username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        if (customerOrder.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Order is already paid");
        }

        if (customerOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay for a cancelled order");
        }

        validateOrderStock(customerOrder.getOrderItems());
        reduceProductStock(customerOrder.getOrderItems());

        customerOrder.setPaymentStatus(PaymentStatus.PAID);
        customerOrder.setPaymentMethod(paymentRequest.getPaymentMethod());
        customerOrder.setPaymentTransactionId(paymentRequest.getPaymentTransactionId());
        customerOrder.setPaidAt(LocalDateTime.now());
        customerOrder.setStatus(OrderStatus.PROCESSING);

        CustomerOrder paidOrder = orderRepository.save(customerOrder);

        return toResponse(paidOrder);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        CustomerOrder customerOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        return toResponse(customerOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusForAdmin(
            Long orderId,
            OrderStatusUpdateRequest orderStatusUpdateRequest
    ) {
        CustomerOrder customerOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        if (orderStatusUpdateRequest.getStatus() == OrderStatus.DELIVERED
                && customerOrder.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Cannot mark an unpaid order as delivered");
        }

        customerOrder.setStatus(orderStatusUpdateRequest.getStatus());

        CustomerOrder updatedOrder = orderRepository.save(customerOrder);

        return toResponse(updatedOrder);
    }

    private AppUser findUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username
                ));
    }

    private void validateCartStock(List<CartItem> cartItems) {
        cartItems.forEach(cartItem -> {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException(
                        "Requested quantity exceeds available stock for product: " + product.getName()
                );
            }
        });
    }

    private void validateOrderStock(List<OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            Product product = orderItem.getProduct();

            if (orderItem.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException(
                        "Requested quantity exceeds available stock for product: " + product.getName()
                );
            }
        });
    }

    private void reduceProductStock(List<OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            Product product = orderItem.getProduct();
            int newStockQuantity = product.getStockQuantity() - orderItem.getQuantity();

            product.setStockQuantity(newStockQuantity);
            productRepository.save(product);
        });
    }

    private BigDecimal calculateTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> cartItem.getProduct()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderResponse toResponse(CustomerOrder customerOrder) {
        List<OrderItemResponse> itemResponses = customerOrder.getOrderItems()
                .stream()
                .map(this::toOrderItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(customerOrder.getId())
                .userId(customerOrder.getAppUser().getId())
                .username(customerOrder.getAppUser().getUsername())
                .items(itemResponses)
                .totalAmount(customerOrder.getTotalAmount())
                .status(customerOrder.getStatus())
                .paymentStatus(customerOrder.getPaymentStatus())
                .paymentMethod(customerOrder.getPaymentMethod())
                .paymentTransactionId(customerOrder.getPaymentTransactionId())
                .paidAt(customerOrder.getPaidAt())
                .shippingAddress(toShippingAddressResponse(customerOrder))
                .createdAt(customerOrder.getCreatedAt())
                .updatedAt(customerOrder.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProductName())
                .productPrice(orderItem.getProductPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getLineTotal())
                .build();
    }

    private OrderShippingAddressResponse toShippingAddressResponse(CustomerOrder customerOrder) {
        return OrderShippingAddressResponse.builder()
                .fullName(customerOrder.getShippingFullName())
                .phoneNumber(customerOrder.getShippingPhoneNumber())
                .addressLine1(customerOrder.getShippingAddressLine1())
                .addressLine2(customerOrder.getShippingAddressLine2())
                .city(customerOrder.getShippingCity())
                .stateOrProvince(customerOrder.getShippingStateOrProvince())
                .postalCode(customerOrder.getShippingPostalCode())
                .country(customerOrder.getShippingCountry())
                .build();
    }
}