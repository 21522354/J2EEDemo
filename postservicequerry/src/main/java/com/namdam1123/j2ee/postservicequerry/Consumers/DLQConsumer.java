package com.namdam1123.j2ee.postservicequerry.Consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DLQConsumer {

    private static final Logger log = LoggerFactory.getLogger(DLQConsumer.class);

    @KafkaListener(topics = "order-dlq-topic", groupId = "dlq-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received message from DLQ: {}", record.value());
        // Process the message as needed
    }
}