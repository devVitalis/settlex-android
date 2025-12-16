package com.settlex.android.presentation.transactions.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.ItemRecipientBinding;
import com.settlex.android.presentation.transactions.model.RecipientUiModel;

public class RecipientAdapter extends ListAdapter<RecipientUiModel, RecipientAdapter.SuggestionsViewHolder> {
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(RecipientUiModel model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RecipientAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RecipientUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecipientUiModel oldItem, @NonNull RecipientUiModel newItem) {
            return oldItem.paymentId.equals(newItem.paymentId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecipientUiModel oldItem, @NonNull RecipientUiModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public SuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipientBinding binding = ItemRecipientBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SuggestionsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionsViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClickListener);
    }

    public static class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipientBinding binding;

        public SuggestionsViewHolder(@NonNull ItemRecipientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecipientUiModel recipient, OnItemClickListener listener) {
            ProfileService.loadProfilePic(recipient.photoUrl, binding.ivProfilePhoto);
            binding.tvFullName.setText(recipient.fullName);
            binding.tvPaymentId.setText(recipient.paymentId);
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(recipient));
        }
    }
}
