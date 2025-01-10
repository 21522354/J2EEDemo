package com.namdam1123.j2ee.postservicequerry.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicequerry.Entities.Post;
import com.namdam1123.j2ee.postservicequerry.Entities.PostStatus;
import com.namdam1123.j2ee.postservicequerry.Events.PostCreatedEvent;
import com.namdam1123.j2ee.postservicequerry.Events.PostEvents.RollbackPostEvent;
import com.namdam1123.j2ee.postservicequerry.Repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api/posts")
public class PostServiceQuerryController {
    private static final Logger log = LoggerFactory.getLogger(PostServiceQuerryController.class);

    @Autowired
    PostRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RedisTemplate template;


    @GetMapping
    public List<Post> getAllPost() {
        try {
            // Kiểm tra dữ liệu trong Redis cache
            String cachedPostsJson = (String) template.opsForValue().get("postss");
            if (cachedPostsJson != null) {
                // Nếu có dữ liệu trong cache, chuyển đổi từ JSON sang danh sách Post và trả về
                List<Post> cachedPosts = objectMapper.readValue(cachedPostsJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Post.class));
                log.info("Returning posts from cache");
                return cachedPosts;
            }

            // Nếu không có dữ liệu trong cache, lấy từ database
            List<Post> posts = repository.findAll();
            if (!posts.isEmpty()) {
                // Chuyển danh sách bài viết thành JSON và lưu vào Redis cache với TTL là 10 giây
                String postsJson = objectMapper.writeValueAsString(posts);
                template.opsForValue().set("postss", postsJson, 10, TimeUnit.SECONDS);
                log.info("Caching posts into Redis with TTL of 10 seconds");
            }

            // Trả về danh sách bài viết từ database
            return posts;
        } catch (Exception e) {
            log.error("Error while handling cache or database: ", e);
            // Trong trường hợp lỗi, trả về danh sách rỗng
            return List.of();
        }
    }
    @GetMapping("/user/{UserId}")
    public List<Post> getPostsByUserId(@PathVariable UUID UserId) {
        try {
            // Tạo khóa cache duy nhất theo userId
            String cacheKey = "PostData_User_" + UserId;

            // Kiểm tra dữ liệu trong Redis cache
            String cachedPostsJson = (String) template.opsForValue().get(cacheKey);
            if (cachedPostsJson != null) {
                // Nếu có dữ liệu trong cache, chuyển đổi từ JSON sang danh sách Post và trả về
                List<Post> cachedPosts = objectMapper.readValue(cachedPostsJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Post.class));
                log.info("Returning posts from cache for userId: {}", UserId);
                return cachedPosts;
            }

            // Nếu không có dữ liệu trong cache, lấy từ database
            List<Post> posts = repository.findByUserId(UserId);
            if (!posts.isEmpty()) {
                // Chuyển danh sách bài viết thành JSON và lưu vào Redis cache với TTL là 10 giây
                String postsJson = objectMapper.writeValueAsString(posts);
                template.opsForValue().set(cacheKey, postsJson, 10, TimeUnit.SECONDS);
                log.info("Caching posts into Redis for userId: {} with TTL of 10 seconds", UserId);
            }

            // Trả về danh sách bài viết từ database
            return posts;
        } catch (Exception e) {
            log.error("Error while handling cache or database for userId: {}: ", UserId, e);
            // Trong trường hợp lỗi, trả về danh sách rỗng
            return List.of();
        }
    }


    @KafkaListener(topics = "Post-event-topic", groupId = "post-event-group")
    public void processPostCreatedEvent(String payload) {
        int attempts = 0;
        int maxAttempts = 1; // Số lần retry tối đa
        long delay = 2000; // Thời gian delay giữa các lần retry

        while (attempts < maxAttempts) {
            try {
                attempts++;
                PostCreatedEvent event = objectMapper.readValue(payload, PostCreatedEvent.class);
                log.info("Received PostCreatedEvent: {}", event);

                // Save the post to the database
                Post post = new Post();
                post.setPostId(event.getPostId());
                post.setUserId(event.getUserId());
                post.setTitle(event.getTitle());
                post.setCreatedDate(event.getCreatedDate());
                post.setNumberOfLike(event.getNumberOfLike());
                post.setStatus(PostStatus.PENDING);

                repository.save(post);
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
            kafkaTemplate.send("post-dlq-topic", payload);

            // Gửi rollback event
            PostCreatedEvent event = objectMapper.readValue(payload, PostCreatedEvent.class);
            RollbackPostEvent rollbackEvent = new RollbackPostEvent(event.getPostId());
            String rollbackEventPayload = objectMapper.writeValueAsString(rollbackEvent);
            kafkaTemplate.send("post-rollback-topic", rollbackEventPayload);

        } catch (Exception ex) {
            log.error("Error when handling failure: ", ex);
        }
    }
}