package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.R;
import com.settlex.android.databinding.ItemSuggestionBinding;
import com.settlex.android.ui.dashboard.model.SuggestionsUiModel;

import java.util.Collections;

public class SuggestionAdapter extends ListAdapter<SuggestionsUiModel, SuggestionAdapter.SuggestionsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SuggestionsUiModel model);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    public SuggestionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SuggestionsUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull SuggestionsUiModel oldItem, @NonNull SuggestionsUiModel newItem) {
            return oldItem.getUsername().equals(newItem.getUsername());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SuggestionsUiModel oldItem, @NonNull SuggestionsUiModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public SuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSuggestionBinding binding = ItemSuggestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SuggestionsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionsViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClickListener);
    }

    public static class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        private final ItemSuggestionBinding binding;

        public SuggestionsViewHolder(@NonNull ItemSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SuggestionsUiModel model, OnItemClickListener listener) {
            binding.userProfilePic.setImageResource(R.drawable.ic_avatar);
            binding.userFullName.setText(model.getFullName());
            binding.username.setText(model.getUsername());

            //Handle click
            binding.getRoot().setOnClickListener(v -> {
                listener.onItemClick(model);
                // getCurrentList() is immutable, so use submitList
                if (SuggestionsViewHolder.this.getBindingAdapter() != null) {
                    ((ListAdapter<?, ?>) SuggestionsViewHolder.this.getBindingAdapter()).submitList(Collections.emptyList());
                }
            });
        }
    }
}
