package com.example.agmart.models;

import java.util.List;

public class BillRecord {
    public String customerName;
    public String customerPhone;
    public boolean paid;
    public String pdfPath;
    public long timestamp;

    public List<Product> items;

    public BillRecord() {} // Required empty constructor

    public BillRecord(String customerName, String customerPhone, boolean paid, String pdfPath, List<Product> items) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.paid = paid;
        this.pdfPath = pdfPath;
        this.timestamp = System.currentTimeMillis();
        this.items = items;
    }
}

