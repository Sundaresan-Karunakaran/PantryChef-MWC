package com.example.stepappv3.database.pantry;

import androidx.annotation.Nullable;

import java.util.Objects;

public class PantryItemDisplay {
    public String name;
    public String category;
    public int quantity;
    public String unit;
    public int masterIngredientId;

    public PantryItemDisplay(String name, String category, int quantity, String unit, int masterIngredientId) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.masterIngredientId = masterIngredientId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PantryItemDisplay that = (PantryItemDisplay) o;
        return quantity == that.quantity && masterIngredientId == that.masterIngredientId && Objects.equals(name, that.name) && Objects.equals(category, that.category) && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, quantity, unit, masterIngredientId);
    }
}
