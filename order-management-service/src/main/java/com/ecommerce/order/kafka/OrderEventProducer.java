// OrderEventProducer.java
package com.ecommerce.order.kafka;

import com.ecommerce.order.entity.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreatedEvent(Order order) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_CREATED");
            event.put("orderId", order.getId());
            event.put("orderNumber", order.getOrderNumber());
            event.put("userId", order.getUserId());
            event.put("totalAmount", order.getTotalAmount());
            event.put("paymentMethod", order.getPaymentMethod());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-events", order.getOrderNumber(), message);

            log.info("Order created event sent for order: {}", order.getOrderNumber());
        } catch (JsonProcessingException e) {
            log.error("Error sending order created event", e);
        }
    }

    public void sendOrderStatusUpdatedEvent(Order order) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_STATUS_UPDATED");
            event.put("orderId", order.getId());
            event.put("orderNumber", order.getOrderNumber());
            event.put("userId", order.getUserId());
            event.put("status", order.getStatus().toString());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-events", order.getOrderNumber(), message);

            log.info("Order status updated event sent for order: {}", order.getOrderNumber());
        } catch (JsonProcessingException e) {
            log.error("Error sending order status updated event", e);
        }
    }

    public void sendPaymentStatusUpdatedEvent(Order order) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PAYMENT_STATUS_UPDATED");
            event.put("orderId", order.getId());
            event.put("orderNumber", order.getOrderNumber());
            event.put("userId", order.getUserId());
            event.put("paymentStatus", order.getPaymentStatus().toString());
            event.put("totalAmount", order.getTotalAmount());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment-events", order.getOrderNumber(), message);

            log.info("Payment status updated event sent for order: {}", order.getOrderNumber());
        } catch (JsonProcessingException e) {
            log.error("Error sending payment status updated event", e);
        }
    }
}