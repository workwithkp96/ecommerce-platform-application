
package com.ecommerce.cart.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartItemDto {
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}