package com.example.stepappv3.database.pantry;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.stepappv3.database.ingredient.MasterIngredient;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Entity(foreignKeys = @ForeignKey(entity = MasterIngredient.class,   parentColumns = "id",
        childColumns = "masterIngredientId",
        onDelete = ForeignKey.CASCADE),
        tableName = "pantry_items",
        indices = {@Index(value = "userId"), @Index("masterIngredientId")})
public class PantryItem {


    @PrimaryKey
    public int masterIngredientId;
    public String category;
    public int quantity;
    public String unit;
    @NonNull
    public String userId;

    // Room needs a public constructor
    public PantryItem(int masterIngredientId,String category, int quantity, String unit, @NonNull String userId) {
        this.masterIngredientId = masterIngredientId;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.userId = userId;


    }
    @Ignore
    public PantryItem(){
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PantryItem that = (PantryItem) o;
        return masterIngredientId == that.masterIngredientId && quantity == that.quantity && Objects.equals(category, that.category) && Objects.equals(unit, that.unit) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash( masterIngredientId, category, quantity, unit, userId);
    }
}