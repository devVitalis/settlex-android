package com.settlex.android.presentation.transactions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.settlex.android.databinding.ItemTransactionBinding
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.setTextColorRes
import com.settlex.android.presentation.common.extensions.toDateTimeString
import com.settlex.android.presentation.transactions.adapter.TransactionListAdapter.TransactionViewHolder
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel

/**
 * Base adapter for displaying transaction items in RecyclerView
 */
class TransactionListAdapter(private val listener: OnTransactionClickListener) :
    ListAdapter<TransactionItemUiModel, TransactionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        if (position == itemCount - 1) holder.binding.divider.gone()
        holder.bind(getItem(position), listener)
    }

    /**
     * ViewHolder for transaction items containing all transaction display elements
     */
    class TransactionViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(transaction: TransactionItemUiModel, listener: OnTransactionClickListener) =
            with(binding) {
                ivTxnIcon.setImageResource(transaction.serviceTypeIcon)
                tvServiceTypeName.text = transaction.serviceTypeName

                tvTxnOperation.text = transaction.operationSymbol
                tvTxnOperation.setTextColorRes(transaction.operationColor)
                tvTxnAmount.text = transaction.amount
                tvTxnAmount.setTextColorRes(transaction.operationColor)

                tvTxnDateTime.text = transaction.timestamp.toDateTimeString()
                recipientOrSender.text = transaction.recipientOrSenderName

                tvTxnStatus.text = transaction.status
                tvTxnStatus.setTextColorRes(transaction.statusColor)
                tvTxnStatus.setBackgroundResource(transaction.statusBackgroundColor)

                root.setOnClickListener { listener.onClick(transaction) }
            }
    }

    interface OnTransactionClickListener {
        fun onClick(transaction: TransactionItemUiModel)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<TransactionItemUiModel?> =
            object : DiffUtil.ItemCallback<TransactionItemUiModel?>() {
                override fun areItemsTheSame(
                    oldItem: TransactionItemUiModel,
                    newItem: TransactionItemUiModel
                ): Boolean {
                    return oldItem.transactionId == newItem.transactionId
                }

                override fun areContentsTheSame(
                    oldItem: TransactionItemUiModel,
                    newItem: TransactionItemUiModel
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}