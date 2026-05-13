
package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.ProductRequest;
import com.percybuilder.ecommerce.dtos.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest productRequest);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);
}