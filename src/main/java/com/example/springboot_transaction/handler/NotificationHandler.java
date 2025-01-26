package com.example.springboot_transaction.handler;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_transaction.entity.Order;

@Service
public class NotificationHandler {

    @Transactional(propagation = Propagation.NEVER)
    public void sendOrderConfirmationNotification(Order order) {
        // Send an email notification to the customer
        System.out.println( order.getId()+" Order placed successfully");
    }
}