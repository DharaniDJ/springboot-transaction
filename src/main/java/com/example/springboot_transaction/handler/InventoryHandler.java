package com.example.springboot_transaction.handler;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;

import com.example.springboot_transaction.entity.Product;
import com.example.springboot_transaction.repository.InventoryRepository;

@Service
public class InventoryHandler {
    private final InventoryRepository inventoryRepository;

    public InventoryHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Product updateProductDetails(Product product) {
        if(product.getPrice() > 5000){
            throw new RuntimeErrorException(null, "DB crashed.....");
        }

        return inventoryRepository.save(product);
    }

    public Product getProductById(int id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
