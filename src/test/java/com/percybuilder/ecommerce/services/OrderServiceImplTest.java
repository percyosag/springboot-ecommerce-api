package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CreateOrderRequest;
import com.percybuilder.ecommerce.dtos.OrderResponse;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.*;
import com.percybuilder.ecommerce.repositories.AddressRepository;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.CartItemRepository;
import com.percybuilder.ecommerce.repositories.OrderRepository;
import com.percybuilder.ecommerce.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.percybuilder.ecommerce.dtos.OrderStatusUpdateRequest;
import com.percybuilder.ecommerce.dtos.PaymentRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldCreateOrderAndClearCart_whenCartAndAddressAreValid() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAddressId(1L);

        AppUser appUser = buildUser();
        Address address = buildAddress(appUser);
        Product product = buildProduct();

        CartItem cartItem = CartItem.builder()
                .id(1L)
                .appUser(appUser)
                .product(product)
                .quantity(2)
                .build();

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(addressRepository.findByAppUserUsernameAndId("percy", 1L))
                .thenReturn(Optional.of(address));
        when(cartItemRepository.findByAppUserUsername("percy"))
                .thenReturn(List.of(cartItem));

        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> {
                    CustomerOrder orderToSave = invocation.getArgument(0);
                    orderToSave.setId(1L);
                    return orderToSave;
                });

        OrderResponse response = orderService.createOrder("percy", request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("percy");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("179.98"));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getShippingAddress().getCity()).isEqualTo("Toronto");

        verify(orderRepository).save(any(CustomerOrder.class));
        verify(cartItemRepository).deleteAll(List.of(cartItem));
    }

    @Test
    void createOrder_shouldThrowBadRequest_whenCartIsEmpty() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAddressId(1L);

        AppUser appUser = buildUser();
        Address address = buildAddress(appUser);

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(addressRepository.findByAppUserUsernameAndId("percy", 1L))
                .thenReturn(Optional.of(address));
        when(cartItemRepository.findByAppUserUsername("percy"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder("percy", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot create order from an empty cart");

        verify(orderRepository, never()).save(any(CustomerOrder.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
    }

    @Test
    void createOrder_shouldThrowNotFound_whenAddressDoesNotExist() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAddressId(999L);

        AppUser appUser = buildUser();

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(addressRepository.findByAppUserUsernameAndId("percy", 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder("percy", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Address not found with id: 999");

        verify(cartItemRepository, never()).findByAppUserUsername(anyString());
        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }
    @Test
    void payMyOrder_shouldMarkOrderAsPaidAndReduceStock_whenOrderIsValid() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod(PaymentMethod.TEST_PAYMENT);
        request.setPaymentTransactionId("TEST-TXN-1001");

        AppUser appUser = buildUser();
        Product product = buildProduct();

        CustomerOrder order = buildPendingOrder(appUser);
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(2)
                .lineTotal(new BigDecimal("179.98"))
                .build();

        order.addOrderItem(orderItem);

        when(orderRepository.findByIdAndAppUserUsername(1L, "percy"))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.payMyOrder("percy", 1L, request);

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.TEST_PAYMENT);
        assertThat(response.getPaymentTransactionId()).isEqualTo("TEST-TXN-1001");
        assertThat(response.getPaidAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING);

        assertThat(product.getStockQuantity()).isEqualTo(18);

        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void payMyOrder_shouldThrowBadRequest_whenOrderIsAlreadyPaid() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod(PaymentMethod.TEST_PAYMENT);
        request.setPaymentTransactionId("TEST-TXN-1002");

        AppUser appUser = buildUser();
        CustomerOrder order = buildPendingOrder(appUser);
        order.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findByIdAndAppUserUsername(1L, "percy"))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payMyOrder("percy", 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Order is already paid");

        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }
    @Test
    void updateOrderStatusForAdmin_shouldUpdateStatus_whenStatusIsValid() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(OrderStatus.SHIPPED);

        AppUser appUser = buildUser();
        CustomerOrder order = buildPendingOrder(appUser);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateOrderStatusForAdmin(1L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    private AppUser buildUser() {
        return AppUser.builder()
                .id(1L)
                .username("percy")
                .email("percy@example.com")
                .build();
    }

    private Address buildAddress(AppUser appUser) {
        return Address.builder()
                .id(1L)
                .appUser(appUser)
                .fullName("Percy Osunde")
                .phoneNumber("4165551234")
                .addressLine1("123 Test Street")
                .addressLine2("Unit 5")
                .city("Toronto")
                .stateOrProvince("Ontario")
                .postalCode("M1A 1A1")
                .country("Canada")
                .defaultAddress(true)
                .build();
    }

    private Product buildProduct() {
        return Product.builder()
                .id(1L)
                .name("Vanilla Aura")
                .description("Warm vanilla fragrance")
                .brand("Apex Fragrance")
                .price(new BigDecimal("89.99"))
                .stockQuantity(20)
                .imageUrl("/images/vanilla-aura.jpg")
                .build();
    }
    private CustomerOrder buildPendingOrder(AppUser appUser) {
        return CustomerOrder.builder()
                .id(1L)
                .appUser(appUser)
                .totalAmount(new BigDecimal("179.98"))
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingFullName("Percy Osunde")
                .shippingPhoneNumber("4165551234")
                .shippingAddressLine1("123 Test Street")
                .shippingAddressLine2("Unit 5")
                .shippingCity("Toronto")
                .shippingStateOrProvince("Ontario")
                .shippingPostalCode("M1A 1A1")
                .shippingCountry("Canada")
                .build();
    }

}
