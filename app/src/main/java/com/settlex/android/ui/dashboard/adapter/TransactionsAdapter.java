package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.R;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;

import java.util.List;

/**
 * Base adapter for displaying transaction items in RecyclerView
 */
public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private final List<TransactionUiModel> transactions;

    public TransactionsAdapter(List<TransactionUiModel> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transactions, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionUiModel txn = transactions.get(position);

        holder.serviceIcon.setImageResource(txn.getServiceTypeIcon());
        holder.serviceName.setText(txn.getServiceTypeName());

        holder.operation.setText(txn.getOperationSymbol());
        holder.operation.setTextColor(txn.getOperationColor());

        holder.amount.setText(txn.getAmount());
        holder.dateTime.setText(txn.getTimestamp());

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
        ImageView serviceIcon;
        TextView serviceName, operation, amount, status, dateTime;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceIcon = itemView.findViewById(R.id.serviceIcon);
            serviceName = itemView.findViewById(R.id.serviceName);
            operation = itemView.findViewById(R.id.operation);
            amount = itemView.findViewById(R.id.amount);
            dateTime = itemView.findViewById(R.id.dateTime);
            status = itemView.findViewById(R.id.status);
        }
    }
}