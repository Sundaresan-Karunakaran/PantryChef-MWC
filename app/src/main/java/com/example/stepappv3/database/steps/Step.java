package com.example.stepappv3.database.steps;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.NonNull;

@Entity(tableName = "daily_steps",indices = {@Index(value = "userId")})
public class Step {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timestamp;
    private int steps;
    @NonNull
    public String userId;


    public Step(long timestamp,int steps,@NonNull String userId){
        this.timestamp = timestamp;
        this.steps = steps;
        this.userId = userId;

    }

    public int getId(){return id;}
    public void setId(int id){ this.id = id;}
    public long getTimestamp(){ return timestamp;}
    public void setTimestamp(long timestamp){ this.timestamp = timestamp;}
    public int getSteps(){ return steps;}


}
