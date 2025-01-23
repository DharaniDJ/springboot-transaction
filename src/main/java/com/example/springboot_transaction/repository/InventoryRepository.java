package com.example.springboot_transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springboot_transaction.entity.Product;

public interface InventoryRepository extends JpaRepository<Product, Integer> {

}
