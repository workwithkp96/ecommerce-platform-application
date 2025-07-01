package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class OrderManagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderManagementServiceApplication.class, args);
    }
}