package com.example.stepappv3.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;

import java.util.List;

public class PantryCategoryAdapter extends RecyclerView.Adapter<PantryCategoryAdapter.CategoryViewHolder> {

    // Inside PantryCategoryAdapter.java

    /**
     * A DiffUtil.Callback to efficiently update the RecyclerView.
     * This is the "detective" that finds the differences between two lists.
     */
    private static class PantryCategoryDiffCallback extends DiffUtil.Callback {

        private final List<PantryCategory> oldList;
        private final List<PantryCategory> newList;

        public PantryCategoryDiffCallback(List<PantryCategory> oldList, List<PantryCategory> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        /**
         * Called to decide if two objects represent the same item.
         * For example, if they have the same unique ID.
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // For now, since our category has no unique ID, we'll use the name.
            // In a real database-backed app, you would compare category.getId().
            return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
        }

        /**
         * Called only if areItemsTheSame() returns true.
         * This checks if the item's contents have changed, requiring a UI update.
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            PantryCategory oldCategory = oldList.get(oldItemPosition);
            PantryCategory newCategory = newList.get(newItemPosition);
            // Compare all relevant fields to see if a redraw is needed.
            return oldCategory.getName().equals(newCategory.getName()) &&
                    oldCategory.getImageResId() == newCategory.getImageResId();
        }
    }

    private final List<PantryCategory> categoryList;

    public PantryCategoryAdapter(List<PantryCategory> categoryList) {
        this.categoryList = categoryList;
    }

    /**
     * Called by the RecyclerView when it needs to create a new card.
     * It inflates the XML layout for a single card.
     */
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pantry_category, parent, false);
        return new CategoryViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display the data at a specific position.
     * It takes a category object and "binds" its data to the views in the card.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        PantryCategory category = categoryList.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryImage.setImageResource(category.getImageResId());
        // The subhead is static, so it's already set in the XML.
    }

    /**
     * Tells the RecyclerView the total number of items in the list.
     */
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    /**
     * The ViewHolder. This is the "construction worker" that holds onto the
     * views for a single card to avoid repeated lookups.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final ImageView categoryImage;
        final TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.category_image);
            categoryName = itemView.findViewById(R.id.category_name);
            // We don't need a reference to the subhead since it doesn't change.
        }
    }

    // Inside PantryCategoryAdapter.java

    /**
     * The public API for submitting a new list of data to the adapter.
     * This will calculate the difference and perform efficient updates.
     */
    public void updateData(List<PantryCategory> newCategories) {// 1. Create an instance of our diff callback.
        PantryCategoryDiffCallback diffCallback = new PantryCategoryDiffCallback(this.categoryList, newCategories);
        // 2. Calculate the difference between the old and new lists.
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // 3. Update the internal list of the adapter.
        this.categoryList.clear();
        this.categoryList.addAll(newCategories);

        // 4. Dispatch the calculated updates to the RecyclerView.
        // This is the magic step that runs the correct animations!
        diffResult.dispatchUpdatesTo(this);
    }
}