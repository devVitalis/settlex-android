package com.settlex.android.presentation.transactions

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.settlex.android.databinding.ActivityTransactionHistoryBinding
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.services.AirtimePurchaseActivity
import com.settlex.android.presentation.transactions.adapter.TransactionListAdapter
import com.settlex.android.presentation.transactions.model.TransactionUiModel
import com.settlex.android.presentation.transactions.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionsListAdapter: TransactionListAdapter
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initTransactionList()

        // Fetch transactions from the current month
        viewModel.fetchTransactionsForTheMonth()
        observeUserTransactionsHistory()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnMakeYourFirstTransaction.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AirtimePurchaseActivity::class.java
                )
            )
        }
    }

    private fun initTransactionList() {
        transactionsListAdapter =
            TransactionListAdapter(object : TransactionListAdapter.OnTransactionClickListener {
                override fun onClick(transaction: TransactionUiModel) {
                    val intent =
                        Intent(this@TransactionHistoryActivity, TransactionActivity::class.java)
                    intent.putExtra("transaction", transaction)
                    startActivity(intent)
                }
            })

        binding.rvTransactionHistory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = transactionsListAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeUserTransactionsHistory() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchTransactionsForTheMonth.collect { state ->
                    when (state) {
                        is UiState.Loading -> onTransactionLoading()
                        is UiState.Success -> setTransactionsData(state.data)
                        is UiState.Failure -> onTransactionError()
                    }
                }
            }
        }
    }

    private fun onTransactionLoading() = with(binding) {
        listOf(viewEmptyState, rvTransactionHistory).forEach { it.gone() }
        shimmerTransactions.show()
    }

    private fun onTransactionError() = with(binding) {
        listOf(shimmerTransactions, rvTransactionHistory).forEach { it.gone() }
        viewEmptyState.show()
    }

    private fun setTransactionsData(transactions: List<TransactionUiModel>?) = with(binding) {
        if (transactions?.isEmpty() == true) {
            transactionsListAdapter.submitList(emptyList())
            shimmerTransactions.gone()
            rvTransactionHistory.gone()
            viewEmptyState.show()
        } else {
            transactionsListAdapter.submitList(transactions)
            shimmerTransactions.gone()
            viewEmptyState.gone()
            rvTransactionHistory.show()
        }
    }
}
