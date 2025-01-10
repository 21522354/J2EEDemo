package com.namdam1123.j2ee.postservicecommand.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicecommand.Entities.PostStatistic;

@Component
public class PostStatisticSerializer {
    private final ObjectMapper objectMapper;

    @Autowired
    public PostStatisticSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(PostStatistic postStatistic) {
        try {
            return objectMapper.writeValueAsString(postStatistic);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
