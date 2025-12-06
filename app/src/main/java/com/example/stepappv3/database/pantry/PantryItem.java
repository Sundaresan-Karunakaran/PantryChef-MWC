package com.example.stepappv3.database.pantry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category; // e.g., "Dairy & Eggs", "Produce"
    public int quantity;
    public String unit; // e.g., "pcs", "grams", "liters"
// Store as a long (milliseconds since epoch)

    // Room needs a public constructor
    public PantryItem(String name, String category, int quantity, String unit) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
    }
}