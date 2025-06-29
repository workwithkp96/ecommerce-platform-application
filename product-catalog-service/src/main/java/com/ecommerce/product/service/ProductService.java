package com.ecommerce.product.service;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductStatus;
import com.ecommerce.product.kafka.ProductEventProducer;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductDocumentRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductEventProducer productEventProducer;

    @Transactional
    public ProductDto createProduct(ProductCreateDto createDto) {
        if (createDto.getSku() != null && productRepository.existsBySku(createDto.getSku())) {
            throw new RuntimeException("SKU already exists");
        }

        Category category = categoryRepository.findById(createDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = Product.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .price(createDto.getPrice())
                .stockQuantity(createDto.getStockQuantity())
                .sku(createDto.getSku())
                .imageUrl(createDto.getImageUrl())
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);

        // Index in Elasticsearch
        indexProductInElasticsearch(savedProduct);

        // Send product creation event
        productEventProducer.sendProductCreatedEvent(savedProduct);

        return mapToProductDto(savedProduct);
    }

    public ProductDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToProductDto(product);
    }

    public Page<ProductDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(this::mapToProductDto);
    }

    public Page<ProductDto> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);

        return products.map(this::mapToProductDto);
    }

    @Transactional
    public ProductDto updateProduct(Long productId, ProductUpdateDto updateDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (updateDto.getName() != null) {
            product.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            product.setDescription(updateDto.getDescription());
        }
        if (updateDto.getPrice() != null) {
            product.setPrice(updateDto.getPrice());
        }
        if (updateDto.getStockQuantity() != null) {
            product.setStockQuantity(updateDto.getStockQuantity());
        }
        if (updateDto.getImageUrl() != null) {
            product.setImageUrl(updateDto.getImageUrl());
        }
        if (updateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);

        // Update in Elasticsearch
        indexProductInElasticsearch(updatedProduct);

        // Send product update event
        productEventProducer.sendProductUpdatedEvent(updatedProduct);

        return mapToProductDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);
        productDocumentRepository.deleteById(productId);

        // Send product deletion event
        productEventProducer.sendProductDeletedEvent(product);
    }

    public Page<ProductDto> searchProducts(ProductSearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());

        if (searchDto.getKeyword() != null && !searchDto.getKeyword().trim().isEmpty()) {
            Page<ProductDocument> documents = productDocumentRepository
                    .findByNameContainingOrDescriptionContaining(
                            searchDto.getKeyword(), searchDto.getKeyword(), pageable);

            return documents.map(this::mapDocumentToProductDto);
        }

        if (searchDto.getCategoryId() != null) {
            Page<ProductDocument> documents = productDocumentRepository
                    .findByCategoryId(searchDto.getCategoryId(), pageable);

            return documents.map(this::mapDocumentToProductDto);
        }

        // Default search - return all products
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToProductDto);
    }

    private void indexProductInElasticsearch(Product product) {
        ProductDocument document = ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus().toString())
                .createdAt(product.getCreatedAt())
                .build();

        productDocumentRepository.save(document);
    }

    private ProductDto mapToProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setStatus(product.getStatus().toString());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }

    private ProductDto mapDocumentToProductDto(ProductDocument document) {
        ProductDto dto = new ProductDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setDescription(document.getDescription());
        dto.setPrice(document.getPrice());
        dto.setStockQuantity(document.getStockQuantity());
        dto.setSku(document.getSku());
        dto.setImageUrl(document.getImageUrl());
        dto.setCategoryId(document.getCategoryId());
        dto.setCategoryName(document.getCategoryName());
        dto.setStatus(document.getStatus());
        dto.setCreatedAt(document.getCreatedAt());
        return dto;
    }
}