package com.namdam1123.j2ee.postservicecommand.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.namdam1123.j2ee.postservicecommand.Entities.PostStatistic;

import java.util.UUID;

public interface PostStatisticRepository extends JpaRepository<PostStatistic, UUID> {
}
