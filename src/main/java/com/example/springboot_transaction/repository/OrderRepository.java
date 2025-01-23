package com.example.springboot_transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springboot_transaction.entity.Order;
public interface OrderRepository extends JpaRepository<Order, Integer> {

}
