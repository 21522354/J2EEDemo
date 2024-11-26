package com.namdam1123.j2ee.postservicecommand.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Order {
    @Id
    private String orderId;
    private String userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;
}