package com.namdam1123.j2ee.postservicecommand.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateOrderCommandDTO {
    private String userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
