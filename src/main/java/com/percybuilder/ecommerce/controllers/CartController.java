package com.percybuilder.ecommerce.controllers;

import com.percybuilder.ecommerce.dtos.CartItemQuantityRequest;
import com.percybuilder.ecommerce.dtos.CartItemRequest;
import com.percybuilder.ecommerce.dtos.CartResponse;
import com.percybuilder.ecommerce.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest cartItemRequest
    ) {
        return ResponseEntity.ok(
                cartService.addItemToCart(authentication.getName(), cartItemRequest)
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @RequestBody CartItemQuantityRequest cartItemQuantityRequest
    ) {
        return ResponseEntity.ok(
                cartService.updateCartItem(
                        authentication.getName(),
                        productId,
                        cartItemQuantityRequest
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeCartItem(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                cartService.removeCartItem(authentication.getName(), productId)
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}