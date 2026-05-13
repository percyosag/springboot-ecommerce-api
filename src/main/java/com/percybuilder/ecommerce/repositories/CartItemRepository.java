package com.percybuilder.ecommerce.repositories;

import com.percybuilder.ecommerce.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByAppUserUsername(String username);

    Optional<CartItem> findByAppUserUsernameAndProductId(String username, Long productId);
}