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

import com.example.stepappv3.database.profile.UserProfile;
import com.example.stepappv3.database.profile.UserProfileDao;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeDao;
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.util.DataLoadingManager;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Step.class,PantryItem.class,Recipe.class, UserProfile.class}, version = 5, exportSchema = false)
public abstract class StepDatabase extends RoomDatabase {
    public abstract StepDao stepDao();
    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();
    public abstract UserProfileDao userProfileDao();





    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pantry_items` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT, " +
                            "`category` TEXT, " +
                            "`quantity` INTEGER NOT NULL, " +
                            "`unit` TEXT, " +
                            "`expiryDate` INTEGER NOT NULL)"
            );
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "ALTER TABLE daily_steps ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
            );
            database.execSQL(
                    "ALTER TABLE pantry_items ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
            );
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3,4){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database){
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recipes` ( " +
                            "`recipeId` INTEGER PRIMARY KEY NOT NULL, " +
                            "`name` TEXT, `servingSize` TEXT, `servings` INTEGER NOT NULL, " +
                            "`steps` TEXT, `ingredients` TEXT, `missingNutrients` TEXT, " +
                            "`calories` REAL NOT NULL, `totalFat` REAL NOT NULL, " +
                            "`totalSugars` REAL NOT NULL, `dietaryFiber` REAL NOT NULL)"
            );
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4,5){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database){
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_profile` (" +
                            "`userId` TEXT NOT NULL, " +
                            "`gender` TEXT, " +
                            "`age` INTEGER NOT NULL, " +
                            "`weight` DOUBLE NOT NULL, " +
                            "`height` DOUBLE NOT NULL, " +
                            "`activityLevel` DOUBLE NOT NULL, " +
                            "PRIMARY KEY(`userId`))"
            );
        }
    };

    private static RoomDatabase.Callback sRoomDatabaseCallback(final Context context) {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                // THE FIX: Delegate the heavy parsing work to our background executor.
                databaseWriteExecutor.execute(() -> {
                    // This code block will now run safely on a background thread.
                    parseRecipesFromCsv(context);
                    DataLoadingManager.getInstance(context).setDataLoadingComplete();
                });
            }
        };
    }

    private static void parseRecipesFromCsv(Context context) {
        RecipeDao dao = instance.recipeDao();
        final java.util.regex.Pattern csvSplitPattern = java.util.regex.Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(context.getAssets().open("final_recipes.csv")))) {
            String line;
            boolean isFirstLine = true;
            List<Recipe> recipeList = new java.util.ArrayList<>();
            Log.d("DatabaseSetup", "Starting to parse recipes from CSV.");

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] tokens = csvSplitPattern.split(line);

                if (tokens.length == 11) {
                    // We moved the NumberFormatException catch outside to handle any parsing failure in the line.
                    try {
                        Recipe recipe = new com.example.stepappv3.database.recipes.Recipe(
                                safeParseInt(unquoter(tokens[0])),      // recipeId
                                unquoter(tokens[1]),                     // name
                                unquoter(tokens[2]),                     // servingSize
                                safeParseInt(unquoter(tokens[3])),     // servings
                                unquoter(tokens[4]),                     // steps
                                unquoter(tokens[5]),                     // ingredients
                                unquoter(tokens[6]),                     // missingNutrients
                                safeParseDouble(unquoter(tokens[7])),  // calories
                                safeParseDouble(unquoter(tokens[8])),  // totalFat
                                safeParseDouble(unquoter(tokens[9])),  // totalSugars
                                safeParseDouble(unquoter(tokens[10])) // dietaryFiber
                        );
                        recipeList.add(recipe);
                    } catch (Exception e) { // Catch any unexpected error during row parsing
                        Log.e("DatabaseSetup", "Skipping malformed line: " + line, e);
                    }
                } else {
                    Log.w("DatabaseSetup", "Skipping line with incorrect token count (" + tokens.length + "): " + line);
                }
            }
            Log.d("DatabaseSetup", "Finished parsing. Attempting to insert " + recipeList.size() + " recipes.");
            if (!recipeList.isEmpty()) {
                dao.insertAllRecipes(recipeList); // Ensure this method name is correct
                Log.d("DatabaseSetup", "Successfully inserted recipes.");
            } else {
                Log.w("DatabaseSetup", "No recipes were parsed to insert.");
            }
        } catch (java.io.IOException e) {
            Log.e("DatabaseSetup", "Failed to read CSV file.", e);
        }
    }


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
                        .fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5)
                        .addCallback(sRoomDatabaseCallback(context))
                        .build();

            }
        }
        return instance;
    }



}
