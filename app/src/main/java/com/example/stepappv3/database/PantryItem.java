package com.example.stepappv3.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // Ürünün adı (örn: "Milk", "Eggs")
    private String name;

    // Miktar (örn: 2, 500, 1.5)
    private double quantity;

    // Birim: "piece", "g", "ml" gibi String tutacağız (enum yerine basit olsun diye)
    private String unit;

    // Bu ürün taranan bir reçeteden mi geldi (true) yoksa elle mi eklendi (false)
    private boolean fromRecipe;

    public PantryItem(String name, double quantity, String unit, boolean fromRecipe) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.fromRecipe = fromRecipe;
    }

    // --- GETTER / SETTER ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isFromRecipe() {
        return fromRecipe;
    }

    public void setFromRecipe(boolean fromRecipe) {
        this.fromRecipe = fromRecipe;
    }
}
