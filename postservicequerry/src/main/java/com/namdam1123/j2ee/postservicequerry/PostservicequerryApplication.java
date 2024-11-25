package com.namdam1123.j2ee.postservicequerry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.JsonMessageConverter;

@SpringBootApplication
public class PostservicequerryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostservicequerryApplication.class, args);
	}


	@Bean
	JsonMessageConverter converter(){
		return new JsonMessageConverter();
	}
	
}
