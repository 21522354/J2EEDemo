package com.namdam1123.j2ee.postservicecommand.Dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderCommandDTO {
    private String userId;
    private List<OrderItemDTO> items;

    public String getUserId() {
        return userId;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}