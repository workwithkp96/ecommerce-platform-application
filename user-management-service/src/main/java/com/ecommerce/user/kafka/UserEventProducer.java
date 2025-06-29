package com.ecommerce.user.kafka;

import com.ecommerce.user.entity.User;
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
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserRegistrationEvent(User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "USER_REGISTERED");
            event.put("userId", user.getId());
            event.put("email", user.getEmail());
            event.put("firstName", user.getFirstName());
            event.put("lastName", user.getLastName());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-events", user.getId().toString(), message);

            log.info("User registration event sent for user: {}", user.getEmail());
        } catch (JsonProcessingException e) {
            log.error("Error sending user registration event", e);
        }
    }

    public void sendUserUpdateEvent(User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "USER_UPDATED");
            event.put("userId", user.getId());
            event.put("email", user.getEmail());
            event.put("firstName", user.getFirstName());
            event.put("lastName", user.getLastName());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-events", user.getId().toString(), message);

            log.info("User update event sent for user: {}", user.getEmail());
        } catch (JsonProcessingException e) {
            log.error("Error sending user update event", e);
        }
    }

    public void sendPasswordResetEvent(User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PASSWORD_RESET_REQUESTED");
            event.put("userId", user.getId());
            event.put("email", user.getEmail());
            event.put("timestamp", LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-events", user.getId().toString(), message);

            log.info("Password reset event sent for user: {}", user.getEmail());
        } catch (JsonProcessingException e) {
            log.error("Error sending password reset event", e);
        }
    }
}