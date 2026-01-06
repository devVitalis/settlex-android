package com.settlex.android.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.settlex.android.R
import com.settlex.android.databinding.FragmentTransactionDetailsBinding
import com.settlex.android.presentation.common.extensions.copyToClipboard
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionDetailsFragment : Fragment() {
    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: FragmentTransactionDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)

        StatusBar.setColor(requireActivity(), R.color.white)
        onBackButtonPressed()
        binding.btnBackBefore.setOnClickListener { requireActivity().finish() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transaction =
            requireActivity().intent.getParcelableExtra<TransactionItemUiModel?>("transaction")
        if (transaction != null) {
            bindTransaction(transaction)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindTransaction(transaction: TransactionItemUiModel) = with(binding) {
        icon.setImageResource(transaction.serviceTypeIcon)
        name.text = transaction.serviceTypeName

        operation.text = transaction.operationSymbol
        operation.setTextColor(
            ContextCompat.getColor(
                root.context,
                transaction.operationColor
            )
        )

        amount.text = transaction.amount
        amount.setTextColor(
            ContextCompat.getColor(
                root.context,
                transaction.operationColor
            )
        )

        status.text = transaction.status
        status.setTextColor(
            ContextCompat.getColor(
                root.context,
                transaction.statusColor
            )
        )
        status.setBackgroundResource(transaction.statusBackgroundColor)

        dateTime.text = transaction.timestamp

        recipient.text = transaction.recipientId
        recipientName.text = transaction.recipientName

        sender.text = transaction.senderId

        // show description if there any
        descriptionContainer.visibility = View.VISIBLE
        description.text = transaction.description

        transactionId.text = transaction.transactionId
        copyTransactionId.setOnClickListener { transactionId.copyToClipboard("Transaction ID") }
    }

    private fun onBackButtonPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            getViewLifecycleOwner(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }
}