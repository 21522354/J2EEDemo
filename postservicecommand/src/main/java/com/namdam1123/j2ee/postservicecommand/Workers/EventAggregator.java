package com.namdam1123.j2ee.postservicecommand.Workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;
import com.namdam1123.j2ee.postservicecommand.Entities.Post;
import com.namdam1123.j2ee.postservicecommand.Entities.PostStatistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EventAggregator {

    private final List<OutboxEvent> eventBuffer = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private static final Logger logger = LoggerFactory.getLogger(OutboxWorker.class);

    @Autowired
    private ObjectMapper objectMapper;

    public void addEvent(OutboxEvent event) {
        lock.lock();
        try {
            eventBuffer.add(event);
        } finally {
            lock.unlock();
        }
    }

    public List<OutboxEvent> getAndClearEvents() {
        lock.lock();
        try {
            List<OutboxEvent> eventsToProcess = new ArrayList<>(eventBuffer);
            eventBuffer.clear();
            return eventsToProcess;
        } finally {
            lock.unlock();
        }
    }

    public PostStatistic processAndSendAverageEvent() {
        List<OutboxEvent> events = getAndClearEvents();
        double sum = 0;
        int count = 0;

        for (OutboxEvent event : events) {
            try {
                Post post = objectMapper.readValue(event.getPayload(), Post.class);
                sum += post.getNumberOfLike();
                count++;
            } catch (Exception e) {
                logger.error("Error when processing event: ", e);
            }
        }

        if (count > 0) {
            double average = sum / count;
            PostStatistic postStatistic = new PostStatistic();
            List<UUID> postIds = new ArrayList<>();
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;

            for (OutboxEvent event : events) {
                try {
                    Post post = objectMapper.readValue(event.getPayload(), Post.class);
                    postIds.add(post.getPostId());

                    // Track earliest and latest create times
                    if (startTime == null || post.getCreatedDate().isBefore(startTime)) {
                        startTime = post.getCreatedDate();
                    }
                    if (endTime == null || post.getCreatedDate().isAfter(endTime)) {
                        endTime = post.getCreatedDate();
                    }
                } catch (Exception e) {
                    logger.error("Error getting post ID from event: ", e);
                }
            }
            postStatistic.setPostIds(postIds);
            postStatistic.setAverageLike((int) average);
            postStatistic.setStartTime(startTime);
            postStatistic.setEndTime(endTime);

            return postStatistic;
        }
        return new PostStatistic();
    }
}