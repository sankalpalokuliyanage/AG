package com.example.agmart.models;

import java.util.List;

public class BillRecord {
    public String customerName;
    public String customerPhone;
    public int totalAmount;
    public boolean paid;
    public String pdfPath;
    public List<Product> items;

    public BillRecord() {}

    public BillRecord(String name, String phone, int total, boolean paid, String pdfPath, List<Product> items) {
        this.customerName = name;
        this.customerPhone = phone;
        this.totalAmount = total;
        this.paid = paid;
        this.pdfPath = pdfPath;
        this.items = items;
    }
}
