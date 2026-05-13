package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CartItemQuantityRequest;
import com.percybuilder.ecommerce.dtos.CartItemRequest;
import com.percybuilder.ecommerce.dtos.CartResponse;

public interface CartService {

    CartResponse getCart(String username);

    CartResponse addItemToCart(String username, CartItemRequest cartItemRequest);

    CartResponse updateCartItem(
            String username,
            Long productId,
            CartItemQuantityRequest cartItemQuantityRequest
    );

    CartResponse removeCartItem(String username, Long productId);

    void clearCart(String username);
}