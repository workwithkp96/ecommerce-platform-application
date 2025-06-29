package com.ecommerce.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSearchDto {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String status;
    private int page = 0;
    private int size = 20;
}