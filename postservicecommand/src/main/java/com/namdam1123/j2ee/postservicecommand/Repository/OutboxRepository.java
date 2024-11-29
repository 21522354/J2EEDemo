package com.namdam1123.j2ee.postservicecommand.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.namdam1123.j2ee.postservicecommand.Entities.OutboxEvent;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
}