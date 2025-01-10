package com.namdam1123.j2ee.postservicequerry.Repository;

import com.namdam1123.j2ee.postservicequerry.Entities.PostStatistic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostStatisticRepository extends JpaRepository<PostStatistic, UUID> {
}
