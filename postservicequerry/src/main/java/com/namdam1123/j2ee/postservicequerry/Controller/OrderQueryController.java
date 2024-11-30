package com.namdam1123.j2ee.postservicequerry.Controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicequerry.Entities.Order;
import com.namdam1123.j2ee.postservicequerry.Entities.OrderItem;
import com.namdam1123.j2ee.postservicequerry.Events.OrderCreatedEvent;
import com.namdam1123.j2ee.postservicequerry.Events.RollbackOrderEvent;
import com.namdam1123.j2ee.postservicequerry.Repository.OrderRepository;

@RestController
@RequestMapping(path = "/api/orders")
public class OrderQueryController {
    private static final Logger log = LoggerFactory.getLogger(OrderQueryController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{orderId}")
    public Order getOrderById(@PathVariable String orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.orElse(null);
    }

    @KafkaListener(topics = "Order-event-topic", groupId = "order-event-group")
    @Retryable(value = { SQLException.class }, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void processOrderCreatedEvent(String payload) {
        OrderCreatedEvent event = null; // Khai báo biến event bên ngoài khối try
        try {
            event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent: {}", event);

            // Save the order to the database
            Order order = new Order();
            order.setOrderId(event.getOrderId());
            order.setUserId(event.getUserId());
            order.setStatus(event.getStatus());
            order.setCreatedAt(event.getCreatedAt());

            List<OrderItem> items = event.getItems().stream().map(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setId(UUID.randomUUID().toString());
                item.setProductId(itemDTO.getProductId());
                item.setProductName(itemDTO.getProductName());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice());
                return item;
            }).collect(Collectors.toList());

            order.setItems(items);

            // Lưu vào cơ sở dữ liệu
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: ", e);
            // If saving the order fails, send a rollback event
            if (event != null) {
                RollbackOrderEvent rollbackEvent = new RollbackOrderEvent(event.getOrderId());
                kafkaTemplate.send("order-rollback-topic", rollbackEvent);
            }
        }
    }

    @Recover
    public void recover(SQLException e, String payload) {
        log.error("Failed to process OrderCreatedEvent after retries, sending to DLQ", e);
        kafkaTemplate.send("order-dlq-topic", payload);
    }
}