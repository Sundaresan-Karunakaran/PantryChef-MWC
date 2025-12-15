package com.example.stepappv3.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stepappv3.R;
import com.example.stepappv3.database.pantry.PantryItem;
import com.google.android.material.button.MaterialButton;

public class PantryListAdapter extends ListAdapter<PantryItem, PantryListAdapter.PantryItemViewHolder> {

    // 1. Define a new, simpler listener interface
    public interface OnEditItemClickListener {
        void onEditClicked(PantryItem item);
    }

    private final OnEditItemClickListener editListener;

    // 2. The constructor now only needs one listener
    public PantryListAdapter(@NonNull OnEditItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.editListener = listener;
    }

    @NonNull
    @Override
    public PantryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pantry_item_list, parent, false);
        return new PantryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryItemViewHolder holder, int position) {
        PantryItem currentItem = getItem(position);
        // Pass the item and the listener to the ViewHolder
        holder.bind(currentItem, editListener);
    }

    /**
     * The ViewHolder class. It's now much simpler.
     */
    static class PantryItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemNameTextView;
        private final TextView itemQuantityTextView;
        private final MaterialButton editButton;

        public PantryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.item_name_textview);
            itemQuantityTextView = itemView.findViewById(R.id.item_quantity_textview);
            editButton = itemView.findViewById(R.id.edit_item_button);
        }

        // 3. The bind method now sets a single, simple click listener
        public void bind(PantryItem item, OnEditItemClickListener listener) {
            itemNameTextView.setText(item.name);
            itemQuantityTextView.setText(String.format("%d %s", item.quantity, item.unit));

            editButton.setOnClickListener(v -> listener.onEditClicked(item));
        }
    }

    /**
     * The DiffUtil.ItemCallback remains the same and is perfect for ListAdapter.
     */
    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<PantryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.name.equals(newItem.name) &&
                    oldItem.quantity == newItem.quantity &&
                    oldItem.unit.equals(newItem.unit);
        }
    };
}