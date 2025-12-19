package com.example.stepappv3.database.pantry;

public class ParsedReceiptItem {
    public String category;
    public String name;
    public int quantity;
    public String unit;

    public ParsedReceiptItem(String category, String name, int quantity, String unit) {
        this.category = category;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
}
