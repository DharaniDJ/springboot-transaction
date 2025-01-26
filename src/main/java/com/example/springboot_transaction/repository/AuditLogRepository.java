package com.example.springboot_transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springboot_transaction.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

}