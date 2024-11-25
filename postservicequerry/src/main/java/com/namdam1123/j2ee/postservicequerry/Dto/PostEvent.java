package com.namdam1123.j2ee.postservicequerry.Dto;

import com.namdam1123.j2ee.postservicequerry.Entities.Post;
import lombok.Data;

@Data
public class PostEvent {
    private String EventType;
    private Post post;

    public PostEvent(){}

    public PostEvent(String eventType, Post post) {
        EventType = eventType;
        this.post = post;
    }

    public String getEventType() {
        return EventType;
    }

    public void setEventType(String eventType) {
        EventType = eventType;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
