package com.namdam1123.j2ee.postservicecommand.Controllers;

import com.namdam1123.j2ee.postservicecommand.Dto.PostEvent;
import com.namdam1123.j2ee.postservicecommand.Entities.Post;
import com.namdam1123.j2ee.postservicecommand.Repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/posts")
public class PostServiceCommandController {
    @Autowired
    private PostRepository repository;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/createPost")
    ResponseEntity<Post> createPost(@RequestBody Post post) {
        post.setPostId(UUID.randomUUID());
        repository.save(post);
        PostEvent event = new PostEvent("Create Post", post);

        // Gửi event với đúng key (nếu cần) và event object
        kafkaTemplate.send("Post-event-topic", event);

        return ResponseEntity.status(HttpStatus.OK).body(post);
    }
    @PutMapping("/updatePost/{postId}")
    ResponseEntity<Post> updatePost(@RequestBody Post post, @PathVariable UUID postId){
        Optional<Post> existPost = repository.findById(postId);
        if(existPost.isPresent()){
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
    ResponseEntity<String> deletePost(@PathVariable UUID postId){
        Optional<Post> existPost = repository.findById(postId);
        if(existPost.isPresent()){
            repository.delete(existPost.get());
            return ResponseEntity.status(HttpStatus.OK).body("Delete post successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
