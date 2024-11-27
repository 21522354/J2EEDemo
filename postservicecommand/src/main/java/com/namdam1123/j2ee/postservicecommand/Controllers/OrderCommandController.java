package com.namdam1123.j2ee.postservicecommand.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import com.namdam1123.j2ee.postservicecommand.Dto.CreateOrderCommandDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;
import com.namdam1123.j2ee.postservicecommand.Entities.Order;
import com.namdam1123.j2ee.postservicecommand.Entities.OrderItem;
import com.namdam1123.j2ee.postservicecommand.Events.OrderCreatedEvent;
import com.namdam1123.j2ee.postservicecommand.OrderCommands.CreateOrder.CreateOrderCommand;
import com.namdam1123.j2ee.postservicecommand.Repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
public class OrderCommandController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/createOrder")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderCommandDTO request) {
        try {
            String orderId = UUID.randomUUID().toString();

            List<OrderItem> items = request.getItems().stream().map(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setProductId(itemDTO.getProductId());
                item.setProductName(itemDTO.getProductName());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice());
                return item;
            }).collect(Collectors.toList());

            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(request.getUserId());
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setItems(items);

            orderRepository.save(order);

            OrderCreatedEvent event = new OrderCreatedEvent(
                    order.getOrderId(),
                    order.getUserId(),
                    request.getItems(),
                    order.getStatus(),
                    order.getCreatedAt());

            kafkaTemplate.send("Order-event-topic", event);

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}