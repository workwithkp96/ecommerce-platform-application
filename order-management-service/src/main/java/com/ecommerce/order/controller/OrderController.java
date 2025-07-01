package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderDto;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderTrackingDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.entity.PaymentStatus;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
        OrderDto order = orderService.createOrder(createOrderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDto order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<OrderDto>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderDto> orders = orderService.getUserOrders(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderDto order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/number/{orderNumber}/payment-status")
    public ResponseEntity<OrderDto> updatePaymentStatus(
            @PathVariable String orderNumber,
            @RequestParam PaymentStatus paymentStatus) {
        OrderDto order = orderService.updatePaymentStatus(orderNumber, paymentStatus);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/tracking/{orderNumber}")
    public ResponseEntity<OrderTrackingDto> getOrderTracking(@PathVariable String orderNumber) {
        OrderTrackingDto tracking = orderService.getOrderTracking(orderNumber);
        return ResponseEntity.ok(tracking);
    }
}