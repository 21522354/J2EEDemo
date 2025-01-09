package com.namdam1123.j2ee.postservicecommand.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.namdam1123.j2ee.postservicecommand.Entities.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
}