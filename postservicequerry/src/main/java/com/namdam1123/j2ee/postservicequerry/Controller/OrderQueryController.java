package com.namdam1123.j2ee.postservicequerry.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    public void processOrderCreatedEvent(String payload) {
        int attempts = 0;
        int maxAttempts = 1; // Số lần retry tối đa
        long delay = 1000; // Thời gian delay giữa các lần retry

        while (attempts < maxAttempts) {
            try {
                attempts++;
                OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
                log.info("Received OrderCreatedEvent: {}", event);

                // Save the order to the database
                Order order = new Order();
                order.setOrderId(event.getOrderId());
                order.setUserId(event.getUserId());
                order.setStatus(event.getStatus());
                order.setCreatedAt(event.getCreatedAt());

                List<OrderItem> items = event.getItems().stream().map(itemDTO -> {
                    OrderItem item = new OrderItem();
                    item.setId(itemDTO.id);
                    item.setProductId(itemDTO.getProductId());
                    item.setProductName(itemDTO.getProductName());
                    item.setQuantity(itemDTO.getQuantity());
                    item.setPrice(itemDTO.getPrice());
                    return item;
                }).collect(Collectors.toList());

                order.setItems(items);

                orderRepository.save(order);
                return; // Thành công, kết thúc vòng lặp
            } catch (Exception e) {
                log.error("Attempt {} failed: {}", attempts, e.getMessage(), e);
                if (attempts >= maxAttempts) {
                    handleFailure(payload, e); // Xử lý khi vượt quá số lần retry
                    return;
                }
                try {
                    Thread.sleep(delay); // Delay giữa các lần retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted", ie);
                    return;
                }
            }
        }
    }

    private void handleFailure(String payload, Exception e) {
        log.error("All retry attempts failed, handling failure for payload: {}", payload);

        try {
            // Gửi vào Dead Letter Queue (DLQ)
            kafkaTemplate.send("order-dlq-topic", payload);

            // Gửi rollback event
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            RollbackOrderEvent rollbackEvent = new RollbackOrderEvent(event.getOrderId());
            String rollbackEventPayload = objectMapper.writeValueAsString(rollbackEvent);
            kafkaTemplate.send("order-rollback-topic", rollbackEventPayload);

        } catch (Exception ex) {
            log.error("Error when handling failure: ", ex);
        }
    }
}