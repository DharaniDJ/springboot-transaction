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

