package com.namdam1123.j2ee.postservicequerry.Events.PostEvents;

import java.util.UUID;

public class RollbackPostEvent {
    private UUID postId;

    public RollbackPostEvent() {
    }

    public RollbackPostEvent(UUID postId) {
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "RollbackPostEvent{" +
                "postId='" + postId + '\'' +
                '}';
    }
}
