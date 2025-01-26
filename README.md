# Spring Boot Transaction Management

Transactions are the backbone of any enterprise application. Whether you are processing payments, updating inventory, or managing user details, ensuring consistency is critical.

In real-world applications, it's common to execute multiple database operations within a single method or use case. But what happens when something goes wrong? How do we ensure that a single failure does not break the entire system? That’s where **transaction management** comes into the picture.

In this project, we’ll dive deep into **Spring Transaction Management**—exploring how it works, why it’s essential, and how to handle **different isolation levels** and **propagation behaviors** with hands-on examples.

---

## Table of Contents

1. [Introduction](#introduction)
2. [Real-Time Use Case](#real-time-use-case)
3. [Problem Without Transactions](#problem-without-transactions)
4. [How Transactions Solve the Problem](#how-transactions-solve-the-problem)
5. [Implementation Steps](#implementation-steps)
6. [Configuration](#configuration)
7. [Testing the Flow](#testing-the-flow)
8. [Placeholders for Images](#placeholders-for-images)
9. [Conclusion](#conclusion)

---

## Introduction

Before we jump directly into the code, let’s understand **what a transaction is** and **why we need it** in real-time applications. 

**Key takeaway**: 
- If multiple steps are performed in a single flow (e.g., saving to one database table, updating another table, and processing payment), a failure in any step should roll back **all** changes to maintain data consistency.
- If all steps succeed, the changes are committed.

---

## Real-Time Use Case

Imagine you’re working on an **e-commerce** application with the following services:

1. **Inventory Service**: 
   - Contains a `Product` table with:
     - `id` (product ID)
     - `name` (product name, e.g., “Laptop”)
     - `price` (price of each product)
     - `stock` (available stock)

2. **Order Service**:
   - Contains an `Order` table with:
     - `order_id`
     - `product_id`
     - `quantity`
     - `total_price`

**Use Case**: User wants to place an order for a laptop (product ID = 1) with a quantity of 2.
Steps involved:
1. **Save the order** in the `Order` DB (e.g., order ID = 101, product ID = 1, quantity = 2, total amount = price * quantity).
2. **Update the stock** in the `Inventory` DB (e.g., reduce stock by 2 if originally there were 10 items in stock).

If any step fails (network glitch, DB connection issue, etc.), we don’t want partial data to remain (like an order that’s been saved but stock not updated).

---

## Problem Without Transactions

If we do **not** use transactions, here’s what can happen:

1. User places an order for 5 laptops.
2. The **order table** is updated successfully (order is saved).
3. When updating the **inventory table**, a failure occurs (exception, timeout, etc.).
4. The **new order record** now exists, but **stock remains unchanged** in inventory.
5. The system data becomes **inconsistent**. Future operations might incorrectly report more stock than is actually available, leading to overselling.

This inconsistency can damage user trust and the reliability of your application.

---

## How Transactions Solve the Problem

To avoid the above scenario, we use **transactions**. In Spring, we can use the `@Transactional` annotation:

1. **Place the annotation** on the method that performs multiple DB operations (e.g., in a service class).
2. If **any** step fails, the entire set of operations **rolls back**—so no partial changes are saved.
3. If **all** steps succeed, the changes are **committed** to the database.

---

## Implementation Steps

Below is a high-level outline:

1. **Create Entities**:
   - `Order` entity with fields `id`, `productId`, `quantity`, `totalPrice`.
   - `Product` entity with fields `id`, `name`, `price`, `stock`.
2. **Create Repositories**:
   - `OrderRepository`
   - `ProductRepository`
3. **Create Handlers**:
   - `OrderHandler` (for saving the `Order` record)
   - `InventoryHandler` (for retrieving & updating the `Product` record)
4. **Create a Service** (`OrderProcessingService`) to:
   1. Fetch the product from inventory.
   2. Validate stock availability.
   3. Calculate `totalPrice`.
   4. Save the `Order`.
   5. Update the `stock` in `Inventory`.
5. **Annotate** the service method with `@Transactional`.

---

## Configuration

docker-compose.yml:

```yml
version: "3.8"

services:
  db:
    image: mysql:latest
    container_name: my-mysql
    restart: always
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=springboot-transaction
      # - MYSQL_USER=my_user
      # - MYSQL_PASSWORD=my_user_password
    volumes:
      - ./mysql-data:/var/lib/mysql
```

Run:
```
docker compose up -d
```

connect using docker exec:
```
docker exec -it my-mysql mysql -u root -p
```

Create database:
```
CREATE DATABASE `springboot-transaction`;
```

And in `application.properties`, you have:

```properties
spring.application.name=springboot-transaction

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url = jdbc:mysql://localhost:3306/springboot-transaction
spring.datasource.username = root
spring.datasource.password = password
spring.jpa.show-sql = true
spring.jpa.hibernate.ddl-auto = update
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQLDialect
server.port=9191
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

```

---

## Testing the Flow

1. **Start the application** and ensure tables are created or updated.
2. Insert a few `Product` records (e.g., via direct database insert or a POST endpoint).
3. Use **Swagger** or **Postman** to send an **Order** request:
   - If everything is correct, it should insert into the `Order` table and update the `Product` stock.
   - If an error is intentionally triggered (e.g., throwing an exception in `InventoryHandler`), the entire transaction will roll back.

### Happy Path

- Place an order that is within the available stock.
- Check both the `Order` and `Product` tables to confirm:
  - `Order` is inserted with the correct `totalPrice`.
  - `Product.stock` is decremented.

### Failure Path

- Force an exception in the inventory update step (simulate DB issue).
- Observe that **no record** is added to the `Order` table (rollback).
- Observe that `Product.stock` remains unchanged.

---

## **Swagger UI Screenshots**
   ![Swagger UI Request body](https://github.com/user-attachments/assets/4a3d0f04-50e5-433b-bd75-2f7915d18ddc)
   ![Swagger UI Response body](https://github.com/user-attachments/assets/a90a86f7-9f60-4209-b10f-1b5cdba4d5a6)

---


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

---

## Conclusion

Spring Boot Transaction Management helps maintain **data consistency** and **integrity** across multiple database operations.

By leveraging the `@Transactional` annotation, you ensure that:

- **All** required changes either **commit** if successful.
- **All** changes are **rolled back** if any step fails.

By leveraging different propagation types, you can tailor transaction behavior to fit specific use cases. This project explored:

- Real-time use cases.
- Different propagation types with examples.
- Testing & validation methods.
