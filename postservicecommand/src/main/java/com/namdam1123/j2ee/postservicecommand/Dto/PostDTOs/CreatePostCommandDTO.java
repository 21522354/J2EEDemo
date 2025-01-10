package com.namdam1123.j2ee.postservicecommand.Dto.PostDTOs;

import java.util.UUID;

public class CreatePostCommandDTO {
    private String Title;
    private UUID UserId;
    private int NumberOfLike;

    public int getNumberOfLike() {
        return NumberOfLike;
    }

    public void setNumberOfLike(int numberOfLike) {
        NumberOfLike = numberOfLike;
    }

    public CreatePostCommandDTO() {
    }

    public CreatePostCommandDTO(String title, UUID userId) {
        this.Title = title;
        this.UserId = userId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public UUID getUserId() {
        return UserId;
    }

    public void setUserId(UUID userId) {
        this.UserId = userId;
    }
}
