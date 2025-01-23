package com.example.springboot_transaction.handler;

import org.springframework.stereotype.Service;

import com.example.springboot_transaction.entity.Order;
import com.example.springboot_transaction.repository.OrderRepository;

@Service
public class OrderHandler {
    private final OrderRepository orderRepository;

    public OrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
