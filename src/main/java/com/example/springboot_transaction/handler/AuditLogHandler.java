package com.example.springboot_transaction.handler;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_transaction.entity.AuditLog;
import com.example.springboot_transaction.entity.Order;
import com.example.springboot_transaction.repository.AuditLogRepository;

@Component
public class AuditLogHandler {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Log audit details (runs in an independent transaction)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuditDetails(Order order, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOrderId(Long.valueOf(order.getId()));
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());

        // Save the audit log
        auditLogRepository.save(auditLog);
    }
}