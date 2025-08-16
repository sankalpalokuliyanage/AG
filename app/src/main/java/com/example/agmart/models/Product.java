package com.example.agmart.models;

public class Product {
    public String id;
    public String name;
    public double price;
    public int stockQty;

    // For billing only
    public int quantity;

    public Product() {
        // Required for Firebase
    }

    // Constructor for product management
    public Product(String id, String name, double price, int stockQty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQty = stockQty;
    }

    // Constructor for billing
    public Product(String id, String name, double price, int quantity, boolean isBilling) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
