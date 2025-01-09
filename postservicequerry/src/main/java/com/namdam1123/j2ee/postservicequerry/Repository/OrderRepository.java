package com.namdam1123.j2ee.postservicequerry.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.namdam1123.j2ee.postservicequerry.Entities.Order;


public interface OrderRepository extends JpaRepository<Order, String> {
}