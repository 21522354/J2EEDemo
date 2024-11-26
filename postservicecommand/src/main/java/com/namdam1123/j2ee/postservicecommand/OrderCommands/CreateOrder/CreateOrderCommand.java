package com.namdam1123.j2ee.postservicecommand.OrderCommands.CreateOrder;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;

@Data
@Builder
public class CreateOrderCommand {
    private String orderId;
    private String userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;
}