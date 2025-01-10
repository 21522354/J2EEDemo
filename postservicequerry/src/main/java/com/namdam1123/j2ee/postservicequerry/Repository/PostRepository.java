package com.namdam1123.j2ee.postservicequerry.Repository;

import com.namdam1123.j2ee.postservicequerry.Entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUserId(UUID userId);
}
