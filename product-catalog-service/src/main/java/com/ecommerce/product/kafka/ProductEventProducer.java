package com.ecommerce.product.kafka;

import com.ecommerce.product.entity.Product;
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
public class ProductEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendProductCreatedEvent(Product product) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PRODUCT_CREATED");
            event.put("productId", product.getId());
            event.put("name", product.getName());
            event.put("price", product.getPrice());
            event.put("stockQuantity", product.getStockQuantity());
            event.put("categoryId", product.getCategory().getId());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("product-events", product.getId().toString(), message);

            log.info("Product created event sent for product: {}", product.getName());
        } catch (JsonProcessingException e) {
            log.error("Error sending product created event", e);
        }
    }

    public void sendProductUpdatedEvent(Product product) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PRODUCT_UPDATED");
            event.put("productId", product.getId());
            event.put("name", product.getName());
            event.put("price", product.getPrice());
            event.put("stockQuantity", product.getStockQuantity());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("product-events", product.getId().toString(), message);

            log.info("Product updated event sent for product: {}", product.getName());
        } catch (JsonProcessingException e) {
            log.error("Error sending product updated event", e);
        }
    }

    public void sendProductDeletedEvent(Product product) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PRODUCT_DELETED");
            event.put("productId", product.getId());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("product-events", product.getId().toString(), message);

            log.info("Product deleted event sent for product: {}", product.getName());
        } catch (JsonProcessingException e) {
            log.error("Error sending product deleted event", e);
        }
    }
}