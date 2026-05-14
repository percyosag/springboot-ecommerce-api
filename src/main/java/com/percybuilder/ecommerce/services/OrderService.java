package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CreateOrderRequest;
import com.percybuilder.ecommerce.dtos.OrderResponse;
import com.percybuilder.ecommerce.dtos.OrderStatusUpdateRequest;
import com.percybuilder.ecommerce.dtos.PaymentRequest;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String username, CreateOrderRequest createOrderRequest);

    List<OrderResponse> getMyOrders(String username);

    OrderResponse getMyOrderById(String username, Long orderId);

    OrderResponse payMyOrder(String username, Long orderId, PaymentRequest paymentRequest);

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderByIdForAdmin(Long orderId);

    OrderResponse updateOrderStatusForAdmin(
            Long orderId,
            OrderStatusUpdateRequest orderStatusUpdateRequest
    );
}