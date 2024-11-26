package com.namdam1123.j2ee.postservicecommand.OrderCommands.CreateOrder;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.springframework.stereotype.Component;

import com.namdam1123.j2ee.postservicecommand.Entities.Order;
import com.namdam1123.j2ee.postservicecommand.Events.OrderCreatedEvent;
import com.namdam1123.j2ee.postservicecommand.Repository.OrderRepository;

@Component
public class CreateOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final EventBus eventBus;

    public CreateOrderCommandHandler(OrderRepository orderRepository, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
    }

    @CommandHandler
    public void handle(CreateOrderCommand command) {
        // Save order to master database
        Order order = new Order();
        order.setOrderId(command.getOrderId());
        order.setUserId(command.getUserId());
        order.setProductId(command.getProductId());
        order.setProductName(command.getProductName());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus(command.getStatus());
        order.setCreatedAt(command.getCreatedAt());
        orderRepository.save(order);

        // Publish event to Kafka
        OrderCreatedEvent event = new OrderCreatedEvent(
                command.getOrderId(),
                command.getUserId(),
                command.getProductId(),
                command.getProductName(),
                command.getQuantity(),
                command.getPrice(),
                command.getStatus(),
                command.getCreatedAt());
        eventBus.publish(GenericEventMessage.asEventMessage(event));
    }
}