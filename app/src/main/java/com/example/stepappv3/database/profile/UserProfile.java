package com.example.stepappv3.database.profile;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.NonNull;

@Entity(tableName = "user_profile",indices = {@Index(value = "userId")})
public class UserProfile {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;

    public String gender;
    public int age;
    public double weight;
    public double height;
    public double activityLevel;
    public double requiredCalories;
    public double requiredSugar;
    public double requiredFat;
    public double requiredSalt;
    public double requiredFruitVeg;
    public double bmr;




    public UserProfile(@NonNull String userId, String gender, int age, double weight, double height, double activityLevel,double requiredCalories,double requiredSugar,double requiredFat,double requiredSalt,double requiredFruitVeg,double bmr) {
        this.userId = userId;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.activityLevel = activityLevel;
        this.requiredCalories = requiredCalories;
        this.requiredSugar = requiredSugar;
        this.requiredFat = requiredFat;
        this.requiredSalt = requiredSalt;
        this.requiredFruitVeg = requiredFruitVeg;
        this.bmr = bmr;


    }

    @Ignore
    public UserProfile(){}
}
