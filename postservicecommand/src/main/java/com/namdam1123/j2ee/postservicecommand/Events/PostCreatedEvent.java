package com.namdam1123.j2ee.postservicecommand.Events;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import com.namdam1123.j2ee.postservicecommand.Entities.Post;

public class PostCreatedEvent {
    private UUID postId;
    private UUID userId;
    private String title;
    private int numberOfLike;
    private LocalDateTime createdDate;

    public PostCreatedEvent(Post post) {
        this.postId = post.getPostId();
        this.userId = post.getUserId();
        this.title = post.getTitle();
        this.numberOfLike = post.getNumberOfLike();
        this.createdDate = post.getCreatedDate();
    }

    // Getters and Setters
    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumberOfLike() {
        return numberOfLike;
    }

    public void setNumberOfLike(int numberOfLike) {
        this.numberOfLike = numberOfLike;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
