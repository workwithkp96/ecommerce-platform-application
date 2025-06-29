package com.ecommerce.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductUpdateDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Long categoryId;
}