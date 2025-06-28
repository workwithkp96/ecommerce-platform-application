# E-commerce Platform

Microservices-based e-commerce platform built with Spring Boot 3.x and Java 17.

## Services
- User Management Service
- Product Catalog Service
- Cart Service
- Order Management Service
- Payment Service
- Notification Service
- API Gateway

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0+
- MongoDB 6.0+
- Redis 7.0+
- Elasticsearch 8.x
- Apache Kafka

## Build
```bash
mvn clean install


user-management-service/
├── pom.xml
├── src/main/java/com/ecommerce/user/
│   ├── UserManagementServiceApplication.java
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── kafka/
└── src/main/resources/
    └── application.yml