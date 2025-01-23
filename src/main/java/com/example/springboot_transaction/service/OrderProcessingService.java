package com.example.springboot_transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_transaction.entity.Order;
import com.example.springboot_transaction.entity.Product;
import com.example.springboot_transaction.handler.InventoryHandler;
import com.example.springboot_transaction.handler.OrderHandler;


@Service
public class OrderProcessingService {
    private final OrderHandler orderHandler;
    private final InventoryHandler inventoryHandler;

    public OrderProcessingService(OrderHandler orderHandler, InventoryHandler inventoryHandler) {
        this.orderHandler = orderHandler;
        this.inventoryHandler = inventoryHandler;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Order placeAnOrder(Order order) {
        //  get product inventory
        Product product = inventoryHandler.getProductById(order.getProductId());
        // validate stock availability < (5)
        validateStockAvailability(order, product);
        // update total price in order entity
        order.setTotalPrice(product.getPrice() * order.getQuantity());
        // save order
        Order saveOrder = orderHandler.saveOrder(order);
        // update stock in inventory
        updateInventory(saveOrder, product);

        return saveOrder;
    }

    private void updateInventory(Order order, Product product) {
        int availableStock = product.getStockQuantity() - order.getQuantity();
        product.setStockQuantity(availableStock);
        inventoryHandler.updateProductDetails(product);
    }

    private static void validateStockAvailability(Order order, Product product) {
        if(order.getQuantity() > product.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
    }
}
