package com.namdam1123.j2ee.postservicequerry.Repository;

import com.namdam1123.j2ee.postservicequerry.Entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
