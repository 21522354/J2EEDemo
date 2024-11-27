package com.namdam1123.j2ee.postservicecommand.Dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String productId;
    private String productName;
    private int quantity;
    private double price;

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}