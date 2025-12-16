package com.example.stepappv3.database;

import static com.example.stepappv3.util.HelperFunctions.safeParseDouble;
import static com.example.stepappv3.util.HelperFunctions.safeParseInt;
import static com.example.stepappv3.util.HelperFunctions.unquoter;

import android.content.Context;
import android.util.Log;

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
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeDao;
// import com.example.stepappv3.util.DataLoadingManager; // Bu sınıf artık gerekli değil, çünkü onOpen metodu temizlendi.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Step.class, PantryItem.class, Recipe.class}, version = 6, exportSchema = false)
public abstract class StepDatabase extends RoomDatabase {

    public abstract StepDao stepDao();
    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();

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

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Recipe tablosu (Updated with correct column names)
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recipes` ( " +
                            "`recipeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT, `servingSize` TEXT, `servings` INTEGER NOT NULL, " +
                            "`steps` TEXT, `ingredients` TEXT, `missingNutrients` TEXT, " +
                            "`calories` REAL NOT NULL, `totalFat` REAL NOT NULL, " +
                            "`totalSugars` REAL NOT NULL, `dietaryFiber` REAL NOT NULL)"
            );
        }
    };

    private static RoomDatabase.Callback sRoomDatabaseCallback(final Context context) {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                databaseWriteExecutor.execute(() -> {
                    // onCreate sadece DB ilk oluşturulduğunda çalışır.
                    parseRecipesFromCsv(context, instance);
                    // DataLoadingManager kaldırıldı
                });
            }

            // Arkadaşınızın kodunda onOpen metodu yoktu.
            // Sizin DataLoadingManager kontrolünüz kaldırıldı.
            // onOpen'a ihtiyacınız varsa, buraya ekleyebilirsiniz, ancak
            // performansı artırmak için çıkarılmış varsayıyorum.
        };
    }

    // Arkadaşınızdan alınan daha güvenilir CSV ayrıştırma metodu.
    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes; // Tırnak içi/dışı durumunu değiştir
            } else if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken.setLength(0); // Bir sonraki token için sıfırla
            } else {
                currentToken.append(c);
            }
        }
        tokens.add(currentToken.toString()); // Son token'ı ekle
        return tokens;
    }

    private static void parseRecipesFromCsv(Context context, StepDatabase db) {
        if (db == null) return;
        RecipeDao dao = db.recipeDao();

        // --- BATCHING STRATEGY (Arkadaşınızdan alındı) ---
        final int BATCH_SIZE = 100;
        List<Recipe> recipeBatch = new ArrayList<>(BATCH_SIZE);
        long totalRecipesInserted = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("final_recipes.csv")))) {

            String line;
            boolean isFirstLine = true;
            Log.d("DatabaseSetup", "Starting to parse recipes from CSV in batches.");

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                List<String> tokens = parseCsvLine(line); // Arkadaşınızın ayrıştırma metodu

                if (tokens.size() >= 11) {
                    try {
                        Recipe recipe = new Recipe(
                                safeParseInt(unquoter(tokens.get(0))),
                                unquoter(tokens.get(1)),
                                unquoter(tokens.get(2)),
                                safeParseInt(unquoter(tokens.get(3))),
                                unquoter(tokens.get(4)),
                                unquoter(tokens.get(5)),
                                unquoter(tokens.get(6)),
                                safeParseDouble(unquoter(tokens.get(7))),
                                safeParseDouble(unquoter(tokens.get(8))),
                                safeParseDouble(unquoter(tokens.get(9))),
                                safeParseDouble(unquoter(tokens.get(10)))
                        );
                        recipeBatch.add(recipe);

                        // Batch dolduğunda, DB'ye toplu ekleme yap ve listeyi temizle.
                        if (recipeBatch.size() >= BATCH_SIZE) {
                            dao.insertAllRecipes(recipeBatch);
                            totalRecipesInserted += recipeBatch.size();
                            recipeBatch.clear();
                        }

                    } catch (Exception e) {
                        Log.e("DatabaseSetup", "Skipping malformed line: " + line, e);
                    }
                }
            }

            // Tam batch yapmayan kalan tarifleri ekle.
            if (!recipeBatch.isEmpty()) {
                dao.insertAllRecipes(recipeBatch);
                totalRecipesInserted += recipeBatch.size();
            }

            Log.d("DatabaseSetup", "Successfully inserted a total of " + totalRecipesInserted + " recipes.");

        } catch (IOException e) {
            Log.e("DatabaseSetup", "Failed to read CSV file.", e);
        }
    }


    private static volatile StepDatabase instance;
    private static final int NUM_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUM_THREADS);

    public static StepDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (StepDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    StepDatabase.class,
                                    "step_database")
                            .fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_5_6)
                            .addCallback(sRoomDatabaseCallback(context))
                            .build();
                }
            }
        }
        return instance;
    }
}