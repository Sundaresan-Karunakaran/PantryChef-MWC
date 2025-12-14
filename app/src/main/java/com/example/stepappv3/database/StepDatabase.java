package com.example.stepappv3.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;
// UserProfileDao importunu SİLİN

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// DÜZELTME 1: UserProfile.class buradan SİLİNDİ
@Database(entities = {Step.class, PantryItem.class}, version = 5, exportSchema = false)
public abstract class StepDatabase extends RoomDatabase {
    public abstract StepDao stepDao();
    public abstract PantryDao pantryDao();

    // DÜZELTME 2: Bu satırı SİLİN (Artık Dao yok)
    // public abstract UserProfileDao userProfileDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `pantry_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `category` TEXT, `quantity` INTEGER NOT NULL, `unit` TEXT, `expiryDate` INTEGER NOT NULL)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            try { database.execSQL("ALTER TABLE daily_steps ADD COLUMN userId TEXT NOT NULL DEFAULT ''"); } catch (Exception e) {}
            try { database.execSQL("ALTER TABLE pantry_items ADD COLUMN userId TEXT NOT NULL DEFAULT ''"); } catch (Exception e) {}
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_steps_userId` ON `daily_steps` (`userId`)");
        }
    };

    // UserProfile ile ilgili olan MIGRATION_3_4 ve MIGRATION_4_5 artık gereksiz,
    // ama kodun derlenmesi için boş bırakabilir veya silebilirsiniz.
    // En temizi veritabanını sıfırdan kurmaktır (Uygulamayı silip yüklemek).

    // Şimdilik hata vermemesi için bunları boş geçiyoruz veya kaldırıyoruz.
    // Eğer Migration hatası alırsanız uygulamayı emülatörden silip tekrar yükleyin.

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
                        // Migrationları burada tutabilirsiniz ama UserProfile tablosu artık kullanılmayacak
                        .fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build();
            }
        }
        return instance;
    }
}