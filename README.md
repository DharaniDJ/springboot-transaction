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

[Propagation Levels Detailed Use cases](./Propagation.md)

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
