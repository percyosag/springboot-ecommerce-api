package com.percybuilder.ecommerce.repositories;

import com.percybuilder.ecommerce.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}