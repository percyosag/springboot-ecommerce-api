package com.percybuilder.ecommerce.controllers;

import com.percybuilder.ecommerce.dtos.CreateOrderRequest;
import com.percybuilder.ecommerce.dtos.OrderResponse;
import com.percybuilder.ecommerce.dtos.OrderStatusUpdateRequest;
import com.percybuilder.ecommerce.dtos.PaymentRequest;
import com.percybuilder.ecommerce.services.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest createOrderRequest
    ) {
        OrderResponse createdOrder = orderService.createOrder(
                authentication.getName(),
                createOrderRequest
        );

        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrders(authentication.getName()));
    }

    @GetMapping("/my/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrderById(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.getMyOrderById(authentication.getName(), orderId)
        );
    }

    @PatchMapping("/my/{orderId}/pay")
    public ResponseEntity<OrderResponse> payMyOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest paymentRequest
    ) {
        return ResponseEntity.ok(
                orderService.payMyOrder(
                        authentication.getName(),
                        orderId,
                        paymentRequest
                )
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/admin/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByIdForAdmin(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderByIdForAdmin(orderId));
    }

    @PatchMapping("/admin/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatusForAdmin(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest orderStatusUpdateRequest
    ) {
        return ResponseEntity.ok(
                orderService.updateOrderStatusForAdmin(orderId, orderStatusUpdateRequest)
        );
    }
}