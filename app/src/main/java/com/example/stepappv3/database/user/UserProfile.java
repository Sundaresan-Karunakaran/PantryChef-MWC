package com.example.stepappv3.database.user;

public class UserProfile {
    // Bu ID, Google hesabının ID'si olacak
    public String userId;

    public String name;
    public String surname;
    public int age;
    public double weight;
    public double height;

    public String allergies; // "Gluten, Nuts" gibi
    public String dietType;  // "Vegan"
    public String goal;      // "Lose Weight"

    // 1. ZORUNLU: Firebase için Boş Constructor
    public UserProfile() {
    }

    // 2. Bizim veri oluştururken kullanacağımız Constructor
    public UserProfile(String userId) {
        this.userId = userId;
    }
}