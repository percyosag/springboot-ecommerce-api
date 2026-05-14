package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CartItemRequest;
import com.percybuilder.ecommerce.dtos.CartResponse;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.AppUser;
import com.percybuilder.ecommerce.models.CartItem;
import com.percybuilder.ecommerce.models.Product;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.CartItemRepository;
import com.percybuilder.ecommerce.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void getCart_shouldReturnEmptyCart_whenUserHasNoCartItems() {
        when(cartItemRepository.findByAppUserUsername("percy")).thenReturn(List.of());

        CartResponse response = cartService.getCart("percy");

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalItems()).isZero();
        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartItemRepository).findByAppUserUsername("percy");
    }

    @Test
    void addItemToCart_shouldAddNewItem_whenProductExistsAndStockIsAvailable() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        AppUser appUser = AppUser.builder()
                .id(1L)
                .username("percy")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Vanilla Aura")
                .price(new BigDecimal("89.99"))
                .stockQuantity(20)
                .imageUrl("/images/vanilla-aura.jpg")
                .build();

        CartItem savedCartItem = CartItem.builder()
                .id(1L)
                .appUser(appUser)
                .product(product)
                .quantity(2)
                .build();

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByAppUserUsernameAndProductId("percy", 1L))
                .thenReturn(Optional.empty());
        when(cartItemRepository.findByAppUserUsername("percy"))
                .thenReturn(List.of(savedCartItem));

        CartResponse response = cartService.addItemToCart("percy", request);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(2);
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("179.98"));

        verify(appUserRepository).findByUsername("percy");
        verify(productRepository).findById(1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_shouldIncreaseQuantity_whenProductAlreadyExistsInCart() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        AppUser appUser = AppUser.builder()
                .id(1L)
                .username("percy")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Vanilla Aura")
                .price(new BigDecimal("89.99"))
                .stockQuantity(20)
                .imageUrl("/images/vanilla-aura.jpg")
                .build();

        CartItem existingCartItem = CartItem.builder()
                .id(1L)
                .appUser(appUser)
                .product(product)
                .quantity(2)
                .build();

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByAppUserUsernameAndProductId("percy", 1L))
                .thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.findByAppUserUsername("percy"))
                .thenReturn(List.of(existingCartItem));

        CartResponse response = cartService.addItemToCart("percy", request);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(3);
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("269.97"));

        verify(cartItemRepository).save(existingCartItem);
    }

    @Test
    void addItemToCart_shouldThrowBadRequest_whenRequestedQuantityExceedsStock() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(5);

        AppUser appUser = AppUser.builder()
                .id(1L)
                .username("percy")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Vanilla Aura")
                .price(new BigDecimal("89.99"))
                .stockQuantity(2)
                .imageUrl("/images/vanilla-aura.jpg")
                .build();

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByAppUserUsernameAndProductId("percy", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart("percy", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Requested quantity exceeds available stock for product: Vanilla Aura");

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_shouldThrowNotFound_whenProductDoesNotExist() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);

        AppUser appUser = AppUser.builder()
                .id(1L)
                .username("percy")
                .build();

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart("percy", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }
}