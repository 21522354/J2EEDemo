package com.namdam1123.j2ee.postservicecommand.Controllers;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.namdam1123.j2ee.postservicecommand.Dto.CreateOrderCommandDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;
import com.namdam1123.j2ee.postservicecommand.OrderCommands.CreateOrder.CreateOrderCommand;

import lombok.RequiredArgsConstructor;

// OrderCommandController.java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCommandController {
    private final CommandGateway commandGateway;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<String> createOrder(@RequestBody CreateOrderCommandDTO request) {
        String orderId = UUID.randomUUID().toString();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .productId(request.getProductId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return commandGateway.send(command);
    }
}