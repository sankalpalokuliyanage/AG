package com.example.agmart.models;

import java.util.List;

public class BillRecord {
    public String customerName;
    public String customerPhone;
    public int totalAmount;
    public boolean paid;
    public String pdfPath;
    public List<Product> items;
    public String date;  // e.g., "2025-08-17"
    public String time;  // e.g., "16:30"

    public BillRecord() {}

    public BillRecord(String name, String phone, int total, boolean paid, String pdfPath, List<Product> items, String date, String time) {
        this.customerName = name;
        this.customerPhone = phone;
        this.totalAmount = total;
        this.paid = paid;
        this.pdfPath = pdfPath;
        this.items = items;
        this.date = date;
        this.time = time;
    }
}
