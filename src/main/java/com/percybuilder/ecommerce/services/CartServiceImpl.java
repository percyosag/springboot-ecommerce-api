package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CartItemQuantityRequest;
import com.percybuilder.ecommerce.dtos.CartItemRequest;
import com.percybuilder.ecommerce.dtos.CartItemResponse;
import com.percybuilder.ecommerce.dtos.CartResponse;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.AppUser;
import com.percybuilder.ecommerce.models.CartItem;
import com.percybuilder.ecommerce.models.Product;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.CartItemRepository;
import com.percybuilder.ecommerce.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final AppUserRepository appUserRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(
            CartItemRepository cartItemRepository,
            AppUserRepository appUserRepository,
            ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.appUserRepository = appUserRepository;
        this.productRepository = productRepository;
    }

    @Override
    public CartResponse getCart(String username) {
        List<CartItem> cartItems = cartItemRepository.findByAppUserUsername(username);
        return toCartResponse(cartItems);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String username, CartItemRequest cartItemRequest) {
        AppUser appUser = findUserByUsername(username);
        Product product = findProductById(cartItemRequest.getProductId());

        CartItem cartItem = cartItemRepository
                .findByAppUserUsernameAndProductId(username, cartItemRequest.getProductId())
                .orElse(null);

        if (cartItem == null) {
            validateStock(product, cartItemRequest.getQuantity());

            cartItem = CartItem.builder()
                    .appUser(appUser)
                    .product(product)
                    .quantity(cartItemRequest.getQuantity())
                    .build();
        } else {
            int newQuantity = cartItem.getQuantity() + cartItemRequest.getQuantity();
            validateStock(product, newQuantity);
            cartItem.setQuantity(newQuantity);
        }

        cartItemRepository.save(cartItem);

        return getCart(username);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(
            String username,
            Long productId,
            CartItemQuantityRequest cartItemQuantityRequest
    ) {
        CartItem cartItem = findCartItem(username, productId);
        Product product = cartItem.getProduct();

        validateStock(product, cartItemQuantityRequest.getQuantity());

        cartItem.setQuantity(cartItemQuantityRequest.getQuantity());
        cartItemRepository.save(cartItem);

        return getCart(username);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(String username, Long productId) {
        CartItem cartItem = findCartItem(username, productId);
        cartItemRepository.delete(cartItem);

        return getCart(username);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        List<CartItem> cartItems = cartItemRepository.findByAppUserUsername(username);
        cartItemRepository.deleteAll(cartItems);
    }

    private AppUser findUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username
                ));
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId
                ));
    }

    private CartItem findCartItem(String username, Long productId) {
        return cartItemRepository.findByAppUserUsernameAndProductId(username, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found for product id: " + productId
                ));
    }

    private void validateStock(Product product, Integer requestedQuantity) {
        if (requestedQuantity > product.getStockQuantity()) {
            throw new BadRequestException(
                    "Requested quantity exceeds available stock for product: " + product.getName()
            );
        }
    }

    private CartResponse toCartResponse(List<CartItem> cartItems) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::toCartItemResponse)
                .toList();

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .productImageUrl(product.getImageUrl())
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
}