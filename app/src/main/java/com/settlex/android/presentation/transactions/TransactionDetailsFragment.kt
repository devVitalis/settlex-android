package com.settlex.android.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.settlex.android.R
import com.settlex.android.databinding.FragmentTransactionDetailsBinding
import com.settlex.android.presentation.common.extensions.copyToClipboard
import com.settlex.android.presentation.common.extensions.getParcelableExtraCompat
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.setTextColorRes
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toFullDateTimeString
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionDetailsFragment : Fragment() {
    private var _binding: FragmentTransactionDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)

        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindTransactionDetails(
            requireActivity().intent.getParcelableExtraCompat<TransactionItemUiModel>(
                "transaction"
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(requireActivity(), R.attr.colorSurface)
        onBackButtonPressed()

        ivCopyTransactionId.setOnClickListener { tvTransactionId.copyToClipboard("Transaction ID") }
        btnBackBefore.setOnClickListener { requireActivity().finish() }
    }

    private fun bindTransactionDetails(transaction: TransactionItemUiModel) = with(binding) {
        ivTxnIcon.setImageResource(transaction.serviceTypeIcon)
        tvTxnName.text = transaction.serviceTypeName

        tvTxnOperation.apply {
            text = transaction.operationSymbol
            setTextColorRes(transaction.operationColor)
        }

        tvTxnAmount.apply {
            text = transaction.amount
            setTextColorRes(transaction.operationColor)
        }

        tvTxnStatus.apply {
            text = transaction.status
            setTextColorRes(transaction.statusColor)
            setBackgroundResource(transaction.statusBackgroundColor)
        }

        tvTxnDateTime.text = transaction.timestamp.toFullDateTimeString()

        tvTxnRecipientId.text = transaction.recipientId
        tvTxnRecipientName.text = transaction.recipientName

        tvTxnSenderId.text = transaction.senderId

        // Show description if there any
        when (transaction.description) {
            null -> viewDescriptionContainer.gone()
            else -> {
                tvTxnDescription.text = transaction.description
                viewDescriptionContainer.show()
            }
        }

        tvTransactionId.text = transaction.transactionId
    }

    private fun onBackButtonPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            getViewLifecycleOwner(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        )
    }
}