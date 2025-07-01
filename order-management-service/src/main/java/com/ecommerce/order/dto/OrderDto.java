package com.ecommerce.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private List<OrderItemDto> orderItems;
    private BigDecimal totalAmount;
    private String status;
    private ShippingAddressDto shippingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    @Data
    public static class ShippingAddressDto {
        private String fullName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phoneNumber;
    }
}

// CreateOrderDto.java


// OrderTrackingDto.java
