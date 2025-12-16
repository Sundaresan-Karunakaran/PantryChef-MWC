package com.example.stepappv3.database.pantry;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// DÜZELTME BURADA: org.jspecify yerine androidx.annotation kullanmalısınız.
// Room, NOT NULL kısıtlamasını bu paketle tanır.
import androidx.annotation.NonNull;

@Entity(tableName = "pantry_items", indices = {@Index(value = "userId")})
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category;
    public int quantity;
    public String unit;

    @NonNull
    public String userId;

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