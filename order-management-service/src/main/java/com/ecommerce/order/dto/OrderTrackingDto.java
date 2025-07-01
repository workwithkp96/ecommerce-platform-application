package com.ecommerce.order.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderTrackingDto {
    private String orderNumber;
    private String status;
    private String statusDescription;
    private LocalDateTime lastUpdated;
    private String estimatedDelivery;
}