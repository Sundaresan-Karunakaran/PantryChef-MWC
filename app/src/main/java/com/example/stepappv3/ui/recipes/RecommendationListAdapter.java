package com.example.stepappv3.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stepappv3.R;
import com.example.stepappv3.recommender.Recommendation;

public class RecommendationListAdapter extends ListAdapter<Recommendation, RecommendationListAdapter.RecommendationViewHolder> {


    public interface OnRecipeClickListener {
        void onRecipeClick(Recommendation recommendation);
    }

    private final OnRecipeClickListener clickListener;

    public RecommendationListAdapter(@NonNull OnRecipeClickListener listener) {
        super(DIFF_CALLBACK);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommendation_item_list, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Recommendation currentRecommendation = getItem(position);
        holder.bind(currentRecommendation,clickListener);
    }
    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeNameTextView;
        private final TextView missingCountTextView;
        private final TextView missingLabelTextView;
        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.recipe_name_textview);
            missingCountTextView = itemView.findViewById(R.id.missing_count_textview);
            missingLabelTextView = itemView.findViewById(R.id.missing_label_textview);
        }

        public void bind(Recommendation recommendation, final OnRecipeClickListener listener) {
            recipeNameTextView.setText(recommendation.recipe.name);

            if (recommendation.missingCount == 0) {
                missingCountTextView.setText("✓");
                missingLabelTextView.setText("Perfect Match!");
            } else if (recommendation.missingCount == 1) {
                missingCountTextView.setText("1");
                missingLabelTextView.setText("ingredient missing");
            } else {
                missingCountTextView.setText(String.valueOf(recommendation.missingCount));
                missingLabelTextView.setText("ingredients missing");
            }
            itemView.setOnClickListener(v -> listener.onRecipeClick(recommendation));

        }
    }

    private static final DiffUtil.ItemCallback<Recommendation> DIFF_CALLBACK = new DiffUtil.ItemCallback<Recommendation>() {
        @Override
        public boolean areItemsTheSame(@NonNull Recommendation oldItem, @NonNull Recommendation newItem) {
            return oldItem.recipe.recipeId == newItem.recipe.recipeId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recommendation oldItem, @NonNull Recommendation newItem) {
            return oldItem.missingCount == newItem.missingCount &&
                    oldItem.recipe.name.equals(newItem.recipe.name);
        }
    };
}