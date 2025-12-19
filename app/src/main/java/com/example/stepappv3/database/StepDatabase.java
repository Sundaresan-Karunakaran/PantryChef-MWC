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

import com.example.stepappv3.database.ingredient.MasterIngredient;
import com.example.stepappv3.database.ingredient.MasterIngredientDao;
import com.example.stepappv3.database.profile.UserProfile;
import com.example.stepappv3.database.profile.UserProfileDao;
import com.example.stepappv3.database.recipes.Recipe;
import com.example.stepappv3.database.recipes.RecipeDao;
import com.example.stepappv3.database.recipes.RecipeIngredientJoin;
import com.example.stepappv3.database.recipes.RecipeIngredientJoinDao;
import com.example.stepappv3.database.steps.Step;
import com.example.stepappv3.database.steps.StepDao;
import com.example.stepappv3.database.pantry.PantryDao;
import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.util.DataLoadingManager;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Database(entities = {Step.class,PantryItem.class,Recipe.class, UserProfile.class, MasterIngredient.class, RecipeIngredientJoin.class}, version = 7, exportSchema = false)
public abstract class StepDatabase extends RoomDatabase {
    public abstract StepDao stepDao();
    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();
    public abstract UserProfileDao userProfileDao();
    public abstract MasterIngredientDao masterIngredientDao();
    public abstract RecipeIngredientJoinDao recipeIngredientJoinDao();


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pantry_items` (" +
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
                            "`steps` TEXT,`quants_g` TEXT, `ingredients` TEXT, `missingNutrients` TEXT, " +
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

    static final Migration MIGRATION_5_6 = new Migration(5,6) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `master_ingredient` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL UNIQUE, " +
                    "`synonyms` TEXT)"
            );
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6,7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database){
            database.execSQL("CREATE TABLE IF NOT EXISTS `recipe_ingredient_join` (" +
                    "`recipeId` INTEGER NOT NULL, " +
                    "`masterIngredientId` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`recipeId`, `masterIngredientId`), " +
                    "FOREIGN KEY(`masterIngredientId`) REFERENCES `master_ingredient`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`recipeId`) ON UPDATE NO ACTION ON DELETE CASCADE)"
            );
        }
    };

    private static RoomDatabase.Callback sRoomDatabaseCallback(final Context context) {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                databaseWriteExecutor.execute(() -> {
                    parseIngredientsFromCsv(context);
                    parseRecipesFromCsv(context);
                    parseRecipeIngredientJoinsFromCsv(context);
                    DataLoadingManager.getInstance(context).setDataLoadingComplete();
                });
            }
        };
    }

    private static void parseIngredientsFromCsv(Context context) {
        instance.runInTransaction(() -> {
            MasterIngredientDao dao = instance.masterIngredientDao();

            try (java.io.BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("ingredients.csv")))) {
                String line;
                boolean isFirstLine = true;
                Log.d("DatabaseSetup", "Starting to parse ingredients from CSV.");
                List<MasterIngredient> allIngredients = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    try{
                        MasterIngredient ingredient = new MasterIngredient(line.trim());
                        allIngredients.add(ingredient);

                    } catch (Exception e){
                        Log.e("DatabaseSetup", "Skipping malformed line: " + line, e);
                    }
                }
                dao.insertAll(allIngredients);
                Log.d("DatabaseSetup", "Finished inserting all ingredients.");
            } catch (java.io.IOException e) {
                Log.e("DatabaseSetup", "Failed to read CSV file.", e);
            }
        });
    }

    private static void parseRecipeIngredientJoinsFromCsv(Context context) {

        final int BATCH_SIZE = 500;
        instance.runInTransaction(() -> {
            Log.d("DatabaseSetup", "Starting to build recipe-ingredient joins.");
            MasterIngredientDao masterDao = instance.masterIngredientDao();
            RecipeIngredientJoinDao joinDao = instance.recipeIngredientJoinDao();

            List<MasterIngredient> allMasterIngredients = masterDao.getAllMasterIngredients();
            java.util.Map<String, Integer> ingredientNameToIdMap = new java.util.HashMap<>();
            for (MasterIngredient ingredient : allMasterIngredients) {

                ingredientNameToIdMap.put(ingredient.name, ingredient.id);
            }
            Log.d("DatabaseSetup", "Built in-memory dictionary with " + ingredientNameToIdMap.size() + " ingredients.");

            final Pattern csvSplitPattern = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("final_recipes.csv")))) {
                String line;
                boolean isFirstLine = true;
                List<RecipeIngredientJoin> joinsToInsert = new java.util.ArrayList<>(BATCH_SIZE);
                Log.d("DatabaseSetup", "Starting to parse recipes from CSV in batches.");

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] tokens = csvSplitPattern.split(line);
                    if (tokens.length == 12) {
                        try {

                            int recipeId = safeParseInt(unquoter(tokens[0]));
                            String ingredientsToken = unquoter(tokens[6]);
                            String cleanedData = ingredientsToken.replace("[", "").replace("]", "").replace("\"", "");
                            String[] ingredients = cleanedData.split(",");
                            for (String ingredientName : ingredients) {
                                String trimmedName = ingredientName.trim();
                                Integer masterIngredientId = ingredientNameToIdMap.get(trimmedName);

                                if (masterIngredientId != null) {
                                    joinsToInsert.add(new RecipeIngredientJoin(recipeId, masterIngredientId));
                                }
                            }
                        } catch (Exception e) {
                            Log.e("DatabaseSetup", "Skipping malformed join line: " + line, e);
                        }
                    }
                    if (joinsToInsert.size() >= BATCH_SIZE){
                        joinDao.insertAll(joinsToInsert);
                        Log.d("DatabaseSetup", "Inserting a batch of " + BATCH_SIZE + " joins.");
                        joinsToInsert.clear();
                    }
                }

                if (!joinsToInsert.isEmpty()) {
                    Log.d("DatabaseSetup", "Inserting final batch of " + joinsToInsert.size() + " recipe-ingredient join records.");
                    joinDao.insertAll(joinsToInsert);
                    joinsToInsert.clear();
                }
                Log.d("DatabaseSetup", "Finished building recipe-ingredient joins.");

            } catch (java.io.IOException e) {
                Log.e("DatabaseSetup", "Failed to read recipes CSV for join creation.", e);
            }
        });
    }
    private static void parseRecipesFromCsv(Context context) {

        final int BATCH_SIZE = 500;
        instance.runInTransaction(() -> {
            RecipeDao dao = instance.recipeDao();
            final Pattern csvSplitPattern = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("final_recipes.csv")))) {
                String line;
                boolean isFirstLine = true;
                List<Recipe> recipeBatch = new ArrayList<>(BATCH_SIZE);
                Log.d("DatabaseSetup", "Starting to parse recipes from CSV in batches.");

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] tokens = csvSplitPattern.split(line);
                    if (tokens.length == 12) {
                        try {
                            Recipe recipe = new Recipe(
                                    safeParseInt(unquoter(tokens[0])),
                                    unquoter(tokens[1]),
                                    unquoter(tokens[2]),
                                    safeParseInt(unquoter(tokens[3])),
                                    unquoter(tokens[4]),
                                    unquoter(tokens[5]),
                                    unquoter(tokens[6]),
                                    unquoter(tokens[7]),
                                    safeParseDouble(unquoter(tokens[8])),
                                    safeParseDouble(unquoter(tokens[9])),
                                    safeParseDouble(unquoter(tokens[10])),
                                    safeParseDouble(unquoter(tokens[11]))
                            );
                            recipeBatch.add(recipe);

                            if (recipeBatch.size() == BATCH_SIZE) {
                                Log.d("DatabaseSetup", "Inserting a batch of " + BATCH_SIZE + " recipes.");
                                dao.insertAllRecipes(recipeBatch);
                                recipeBatch.clear();
                            }

                        } catch (Exception e) {
                            Log.e("DatabaseSetup", "Skipping malformed line: " + line, e);
                        }
                    } else {
                        Log.w("DatabaseSetup", "Skipping line with incorrect token count (" + tokens.length + "): " + line);
                    }
                }

                if (!recipeBatch.isEmpty()) {
                    Log.d("DatabaseSetup", "Inserting final batch of " + recipeBatch.size() + " recipes.");
                    dao.insertAllRecipes(recipeBatch);
                }

                Log.d("DatabaseSetup", "Finished inserting all recipes.");

            } catch (IOException e) {
                Log.e("DatabaseSetup", "Failed to read CSV file.", e);
            }
        });
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7)
                        .addCallback(sRoomDatabaseCallback(context))
                        .build();

            }
        }
        return instance;
    }



}
