package com.example.stepappv3.database.ingredient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "master_ingredient", indices = {@Index(value = {"name"}, unique = true)})
public class MasterIngredient {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String name;
    @Nullable
    public String synonyms;

    public MasterIngredient(String name){
        this.name = name;
    }
}
