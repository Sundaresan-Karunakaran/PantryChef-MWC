package com.example.stepappv3.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_steps")
public class Step {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timestamp;
    private int steps;

    public Step(long timestamp,int steps){
        this.timestamp = timestamp;
        this.steps = steps;
    }

    public int getId(){return id;}
    public void setId(int id){ this.id = id;}
    public long getTimestamp(){ return timestamp;}
    public void setTimestamp(long timestamp){ this.timestamp = timestamp;}
    public int getSteps(){ return steps;}


}
