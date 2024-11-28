package com.namdam1123.j2ee.postservicequerry.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.namdam1123.j2ee.postservicequerry.Entities.Order;
import com.namdam1123.j2ee.postservicequerry.Entities.OrderItem;
import com.namdam1123.j2ee.postservicequerry.Events.OrderCreatedEvent;
import com.namdam1123.j2ee.postservicequerry.Repository.OrderRepository;

@RestController
@RequestMapping(path = "/api/orders")
public class OrderQueryController {
    private static final Logger log = LoggerFactory.getLogger(OrderQueryController.class);

    @Autowired
    private OrderRepository orderRepository;

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
    public void processOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
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
        orderRepository.save(order);
    }
}