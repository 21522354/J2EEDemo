package com.namdam1123.j2ee.postservicecommand.Entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PostStatistic {
    public List<UUID> PostId;
    public int AverageLike;
    public LocalDateTime StartTime;
    public LocalDateTime EndTime;

    public List<UUID> getPostId() {
        return PostId;
    }

    public void setPostId(List<UUID> postId) {
        PostId = postId;
    }

    public int getAverageLike() {
        return AverageLike;
    }

    public void setAverageLike(int averageLike) {
        AverageLike = averageLike;
    }

    public LocalDateTime getStartTime() {
        return StartTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        StartTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return EndTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        EndTime = endTime;
    }

    @Autowired
    private ObjectMapper objectMapper;

    public String getPayload() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
