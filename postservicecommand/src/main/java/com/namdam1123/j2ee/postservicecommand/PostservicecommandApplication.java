package com.namdam1123.j2ee.postservicecommand;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PostservicecommandApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostservicecommandApplication.class, args);
	}
	@Bean
	NewTopic postTopic() {
		return new NewTopic("Post-event-topic", 2, (short) 1);
	}
}
