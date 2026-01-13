package com.settlex.android.presentation.transactions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.databinding.ItemRecipientBinding
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.transactions.adapter.RecipientAdapter.SuggestionsViewHolder
import com.settlex.android.presentation.transactions.model.RecipientUiModel

class RecipientAdapter(private val onItemClickListener: OnItemClickListener) :
    ListAdapter<RecipientUiModel, SuggestionsViewHolder>(DIFF_CALLBACK) {

    interface OnItemClickListener {
        fun onClick(selectedRecipient: RecipientUiModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionsViewHolder {
        val binding =
            ItemRecipientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SuggestionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionsViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClickListener)
    }

    class SuggestionsViewHolder(private val binding: ItemRecipientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipient: RecipientUiModel, listener: OnItemClickListener) = with(binding) {
            loadProfilePhoto(recipient.photoUrl, ivProfilePhoto)
            tvFullName.text = recipient.fullName
            tvPaymentId.text = recipient.paymentId.addAtPrefix()
            root.setOnClickListener { listener.onClick(recipient) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecipientUiModel>() {
            override fun areItemsTheSame(
                oldItem: RecipientUiModel,
                newItem: RecipientUiModel
            ): Boolean {
                return oldItem.paymentId == newItem.paymentId
            }

            override fun areContentsTheSame(
                oldItem: RecipientUiModel,
                newItem: RecipientUiModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
