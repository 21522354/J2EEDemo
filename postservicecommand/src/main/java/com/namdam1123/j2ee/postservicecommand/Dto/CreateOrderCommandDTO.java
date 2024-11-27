package com.namdam1123.j2ee.postservicecommand.Dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderCommandDTO {
    private String userId;
    private List<OrderItemDTO> items;
}
