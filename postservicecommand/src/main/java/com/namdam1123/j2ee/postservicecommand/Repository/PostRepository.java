package com.namdam1123.j2ee.postservicecommand.Repository;

import com.namdam1123.j2ee.postservicecommand.Entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
