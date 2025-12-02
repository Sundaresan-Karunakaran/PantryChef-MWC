package com.example.stepappv3.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class PantryRepository {

    private final PantryDao pantryDao;
    private final LiveData<List<PantryItem>> allItems;

    public PantryRepository(Application application) {
        StepDatabase db = StepDatabase.getDatabase(application);
        pantryDao = db.pantryDao();
        allItems = pantryDao.getAllItems();
    }

    public LiveData<List<PantryItem>> getAllItems() {
        return allItems;
    }

    public void insert(final PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pantryDao.insert(item);
            }
        });
    }

    public void update(final PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pantryDao.update(item);
            }
        });
    }

    public void delete(final PantryItem item) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pantryDao.delete(item);
            }
        });
    }

    public void deleteAll() {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pantryDao.deleteAll();
            }
        });
    }

    /**
     * Gemini'nin döndürdüğü metni satır satır PantryItem'a çevirir ve ekler.
     *
     * Beklenen format örneği:
     *  - Milk 1L
     *  - Eggs (10 pcs)
     *  - Tomato 500 g
     *
     * Şimdilik basit yapıyoruz:
     *   • her satırı "name" olarak alıyoruz
     *   • quantity = 1
     *   • unit = "piece"
     *   • fromRecipe parametresine göre işaretliyoruz
     */
    public void addItemsFromText(final String text, final boolean fromRecipe) {
        StepDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (text == null) {
                    return;
                }

                String[] lines = text.split("\\r?\\n");
                List<PantryItem> items = new ArrayList<PantryItem>();

                for (String line : lines) {
                    if (line == null) {
                        continue;
                    }

                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }

                    // Başındaki bullet karakterlerini temizle
                    if (trimmed.startsWith("-")) {
                        trimmed = trimmed.substring(1).trim();
                    } else if (trimmed.startsWith("•")) {
                        trimmed = trimmed.substring(1).trim();
                    }

                    if (trimmed.isEmpty()) {
                        continue;
                    }

                    // Şimdilik: her satır 1 adet "piece"
                    String name = trimmed;
                    double quantity = 1.0;
                    String unit = "piece";

                    PantryItem item = new PantryItem(name, quantity, unit, fromRecipe);
                    items.add(item);
                }

                for (PantryItem item : items) {
                    pantryDao.insert(item);
                }
            }
        });
    }

    /**
     * fromRecipe = true varsayılanı ile kısayol.
     * (Scanner için kullanmak isteyebiliriz)
     */
    public void addItemsFromText(final String text) {
        addItemsFromText(text, true);
    }
}
