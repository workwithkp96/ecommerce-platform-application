package com.ecommerce.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private String status;
    private LocalDateTime createdAt;
}
