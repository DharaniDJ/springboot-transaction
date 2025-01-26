
## Propagation Levels with Examples

In this section, we break down all transaction propagation levels with real-time examples and detailed use cases:

### 1. Propagation.REQUIRED
- **Definition**: Joins an existing transaction or creates a new one if none exists.
- **Use Case**: Most commonly used. Ensures a single transaction for the entire flow.
- **Example**: Placing an order where saving the order and updating inventory should happen in the same transaction.

**Scenario**: Suppose a user places an order for a product. The system needs to:
1. Save the order details in the `Order` table.
2. Deduct the quantity from the `Product` table.
If either step fails, the entire operation should roll back.

```java
@Transactional(propagation = Propagation.REQUIRED)
public void placeOrder(Order order) {
    saveOrder(order);  // joins the existing transaction
    updateInventory(order.getProductId(), order.getQuantity());
}
```

### 2. Propagation.REQUIRES_NEW
- **Definition**: Suspends the current transaction and creates a new one.
- **Use Case**: For auditing or logging irrespective of the success/failure of the main transaction.
- **Example**: Logging audit details even if the main order process fails.

**Scenario**: Consider an e-commerce system that logs order audit details regardless of whether the order process succeeds or fails. This ensures you can track issues without interfering with the main transaction.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logAudit(Order order) {
    auditRepository.save(new Audit(order));
}
```

### 3. Propagation.MANDATORY
- **Definition**: Requires an existing transaction; throws an exception if none exists.
- **Use Case**: Ensures certain operations are only executed within an active transaction.
- **Example**: Payment validation as part of the order process.

**Scenario**: Before finalizing an order, you validate the payment status. This operation must occur within an existing transaction to ensure consistency.

```java
@Transactional(propagation = Propagation.MANDATORY)
public void validatePayment(Order order) {
    if (!isPaymentValid(order)) {
        throw new RuntimeException("Payment validation failed");
    }
}
```

### 4. Propagation.NEVER
- **Definition**: Ensures no transaction is active; throws an exception if one exists.
- **Use Case**: For non-critical operations that should not be part of a transaction, e.g., sending notifications.

**Scenario**: After placing an order, you send a notification to the user. This operation should not depend on the transaction state to avoid duplicate notifications.

```java
@Transactional(propagation = Propagation.NEVER)
public void sendNotification(Order order) {
    notificationService.send(order);
}
```

### 5. Propagation.NOT_SUPPORTED
- **Definition**: Suspends any existing transaction and executes without a transaction.
- **Use Case**: For read-only operations or logging outside of a transaction.

**Scenario**: Fetch product recommendations based on a user’s recent purchase. This operation does not require a transaction.

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public List<Product> recommendProducts() {
    return productRepository.findRecommendations();
}
```

### 6. Propagation.SUPPORTS
- **Definition**: Joins an existing transaction if available; executes without one otherwise.
- **Use Case**: For optional transactional contexts.

**Scenario**: Fetch customer details. If a transaction exists, the method participates; otherwise, it runs without a transaction.

```java
@Transactional(propagation = Propagation.SUPPORTS)
public Customer getCustomerDetails(Long customerId) {
    return customerRepository.findById(customerId).orElse(null);
}
```

### 7. Propagation.NESTED
- **Definition**: Executes within a nested transaction; allows rollbacks of the nested transaction without affecting the outer transaction.
- **Note**: Requires specific database support; not fully supported by JPA.

**Scenario**: Validate a sub-operation within a larger transaction, such as payment validation within order processing. If the validation fails, only the nested transaction rolls back.

```java
@Transactional(propagation = Propagation.NESTED)
public void validateNestedTransaction(Order order) {
    // Nested transaction logic
}
```