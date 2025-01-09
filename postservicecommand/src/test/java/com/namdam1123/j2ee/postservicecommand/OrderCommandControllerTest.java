package com.namdam1123.j2ee.postservicecommand;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.namdam1123.j2ee.postservicecommand.Controllers.OrderCommandController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;

import com.namdam1123.j2ee.postservicecommand.Dto.CreateOrderCommandDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.OrderItemDTO;
import com.namdam1123.j2ee.postservicecommand.Entities.Order;
import com.namdam1123.j2ee.postservicecommand.Repository.OrderRepository;

public class OrderCommandControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private RetryTemplate retryTemplate;

    @InjectMocks
    private OrderCommandController orderCommandController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOrder_RetryOnKafkaSendFailure() {
        // Arrange
        CreateOrderCommandDTO request = new CreateOrderCommandDTO();
        request.setUserId("user123");
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId("product123");
        itemDTO.setProductName("Product Name");
        itemDTO.setQuantity(1);
        itemDTO.setPrice(100.0);
        request.setItems(Collections.singletonList(itemDTO));

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka send failed"));

        when(kafkaTemplate.send(any(String.class), any(Object.class))).thenReturn(failedFuture);

        // Act
        ResponseEntity<Order> response = orderCommandController.createOrder(request);

        // Assert
        assert(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    }
}