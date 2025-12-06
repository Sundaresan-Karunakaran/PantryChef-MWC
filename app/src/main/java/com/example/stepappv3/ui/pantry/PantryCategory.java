package com.example.stepappv3.ui.pantry;

public class PantryCategory {
    private final String name;
    private final int imageResId;

    public PantryCategory(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}