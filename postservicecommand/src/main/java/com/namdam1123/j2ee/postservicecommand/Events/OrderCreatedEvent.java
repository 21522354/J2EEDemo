package com.namdam1123.j2ee.postservicecommand.Events;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderItemDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;

import lombok.Data;

@Data
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private List<OrderItemDTO> items;
    private OrderStatus status;
    private LocalDateTime createdAt;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("userId") String userId,
            @JsonProperty("items") List<OrderItemDTO> items,
            @JsonProperty("status") OrderStatus status,
            @JsonProperty("createdAt") LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}