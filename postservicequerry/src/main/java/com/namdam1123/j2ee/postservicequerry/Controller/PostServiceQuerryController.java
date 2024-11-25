package com.namdam1123.j2ee.postservicequerry.Controller;

import com.namdam1123.j2ee.postservicequerry.Dto.PostEvent;
import com.namdam1123.j2ee.postservicequerry.Entities.Post;
import com.namdam1123.j2ee.postservicequerry.Repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/posts")
public class PostServiceQuerryController {
    private static final Logger log = LoggerFactory.getLogger(PostServiceQuerryController.class);
    @Autowired
    PostRepository repository;

    @GetMapping
    List<Post> getAllPost(){
        return repository.findAll();
    }

    @KafkaListener(topics = "Post-event-topic", groupId = "post-event-group")
    public void processPostEvent(PostEvent event){
        log.info(event.getPost().toString());
        if (event.getEventType().equals("Create Post")) {
            repository.save(event.getPost());
        }
    }
}