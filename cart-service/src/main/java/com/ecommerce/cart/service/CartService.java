package com.ecommerce.cart.service;

import com.ecommerce.cart.document.Cart;
import com.ecommerce.cart.dto.AddToCartDto;
import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.dto.UpdateCartItemDto;
import com.ecommerce.cart.kafka.CartEventProducer;
import com.ecommerce.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartEventProducer cartEventProducer;
    private final RestTemplate restTemplate;

    @CacheEvict(value = "carts", key = "#userId")
    public CartDto addToCart(Long userId, AddToCartDto addToCartDto) {
        // Get product details from product service
        Map<String, Object> productDetails = getProductDetails(addToCartDto.getProductId());

        if (productDetails == null) {
            throw new RuntimeException("Product not found");
        }

        String productName = (String) productDetails.get("name");
        BigDecimal price = new BigDecimal(productDetails.get("price").toString());

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .totalAmount(BigDecimal.ZERO)
                        .createdAt(LocalDateTime.now())
                        .build());

        // Check if product already in cart
        Optional<Cart.CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(addToCartDto.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            existingItem.get().setQuantity(existingItem.get().getQuantity() + addToCartDto.getQuantity());
            existingItem.get().setSubtotal(existingItem.get().getPrice().multiply(BigDecimal.valueOf(existingItem.get().getQuantity())));
        } else {
            // Add new item
            Cart.CartItem newItem = Cart.CartItem.builder()
                    .productId(addToCartDto.getProductId())
                    .productName(productName)
                    .price(price)
                    .quantity(addToCartDto.getQuantity())
                    .subtotal(price.multiply(BigDecimal.valueOf(addToCartDto.getQuantity())))
                    .build();

            cart.getItems().add(newItem);
        }

        // Recalculate total
        cart.setTotalAmount(cart.getItems().stream()
                .map(Cart.CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        // Send cart update event
        cartEventProducer.sendCartUpdatedEvent(savedCart);

        return mapToCartDto(savedCart);
    }

    @Cacheable(value = "carts", key = "#userId")
    public CartDto getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .totalAmount(BigDecimal.ZERO)
                        .createdAt(LocalDateTime.now())
                        .build());

        return mapToCartDto(cart);
    }

    @CacheEvict(value = "carts", key = "#userId")
    public CartDto updateCartItem(Long userId, Long productId, UpdateCartItemDto updateDto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Cart.CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        item.setQuantity(updateDto.getQuantity());
        item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(updateDto.getQuantity())));

        // Recalculate total
        cart.setTotalAmount(cart.getItems().stream()
                .map(Cart.CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        // Send cart update event
        cartEventProducer.sendCartUpdatedEvent(savedCart);

        return mapToCartDto(savedCart);
    }

    @CacheEvict(value = "carts", key = "#userId")
    public CartDto removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        // Recalculate total
        cart.setTotalAmount(cart.getItems().stream()
                .map(Cart.CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        // Send cart update event
        cartEventProducer.sendCartUpdatedEvent(savedCart);

        return mapToCartDto(savedCart);
    }

    @CacheEvict(value = "carts", key = "#userId")
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);

        // Send cart cleared event
        cartEventProducer.sendCartClearedEvent(userId);
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

    private CartDto mapToCartDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        List<CartDto.CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> {
                    CartDto.CartItemDto itemDto = new CartDto.CartItemDto();
                    itemDto.setProductId(item.getProductId());
                    itemDto.setProductName(item.getProductName());
                    itemDto.setPrice(item.getPrice());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setSubtotal(item.getSubtotal());
                    return itemDto;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}