package com.namdam1123.j2ee.postservicequerry.Entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_statistic")
public class PostStatistic {
    @Id
    public UUID PostStatisticId;
    public List<UUID> PostId;
    public int AverageLike;
    public LocalDateTime StartTime;
    public LocalDateTime EndTime;

    @JsonCreator
    public PostStatistic(
            @JsonProperty("postStatisticId") UUID postStatisticId,
            @JsonProperty("postId") List<UUID> postId,
            @JsonProperty("averageLike") int averageLike,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime) {
        this.PostStatisticId = postStatisticId;
        this.PostId = postId;
        this.AverageLike = averageLike;
        this.StartTime = startTime;
        this.EndTime = endTime;
    }

    public PostStatistic() {
    }

    public UUID getPostStatisticId() {
        return PostStatisticId;
    }

    public void setPostStatisticId(UUID postStatisticId) {
        this.PostStatisticId = postStatisticId;
    }

    public List<UUID> getPostIds() {
        return PostId;
    }

    public void setPostIds(List<UUID> postId) {
        this.PostId = postId;
    }

    public int getAverageLike() {
        return AverageLike;
    }

    public void setAverageLike(int averageLike) {
        this.AverageLike = averageLike;
    }

    public LocalDateTime getStartTime() {
        return StartTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.StartTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return EndTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.EndTime = endTime;
    }

}
