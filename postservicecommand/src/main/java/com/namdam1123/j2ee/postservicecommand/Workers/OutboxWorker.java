package com.namdam1123.j2ee.postservicecommand.Workers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderStatus;
import com.namdam1123.j2ee.postservicecommand.Entities.Order;
import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;
import com.namdam1123.j2ee.postservicecommand.Events.OrderCreatedEvent;
import com.namdam1123.j2ee.postservicecommand.Repository.OrderRepository;
import com.namdam1123.j2ee.postservicecommand.Repository.OutboxRepository;

@Component
public class OutboxWorker {

    private static final Logger logger = LoggerFactory.getLogger(OutboxWorker.class);

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000)
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findAll();

        for (OutboxEvent event : events) {
            retryTemplate.execute(context -> {
                CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("Order-event-topic", event.getPayload());
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Sent event=[" + event + "] with offset=[" + result.getRecordMetadata().offset() + "]");
                        outboxRepository.delete(event);
                    } else {
                        logger.error("Unable to send event=[" + event + "] due to : " + ex.getMessage());
                        // Handle DLQ logic here if needed
                        if (context.getRetryCount() >= 3) {
                            // Update master state or trigger rollback
                            logger.error("All retries failed for event=[" + event + "]. Triggering rollback.");
                            rollback(event);
                        }
                    }
                });
                return null;
            });
        }
    }

    private void rollback(OutboxEvent event) {
        // Implement rollback logic here
        // For example, you can update the status of the order to "FAILED"
        try {
            // Deserialize the payload to get the order ID
            OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(event.getPayload(), OrderCreatedEvent.class);
            String orderId = orderCreatedEvent.getOrderId();

            // Update the order status to "FAILED"
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            logger.info("Rolled back order with ID=[" + orderId + "]");
        } catch (Exception e) {
            logger.error("Error during rollback: ", e);
        }
    }
}