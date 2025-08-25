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
import com.settlex.android.util.string.StringUtil;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transactions, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionUiModel transaction = transactions.get(position);

        // Set icon based on transaction type
        if ("credit".equalsIgnoreCase(transaction.getOperation())) {
            holder.icon.setImageResource(R.drawable.ic_money_added);
            holder.operation.setText("+");

        } else if ("debit/send".equalsIgnoreCase(transaction.getOperation())) {
            holder.icon.setImageResource(R.drawable.ic_money_sent);
            holder.operation.setText("-");

        } else {
            holder.icon.setImageResource(R.drawable.ic_money_received);
            holder.operation.setText("-");
        }

        // Bind transaction data to views
        holder.title.setText(transaction.getTitle());
        holder.amount.setText(StringUtil.formatToNaira(transaction.getAmount()));
        holder.dateTime.setText(StringUtil.formatTimeStamp(transaction.getDateTime()));
        holder.status.setText(transaction.getStatus());
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
        TextView title, operation, amount, status, dateTime;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            operation = itemView.findViewById(R.id.operation);
            amount = itemView.findViewById(R.id.amount);
            dateTime = itemView.findViewById(R.id.dateTime);
            status = itemView.findViewById(R.id.status);
        }
    }
}