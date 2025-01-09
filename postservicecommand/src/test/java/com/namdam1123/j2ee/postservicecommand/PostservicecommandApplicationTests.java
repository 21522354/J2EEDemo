package com.namdam1123.j2ee.postservicecommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdam1123.j2ee.postservicecommand.Controllers.PostServiceCommandController;
import com.namdam1123.j2ee.postservicecommand.Dto.PostDTOs.CreatePostCommandDTO;
import com.namdam1123.j2ee.postservicecommand.Entities.Post;
import com.namdam1123.j2ee.postservicecommand.Entities.PostStatus;
import com.namdam1123.j2ee.postservicecommand.Repository.OutboxRepository;
import com.namdam1123.j2ee.postservicecommand.Repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PostservicecommandApplicationTests {

	@Mock
	private PostRepository postRepository;

	@Mock
	private OutboxRepository outboxRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private PostServiceCommandController postServiceCommandController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testCreatePostIdempotence() {
		// Given
		UUID userId = UUID.randomUUID();
		CreatePostCommandDTO postDTO = new CreatePostCommandDTO("Test Title", userId);
		Post existingPost = new Post();
		existingPost.setPostId(UUID.randomUUID());
		existingPost.setUserId(userId);
		existingPost.setTitle("Test Title");
		existingPost.setNumberOfLike(0);
		existingPost.setStatus(PostStatus.PENDING);

		// When
		when(postRepository.save(any(Post.class))).thenReturn(existingPost);
		ResponseEntity<Post> response1 = postServiceCommandController.createPost(postDTO);
		ResponseEntity<Post> response2 = postServiceCommandController.createPost(postDTO);

		// Then
		assertEquals(201, response1.getStatusCodeValue()); // First call should create the post
		assertEquals(201, response2.getStatusCodeValue()); // Second call should also return created
		verify(postRepository, times(1)).save(any(Post.class)); // Ensure save is called only once
	}
}