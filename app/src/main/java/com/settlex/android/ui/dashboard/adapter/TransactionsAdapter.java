package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.R;
import com.settlex.android.databinding.ItemTransactionBinding;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;

import java.util.List;

/**
 * Base adapter for displaying transaction items in RecyclerView
 */
public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private final List<TransactionUiModel> transactions;

    public TransactionsAdapter(List<TransactionUiModel> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionUiModel txn = transactions.get(position);

        holder.icon.setImageResource(txn.getServiceTypeIcon());
        holder.name.setText(txn.getServiceTypeName());

        holder.operation.setText(txn.getOperationSymbol());
        holder.operation.setTextColor(txn.getOperationColor());

        holder.amount.setText(txn.getAmount());
        holder.amount.setTextColor(txn.getOperationColor());

        holder.dateTime.setText(txn.getTimestamp());
        holder.recipientOrSender.setText(txn.getRecipientOrSender());

        holder.status.setText(txn.getStatus());
        holder.status.setTextColor(txn.getStatusColor());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    /**
     * ViewHolder for transaction items containing all transaction display elements
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, operation, amount, dateTime, recipientOrSender, status;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemTransactionBinding binding = ItemTransactionBinding.bind(itemView);
            icon = binding.icon;
            name = binding.name;
            operation = binding.operation;
            amount = binding.amount;
            dateTime = binding.dateTime;
            recipientOrSender = binding.recipientOrSender;
            status = binding.status;
        }
    }
}