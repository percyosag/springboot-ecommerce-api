package com.percybuilder.ecommerce.repositories;

import com.percybuilder.ecommerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}