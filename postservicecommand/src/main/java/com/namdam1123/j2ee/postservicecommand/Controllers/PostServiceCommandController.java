package com.namdam1123.j2ee.postservicecommand.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicecommand.Dto.PostStatus;
import com.namdam1123.j2ee.postservicecommand.Dto.PostDTOs.CreatePostCommandDTO;
import com.namdam1123.j2ee.postservicecommand.Dto.PostDTOs.PostEvent;
import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;
import com.namdam1123.j2ee.postservicecommand.Entities.Post;
import com.namdam1123.j2ee.postservicecommand.Events.PostCreatedEvent;
import com.namdam1123.j2ee.postservicecommand.Events.PostEvents.RollbackPostEvent;
import com.namdam1123.j2ee.postservicecommand.Repository.OutboxRepository;
import com.namdam1123.j2ee.postservicecommand.Repository.PostRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/posts")
public class PostServiceCommandController {
    @Autowired
    private PostRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(OrderCommandController.class);

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/createPost")
    @Transactional
    ResponseEntity<Post> createPost(@RequestBody CreatePostCommandDTO postDTO) {
        try {
            Post post = new Post();
            post.setPostId(UUID.randomUUID());
            post.setStatus(PostStatus.PENDING);
            post.setUserId(postDTO.getUserId());
            post.setTitle(postDTO.getTitle());
            post.setNumberOfLike(0);
            post.setCreatedDate((LocalDateTime.now()));

            repository.save(post);

            // Create PostCreatedEvent
            PostCreatedEvent event = new PostCreatedEvent(post);

            // Convert event to JSON string
            String payload = objectMapper.writeValueAsString(event);

            // Create and save OutboxEvent
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setId(UUID.randomUUID().toString());
            outboxEvent.setAggregateId(post.getPostId().toString());
            outboxEvent.setAggregateType("Post");
            outboxEvent.setEventType("PostCreatedEvent");
            outboxEvent.setPayload(payload);
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxRepository.save(outboxEvent);

            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing event: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @KafkaListener(topics = "post-rollback-topic", groupId = "post-rollback-group")
    public void processRollbackPostEvent(String payload) {
        try {
            RollbackPostEvent event = objectMapper.readValue(payload, RollbackPostEvent.class);
            logger.info("Received RollbackPostEvent: {}", event);

            // Find post by postId and delete it
            Optional<Post> optionalPost = repository.findById(event.getPostId());
            if (optionalPost.isPresent()) {
                repository.delete(optionalPost.get());
                logger.info("Post with ID [{}] has been rolled back", event.getPostId());
            } else {
                logger.warn("Post with ID [{}] not found for rollback", event.getPostId());
            }
        } catch (Exception e) {
            logger.error("Error processing RollbackPostEvent: ", e);
        }
    }

    @PutMapping("/updatePost/{postId}")
    ResponseEntity<Post> updatePost(@RequestBody Post post, @PathVariable UUID postId) {
        Optional<Post> existPost = repository.findById(postId);
        if (existPost.isPresent()) {
            Post oldPost = existPost.get();
            oldPost.setUserId(post.getUserId());
            oldPost.setCreatedDate(post.getCreatedDate());
            oldPost.setTitle(post.getTitle());
            oldPost.setNumberOfLike(post.getNumberOfLike());
            repository.save(oldPost);
            return ResponseEntity.status(HttpStatus.OK).body(oldPost);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @DeleteMapping("deletePost/{postId}")
    ResponseEntity<String> deletePost(@PathVariable UUID postId) {
        Optional<Post> existPost = repository.findById(postId);
        if (existPost.isPresent()) {
            repository.delete(existPost.get());
            return ResponseEntity.status(HttpStatus.OK).body("Delete post successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
