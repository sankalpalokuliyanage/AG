package com.example.agmart.models;

public class Product {
    public String id;
    public String name;
    public String barcode;
    public double price;
    public int stockQty;

    // For billing only
    public int quantity;

    public Product() {

    } // Required for Firebase

    public Product(String id, String name, String barcode, double price, int stockQty) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.price = price;
        this.stockQty = stockQty;

    }

    // New constructor for billing
    public Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
