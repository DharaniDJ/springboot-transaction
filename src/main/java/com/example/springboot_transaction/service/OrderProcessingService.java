package com.example.springboot_transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_transaction.entity.Order;
import com.example.springboot_transaction.entity.Product;
import com.example.springboot_transaction.handler.AuditLogHandler;
import com.example.springboot_transaction.handler.InventoryHandler;
import com.example.springboot_transaction.handler.NotificationHandler;
import com.example.springboot_transaction.handler.OrderHandler;
import com.example.springboot_transaction.handler.PaymentValidatorHandler;
import com.example.springboot_transaction.handler.ProductRecommendationHandler;


@Service
public class OrderProcessingService {
    private final OrderHandler orderHandler;
    private final InventoryHandler inventoryHandler;
    private final AuditLogHandler auditLogHandler;
    private final PaymentValidatorHandler paymentValidatorHandler;
    private final NotificationHandler notificationHandler;
    private final ProductRecommendationHandler productRecommendationHandler;

    public OrderProcessingService(OrderHandler orderHandler, InventoryHandler inventoryHandler, AuditLogHandler auditLogHandler, PaymentValidatorHandler paymentValidatorHandler, NotificationHandler notificationHandler, ProductRecommendationHandler productRecommendationHandler) {
        this.orderHandler = orderHandler;
        this.inventoryHandler = inventoryHandler;
        this.auditLogHandler = auditLogHandler;
        this.paymentValidatorHandler = paymentValidatorHandler;
        this.notificationHandler = notificationHandler;
        this.productRecommendationHandler = productRecommendationHandler;
    }

    // Propagation.REQUIRED : join the existing transaction if exists, otherwise create a new transaction
    // Propagation.REQUIRES_NEW : create a new transaction, suspend the existing transaction if exists
    // Propagation.MANDATORY : join the existing transaction if exists, otherwise throw an exception
    // Propagation.NEVER : do not join the existing transaction, throw an exception if exists
    // Propagation.NOT_SUPPORTED : do not join the existing transaction, suspend the existing transaction if exists
    // Propagation.SUPPORTS : join the existing transaction if exists, otherwise execute non-transactionally
    // Propagation.NESTED : create a nested transaction if exists, otherwise create a new transaction
    
    // Outer txn
    @Transactional(propagation = Propagation.REQUIRED)
    public Order placeAnOrder(Order order) {
        //  get product inventory
        Product product = inventoryHandler.getProductById(order.getProductId());
        // validate stock availability < (5)
        validateStockAvailability(order, product);
        // update total price in order entity
        order.setTotalPrice(product.getPrice() * order.getQuantity());

        Order saveOrder = null;
        try{
            // save order
            saveOrder = orderHandler.saveOrder(order);

            // update stock in inventory
            updateInventory(saveOrder, product);

            // required_new - create a new transaction, suspend the existing transaction if exists
            // auditOrderDetails(order);
            auditLogHandler.logAuditDetails(order, "Order placed");

        }catch (Exception e){
            auditLogHandler.logAuditDetails(order,"Order placement failed");
        }

        // // I want to execute this without a transaction
        // // lets say, save order is failed or update inventory is failed, we do retry mechanism
        // // for each retry, we dont want to send the order confirmation. so we can use REQUIRES_NEVER
        // notificationHandler.sendOrderConfirmationNotification(order);

        // // I want to use the existing transaction
        // // Exception in payment validation will rollback the transaction, but it will not rollback the order placement
        // paymentValidatorHandler.validatePayment(order);

        // Suspends the existing transaction and fetches the product recommendations
        // Resumes the existing transaction after fetching the recommendations
        productRecommendationHandler.getRecommendations();
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

    @Transactional(propagation = Propagation.SUPPORTS)
    public void getCustomerDetails() {
        System.out.println("Customer details fetched !!!!!");
    }
}
