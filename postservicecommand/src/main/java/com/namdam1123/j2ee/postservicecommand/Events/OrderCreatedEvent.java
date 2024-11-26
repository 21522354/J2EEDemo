package com.namdam1123.j2ee.postservicecommand.Events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;
}