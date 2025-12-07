package com.example.stepappv3.database.pantry;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.NonNull;

@Entity(tableName = "pantry_items",indices = {@Index(value = "userId")})
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category; // e.g., "Dairy & Eggs", "Produce"
    public int quantity;
    public String unit; // e.g., "pcs", "grams", "liters"
    @NonNull
    public String userId;

    // Room needs a public constructor
    public PantryItem(String name, String category, int quantity, String unit, @NonNull String userId) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.userId = userId;


    }


    @Ignore
    public PantryItem(){

    }
}