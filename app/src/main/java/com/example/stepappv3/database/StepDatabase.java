package com.example.stepappv3.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                Step.class,
                PantryItem.class      // YENİ: Pantry tablosu
        },
        version = 2,
        exportSchema = false
)
public abstract class StepDatabase extends RoomDatabase {

    public abstract StepDao stepDao();
    public abstract PantryDao pantryDao();   // YENİ

    private static volatile StepDatabase instance;
    private static final int NUM_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUM_THREADS);

    public static StepDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (StepDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    StepDatabase.class,
                                    "step_database"
                            )
                            // Şema değişince DB’yi sıfırla (ödev/proje için en pratik yol)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
