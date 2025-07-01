package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemCreateDto> orderItems;

    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressCreateDto shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @Data
    public static class OrderItemCreateDto {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }

    @Data
    public static class ShippingAddressCreateDto {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;

        private String addressLine2;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        @NotBlank(message = "Country is required")
        private String country;

        private String phoneNumber;
    }
}