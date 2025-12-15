package com.example.stepappv3.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter; // 1. Change to ListAdapter
import androidx.recyclerview.widget.RecyclerView;
import com.example.stepappv3.R;

public class PantryCategoryAdapter extends ListAdapter<PantryCategory, PantryCategoryAdapter.CategoryViewHolder> {

    // 2. Define the listener interface (the "contract")
    public interface OnCategoryClickListener {
        void onCategoryClick(PantryCategory category);
    }

    private final OnCategoryClickListener clickListener;

    // 3. Update the constructor to accept the listener
    public PantryCategoryAdapter(OnCategoryClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pantry_category, parent, false);
        // Pass the listener to the ViewHolder when it's created
        return new CategoryViewHolder(view, clickListener, this::getItem);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        PantryCategory currentCategory = getItem(position);
        holder.bind(currentCategory);
    }

    // 4. Update the ViewHolder to handle the click
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final ImageView categoryImage;
        final TextView categoryName;

        // Functional interface to get the item at a position
        interface ItemProvider {
            PantryCategory get(int position);
        }

        public CategoryViewHolder(@NonNull View itemView, OnCategoryClickListener listener, ItemProvider itemProvider) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.category_image);
            categoryName = itemView.findViewById(R.id.category_name);

            // Set the click listener on the entire card view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // When clicked, call the listener's method, passing the item data
                    listener.onCategoryClick(itemProvider.get(position));
                }
            });
        }

        public void bind(PantryCategory category) {
            categoryName.setText(category.getName());
            categoryImage.setImageResource(category.getImageResId());
        }
    }

    // The DiffUtil.ItemCallback is perfect for ListAdapter
    private static final DiffUtil.ItemCallback<PantryCategory> DIFF_CALLBACK = new DiffUtil.ItemCallback<PantryCategory>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryCategory oldItem, @NonNull PantryCategory newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryCategory oldItem, @NonNull PantryCategory newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getImageResId() == newItem.getImageResId();
        }
    };
}