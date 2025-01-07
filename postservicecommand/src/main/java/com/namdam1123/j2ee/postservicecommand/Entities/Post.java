package com.namdam1123.j2ee.postservicecommand.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import com.namdam1123.j2ee.postservicecommand.Dto.PostStatus;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Post {
    @Id
    private UUID PostId;
    private UUID UserId;
    private String Title;
    private LocalDateTime CreatedDate;
    private int NumberOfLike;
    private PostStatus status;

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public UUID getPostId() {
        return PostId;
    }

    public void setPostId(UUID postId) {
        PostId = postId;
    }

    public UUID getUserId() {
        return UserId;
    }

    public void setUserId(UUID userId) {
        UserId = userId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public LocalDateTime getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        CreatedDate = createdDate;
    }

    public int getNumberOfLike() {
        return NumberOfLike;
    }

    public void setNumberOfLike(int numberOfLike) {
        NumberOfLike = numberOfLike;
    }
}
