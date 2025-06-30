package com.ecommerce.cart.kafka;

import com.ecommerce.cart.document.Cart;
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
public class CartEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendCartUpdatedEvent(Cart cart) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "CART_UPDATED");
            event.put("userId", cart.getUserId());
            event.put("cartId", cart.getId());
            event.put("totalAmount", cart.getTotalAmount());
            event.put("itemCount", cart.getItems().size());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("cart-events", cart.getUserId().toString(), message);

            log.info("Cart updated event sent for user: {}", cart.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Error sending cart updated event", e);
        }
    }

    public void sendCartClearedEvent(Long userId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "CART_CLEARED");
            event.put("userId", userId);
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("cart-events", userId.toString(), message);

            log.info("Cart cleared event sent for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error sending cart cleared event", e);
        }
    }
}