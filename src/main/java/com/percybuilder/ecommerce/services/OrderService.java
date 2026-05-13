package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CreateOrderRequest;
import com.percybuilder.ecommerce.dtos.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String username, CreateOrderRequest createOrderRequest);

    List<OrderResponse> getMyOrders(String username);

    OrderResponse getMyOrderById(String username, Long orderId);

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderByIdForAdmin(Long orderId);
}