package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.ProductRequest;
import com.percybuilder.ecommerce.dtos.ProductResponse;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.Category;
import com.percybuilder.ecommerce.models.Product;
import com.percybuilder.ecommerce.repositories.CategoryRepository;
import com.percybuilder.ecommerce.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldCreateProduct_whenCategoryExists() {
        ProductRequest request = new ProductRequest();
        request.setName("Vanilla Aura");
        request.setDescription("Warm vanilla fragrance");
        request.setBrand("Apex Fragrance");
        request.setPrice(BigDecimal.valueOf(89.99));
        request.setStockQuantity(20);
        request.setImageUrl("/images/vanilla-aura.jpg");
        request.setCategoryId(1L);

        Category category = Category.builder()
                .id(1L)
                .name("Perfumes")
                .description("Luxury fragrance products")
                .build();

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Vanilla Aura")
                .description("Warm vanilla fragrance")
                .brand("Apex Fragrance")
                .price(BigDecimal.valueOf(89.99))
                .stockQuantity(20)
                .imageUrl("/images/vanilla-aura.jpg")
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Vanilla Aura");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Perfumes");

        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowNotFound_whenCategoryDoesNotExist() {
        ProductRequest request = new ProductRequest();
        request.setName("Vanilla Aura");
        request.setDescription("Warm vanilla fragrance");
        request.setBrand("Apex Fragrance");
        request.setPrice(BigDecimal.valueOf(89.99));
        request.setStockQuantity(20);
        request.setImageUrl("/images/vanilla-aura.jpg");
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");

        verify(categoryRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        Category category = Category.builder()
                .id(1L)
                .name("Perfumes")
                .description("Luxury fragrance products")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Vanilla Aura")
                .description("Warm vanilla fragrance")
                .brand("Apex Fragrance")
                .price(BigDecimal.valueOf(89.99))
                .stockQuantity(20)
                .imageUrl("/images/vanilla-aura.jpg")
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Vanilla Aura");
        assertThat(response.getCategoryName()).isEqualTo("Perfumes");

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_shouldThrowNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");

        verify(productRepository).findById(999L);
    }
}