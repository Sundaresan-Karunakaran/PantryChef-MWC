package com.example.stepappv3.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Step.class}, version = 1, exportSchema = false)
public abstract class StepDatabase extends RoomDatabase {
    public abstract StepDao stepDao();

    private static volatile StepDatabase instance;
    private static final int NUM_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUM_THREADS);

    public static StepDatabase getDatabase(final Context context) {
        if(instance == null){
            synchronized (StepDatabase.class){
                instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        StepDatabase.class,
                        "step_database")
                        .build();

            }
        }
        return instance;
    }

}
