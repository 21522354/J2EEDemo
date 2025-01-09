package com.namdam1123.j2ee.postservicecommand.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicecommand.Dto.CreateOrderCommandDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;
import com.namdam1123.j2ee.postservicecommand.Entities.Order;
import com.namdam1123.j2ee.postservicecommand.Entities.OrderItem;
import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;
import com.namdam1123.j2ee.postservicecommand.Events.OrderCreatedEvent;
import com.namdam1123.j2ee.postservicecommand.Events.RollbackOrderEvent;
import com.namdam1123.j2ee.postservicecommand.Repository.OrderRepository;
import com.namdam1123.j2ee.postservicecommand.Repository.OutboxRepository;

@RestController
@RequestMapping("/api/orders")
public class OrderCommandController {

    private static final Logger logger = LoggerFactory.getLogger(OrderCommandController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/createOrder")
    @Transactional
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderCommandDTO request) {
        try {
            String orderId = UUID.randomUUID().toString();

            List<OrderItem> items = request.getItems().stream().map(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setId(UUID.randomUUID().toString());
                itemDTO.id = item.getId();
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

            logger.info("Saving order to database: " + order);
            orderRepository.save(order);
            logger.info("Order saved to database: " + order);

            OrderCreatedEvent event = new OrderCreatedEvent(
                    order.getOrderId(),
                    order.getUserId(),
                    request.getItems(),
                    order.getStatus(),
                    order.getCreatedAt());

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setId(UUID.randomUUID().toString());
            outboxEvent.setAggregateId(order.getOrderId());
            outboxEvent.setAggregateType("Order");
            outboxEvent.setEventType("OrderCreatedEvent");
            outboxEvent.setPayload(payload);
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxRepository.save(outboxEvent);

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (JsonProcessingException e) {
            logger.error("Error serializing event: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @KafkaListener(topics = "order-rollback-topic", groupId = "order-rollback-group")
    public void processRollbackOrderEvent(String payload) {
        try {
            RollbackOrderEvent event = objectMapper.readValue(payload, RollbackOrderEvent.class);
            logger.info("Received RollbackOrderEvent: {}", event);

            // Tìm order theo orderId và cập nhật trạng thái
            Optional<Order> optionalOrder = orderRepository.findById(event.getOrderId());
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
                logger.info("Order with ID [{}] has been rolled back", event.getOrderId());
            } else {
                logger.warn("Order with ID [{}] not found for rollback", event.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Error processing RollbackOrderEvent: ", e);
        }
    }
}