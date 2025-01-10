package com.namdam1123.j2ee.postservicecommand.Workers;

import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    private KafkaTemplate<String, Object> kafkaTemplate;

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

    public OutboxEvent processAndSendAverageEvent() {
        List<OutboxEvent> events = getAndClearEvents();
        double sum = 0;
        int count = 0;

        Pattern pattern = Pattern.compile("\"title\":\"test\\s+(\\d+)\"");
        for (OutboxEvent event : events) {
            Matcher matcher = pattern.matcher(event.getPayload());
            while (matcher.find()) {
                sum += Integer.parseInt(matcher.group(1));
                count++;
            }
        }

        if (count > 0) {
            double average = sum / count;
            OutboxEvent avgEvent = events.get(0);
            String updatedPayload = avgEvent.getPayload().replaceAll("test\\s+\\d+", "title avg" + average);
            avgEvent.setPayload(updatedPayload);
            return avgEvent;
        }
        return null;
    }
} 