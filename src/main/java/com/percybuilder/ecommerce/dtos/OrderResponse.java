package com.percybuilder.ecommerce.dtos;

import com.percybuilder.ecommerce.models.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String username;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private OrderShippingAddressResponse shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}