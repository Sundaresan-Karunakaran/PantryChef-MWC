package com.example.stepappv3.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;
import com.example.stepappv3.database.PantryItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for pantry items.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface PantryItemListener {
        void onIncreaseClicked(PantryItem item);
        void onDecreaseClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private List<PantryItem> items;
    private PantryItemListener listener;

    public PantryAdapter(PantryItemListener listener) {
        this.items = new ArrayList<PantryItem>();
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        if (newItems == null) {
            this.items = new ArrayList<PantryItem>();
        } else {
            this.items = newItems;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        final PantryItem item = items.get(position);

        holder.textName.setText(item.getName());
        String quantityText = item.getQuantity() + " " + item.getUnit();
        holder.textQuantity.setText(quantityText);

        // fromRecipe ise istersen burada görsel bir ipucu ekleyebilirsin (şimdilik boş)

        holder.buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onIncreaseClicked(item);
                }
            }
        });

        holder.buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDecreaseClicked(item);
                }
            }
        });

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDeleteClicked(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        TextView textQuantity;
        MaterialButton buttonIncrease;
        MaterialButton buttonDecrease;
        MaterialButton buttonDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
