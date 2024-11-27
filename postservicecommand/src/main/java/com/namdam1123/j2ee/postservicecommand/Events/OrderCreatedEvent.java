package com.namdam1123.j2ee.postservicecommand.Events;

import java.time.LocalDateTime;
import java.util.List;

import com.namdam1123.j2ee.postservicecommand.Dto.OrderItemDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private List<OrderItemDTO> items;
    private OrderStatus status;
    private LocalDateTime createdAt;
}