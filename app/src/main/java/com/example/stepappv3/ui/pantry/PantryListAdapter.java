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
import com.example.stepappv3.database.pantry.PantryItemDisplay;
import com.google.android.material.button.MaterialButton;

public class PantryListAdapter extends ListAdapter<PantryItemDisplay, PantryListAdapter.PantryItemViewHolder> {

    public interface OnEditItemClickListener {
        void onEditClicked(PantryItemDisplay item);
    }

    private final OnEditItemClickListener editListener;

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
        PantryItemDisplay currentItem = getItem(position);
        holder.bind(currentItem, editListener);
    }
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

        public void bind(PantryItemDisplay item, OnEditItemClickListener listener) {
            itemNameTextView.setText(item.name);
            itemQuantityTextView.setText(String.format("%d %s", item.quantity, item.unit));

            editButton.setOnClickListener(v -> listener.onEditClicked(item));
        }
    }
    private static final DiffUtil.ItemCallback<PantryItemDisplay> DIFF_CALLBACK = new DiffUtil.ItemCallback<PantryItemDisplay>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItemDisplay oldItem, @NonNull PantryItemDisplay newItem) {
            return oldItem.masterIngredientId == newItem.masterIngredientId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItemDisplay oldItem, @NonNull PantryItemDisplay newItem) {

            return oldItem.equals(newItem);
        }
    };
}