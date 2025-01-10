package com.namdam1123.j2ee.postservicecommand.Workers;

import java.util.List;
import java.util.UUID;
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
import com.namdam1123.j2ee.postservicecommand.Entities.Post;
import com.namdam1123.j2ee.postservicecommand.Entities.PostStatus;
import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;
import com.namdam1123.j2ee.postservicecommand.Events.PostCreatedEvent;
import com.namdam1123.j2ee.postservicecommand.Repository.PostRepository;
import com.namdam1123.j2ee.postservicecommand.Repository.OutboxRepository;

@Component
public class OutboxWorker {

    private static final Logger logger = LoggerFactory.getLogger(OutboxWorker.class);

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // @Scheduled(fixedRate = 5000)
    // public void processOutbox() {
    // List<OutboxEvent> events = outboxRepository.findAll();

    // for (OutboxEvent event : events) {
    // retryTemplate.execute(context -> {
    // CompletableFuture<SendResult<String, Object>> future =
    // kafkaTemplate.send("Post-event-topic",
    // event.getPayload());
    // future.whenComplete((result, ex) -> {
    // if (ex == null) {
    // logger.info(
    // "Sent event=[" + event + "] with offset=[" +
    // result.getRecordMetadata().offset() + "]");
    // outboxRepository.delete(event);
    // } else {
    // logger.error("Unable to send event=[" + event + "] due to : " +
    // ex.getMessage());
    // // Handle DLQ logic here if needed
    // if (context.getRetryCount() >= 3) {
    // // Update master state or trigger rollback
    // logger.error("All retries failed for event=[" + event + "]. Triggering
    // rollback.");
    // rollback(event);
    // }
    // }
    // });
    // return null;
    // });
    // }
    // }

    @Autowired
    private EventAggregator eventAggregator;

    @Scheduled(fixedRate = 60000) // 1 minute
    public void aggregateEvents() {
        List<OutboxEvent> events = outboxRepository.findAll();
        for (OutboxEvent event : events) {
            eventAggregator.addEvent(event);
        }

        OutboxEvent eventsToProcess = eventAggregator.processAndSendAverageEvent();
        retryTemplate.execute(context -> {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("Post-event-topic",
                    eventsToProcess.getPayload());
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info(
                            "Sent event=[" + eventsToProcess + "] with offset=[" + result.getRecordMetadata().offset() + "]");
                    outboxRepository.delete(eventsToProcess);
                } else {
                    logger.error("Unable to send event=[" + eventsToProcess + "] due to : " + ex.getMessage());
                    if (context.getRetryCount() >= 3) {
                        logger.error("All retries failed for event=[" + eventsToProcess + "]. Triggering rollback.");
                        rollback(eventsToProcess);
                    }
                }
            });
            return null;
        });
    }

    private void rollback(OutboxEvent event) {
        try {
            // Deserialize the payload to get the post ID
            PostCreatedEvent postCreatedEvent = objectMapper.readValue(event.getPayload(), PostCreatedEvent.class);
            UUID postId = postCreatedEvent.getPostId();

            // Update the post status to "FAILED"
            Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
            post.setStatus(PostStatus.FAILED);
            postRepository.save(post);

            logger.info("Rolled back post with ID=[" + postId + "]");
        } catch (Exception e) {
            logger.error("Error during rollback: ", e);
        }
    }
}