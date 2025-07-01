package com.ecommerce.order.service;

import com.ecommerce.order.dto.CreateOrderDto;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderTrackingDto;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.kafka.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final RestTemplate restTemplate;

    @Transactional
    public OrderDto createOrder(CreateOrderDto createOrderDto) {
        // Validate products and calculate total
        List<OrderItem> orderItems = createOrderDto.getOrderItems().stream()
                .map(itemDto -> {
                    Map<String, Object> productDetails = getProductDetails(itemDto.getProductId());
                    if (productDetails == null) {
                        throw new RuntimeException("Product not found: " + itemDto.getProductId());
                    }

                    String productName = (String) productDetails.get("name");
                    BigDecimal price = new BigDecimal(productDetails.get("price").toString());
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

                    return OrderItem.builder()
                            .productId(itemDto.getProductId())
                            .productName(productName)
                            .price(price)
                            .quantity(itemDto.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create shipping address
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .fullName(createOrderDto.getShippingAddress().getFullName())
                .addressLine1(createOrderDto.getShippingAddress().getAddressLine1())
                .addressLine2(createOrderDto.getShippingAddress().getAddressLine2())
                .city(createOrderDto.getShippingAddress().getCity())
                .state(createOrderDto.getShippingAddress().getState())
                .postalCode(createOrderDto.getShippingAddress().getPostalCode())
                .country(createOrderDto.getShippingAddress().getCountry())
                .phoneNumber(createOrderDto.getShippingAddress().getPhoneNumber())
                .build();

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(createOrderDto.getUserId())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(shippingAddress)
                .paymentMethod(createOrderDto.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // Set order reference for items
        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear user's cart
        clearUserCart(createOrderDto.getUserId());

        // Send order created event
        orderEventProducer.sendOrderCreatedEvent(savedOrder);

        return mapToOrderDto(savedOrder);
    }

    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToOrderDto(order);
    }

    public OrderDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToOrderDto(order);
    }

    public Page<OrderDto> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);

        return orders.map(this::mapToOrderDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Send order status update event
        orderEventProducer.sendOrderStatusUpdatedEvent(updatedOrder);

        return mapToOrderDto(updatedOrder);
    }

    @Transactional
    public OrderDto updatePaymentStatus(String orderNumber, PaymentStatus paymentStatus) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(paymentStatus);

        // Update order status based on payment status
        if (paymentStatus == PaymentStatus.COMPLETED) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else if (paymentStatus == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        Order updatedOrder = orderRepository.save(order);

        // Send payment status update event
        orderEventProducer.sendPaymentStatusUpdatedEvent(updatedOrder);

        return mapToOrderDto(updatedOrder);
    }

    public OrderTrackingDto getOrderTracking(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderTrackingDto tracking = new OrderTrackingDto();
        tracking.setOrderNumber(order.getOrderNumber());
        tracking.setStatus(order.getStatus().toString());
        tracking.setStatusDescription(getStatusDescription(order.getStatus()));
        tracking.setLastUpdated(order.getUpdatedAt());
        tracking.setEstimatedDelivery(calculateEstimatedDelivery(order));

        return tracking;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Map<String, Object> getProductDetails(Long productId) {
        try {
            String productServiceUrl = "http://localhost:8082/api/products/" + productId;
            return restTemplate.getForObject(productServiceUrl, Map.class);
        } catch (Exception e) {
            log.error("Error fetching product details for productId: {}", productId, e);
            return null;
        }
    }

    private void clearUserCart(Long userId) {
        try {
            String cartServiceUrl = "http://localhost:8083/api/cart/users/" + userId;
            restTemplate.delete(cartServiceUrl);
        } catch (Exception e) {
            log.error("Error clearing cart for user: {}", userId, e);
        }
    }

    private String getStatusDescription(OrderStatus status) {
        switch (status) {
            case PENDING: return "Order is being processed";
            case CONFIRMED: return "Order has been confirmed";
            case PROCESSING: return "Order is being prepared";
            case SHIPPED: return "Order has been shipped";
            case DELIVERED: return "Order has been delivered";
            case CANCELLED: return "Order has been cancelled";
            default: return "Unknown status";
        }
    }

    private String calculateEstimatedDelivery(Order order) {
        // Simple estimation logic - can be enhanced based on shipping method, location, etc.
        return order.getCreatedAt().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private OrderDto mapToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().toString());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus().toString());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Map shipping address
        OrderDto.ShippingAddressDto addressDto = new OrderDto.ShippingAddressDto();
        addressDto.setFullName(order.getShippingAddress().getFullName());
        addressDto.setAddressLine1(order.getShippingAddress().getAddressLine1());
        addressDto.setAddressLine2(order.getShippingAddress().getAddressLine2());
        addressDto.setCity(order.getShippingAddress().getCity());
        addressDto.setState(order.getShippingAddress().getState());
        addressDto.setPostalCode(order.getShippingAddress().getPostalCode());
        addressDto.setCountry(order.getShippingAddress().getCountry());
        addressDto.setPhoneNumber(order.getShippingAddress().getPhoneNumber());
        dto.setShippingAddress(addressDto);

        // Map order items
        List<OrderDto.OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(item -> {
                    OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
                    itemDto.setId(item.getId());
                    itemDto.setProductId(item.getProductId());
                    itemDto.setProductName(item.getProductName());
                    itemDto.setPrice(item.getPrice());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setSubtotal(item.getSubtotal());
                    return itemDto;
                })
                .collect(Collectors.toList());
        dto.setOrderItems(itemDtos);

        return dto;
    }
}