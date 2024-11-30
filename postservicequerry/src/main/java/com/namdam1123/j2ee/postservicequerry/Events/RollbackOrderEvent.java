package com.namdam1123.j2ee.postservicequerry.Events;

public class RollbackOrderEvent {
    private String orderId;

    public RollbackOrderEvent() {
    }

    public RollbackOrderEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "RollbackOrderEvent{" +
                "orderId='" + orderId + '\'' +
                '}';
    }
}