package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.R;
import com.settlex.android.databinding.ItemSuggestionBinding;
import com.settlex.android.ui.dashboard.model.SuggestionsUiModel;

public class SuggestionAdapter extends ListAdapter<SuggestionsUiModel, SuggestionAdapter.SuggestionsViewHolder> {

    public SuggestionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SuggestionsUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<SuggestionsUiModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull SuggestionsUiModel oldItem, @NonNull SuggestionsUiModel newItem) {
                    // Compare unique IDs or usernames (assuming usernames are unique)
                    return oldItem.getUsername().equals(newItem.getUsername());
                }

                @Override
                public boolean areContentsTheSame(@NonNull SuggestionsUiModel oldItem, @NonNull SuggestionsUiModel newItem) {
                    // Compare contents for changes
                    return oldItem.getFullName().equals(newItem.getFullName()) && oldItem.getUsername().equals(newItem.getUsername());
                }
            };

    @NonNull
    @Override
    public SuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionsViewHolder holder, int position) {
        SuggestionsUiModel suggestionsUiModel = getItem(position);
        holder.profilePic.setImageResource(R.drawable.ic_avatar);
        holder.fullName.setText(suggestionsUiModel.getFullName());
        holder.username.setText(suggestionsUiModel.getUsername());
    }

    public static class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView fullName, username;

        public SuggestionsViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemSuggestionBinding binding = ItemSuggestionBinding.bind(itemView);
            profilePic = binding.userProfilePic;
            fullName = binding.userFullName;
            username = binding.username;
        }
    }
}
