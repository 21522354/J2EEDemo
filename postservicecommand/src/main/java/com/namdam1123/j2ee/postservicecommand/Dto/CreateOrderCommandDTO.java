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
}