package com.namdam1123.j2ee.postservicecommand.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.namdam1123.j2ee.postservicecommand.Entities.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Optional<Post> findByUserIdAndTitle(UUID userId, String title);
}
