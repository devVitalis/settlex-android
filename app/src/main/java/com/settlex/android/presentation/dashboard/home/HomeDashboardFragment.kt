package com.settlex.android.presentation.dashboard.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.settlex.android.R
import com.settlex.android.data.enums.ServiceType
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.FragmentDashboardHomeBinding
import com.settlex.android.presentation.auth.login.LoginActivity
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.setAsterisks
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toastNotImplemented
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.account.ProfileActivity
import com.settlex.android.presentation.dashboard.home.adapter.PromotionalBannerAdapter
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import com.settlex.android.presentation.dashboard.home.viewmodel.HomeViewModel
import com.settlex.android.presentation.dashboard.home.viewmodel.PromoBannerViewModel
import com.settlex.android.presentation.dashboard.services.adapter.ServicesAdapter
import com.settlex.android.presentation.dashboard.services.model.ServiceUiModel
import com.settlex.android.presentation.settings.CreatePaymentIdActivity
import com.settlex.android.presentation.transactions.TransactionActivity
import com.settlex.android.presentation.transactions.TransferToFriendActivity
import com.settlex.android.presentation.transactions.adapter.TransactionListAdapter
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.presentation.wallet.CommissionWithdrawalActivity
import com.settlex.android.presentation.wallet.ReceiveActivity
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeDashboardFragment : Fragment() {
    private var backPressedTime: Long = 0
    private var autoScrollRunnable: Runnable? = null
    private val autoScrollHandler = Handler(Looper.getMainLooper())

    // Dependencies
    private var _binding: FragmentDashboardHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionsListAdapter: TransactionListAdapter

    private val viewModel: HomeViewModel by activityViewModels()
    private val bannerViewModel: PromoBannerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardHomeBinding.inflate(inflater, container, false)

        initViews()
        initObservers()
        initAppServices()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
        _binding = null
    }

    private fun initObservers() {
        observeUserSession()
        observeUserBalance()
        observeUserBalanceHiddenState()
        observeUserRecentTransactions()
        observePromotionalBanners()
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.colorBackgroundInverse)
        initListeners()
        initTransactionRecyclerView()
        setupDoubleBackPressToExit()
    }

    private fun initListeners() = with(binding) {
        btnReceive.setOnClickListener { startActivity(ReceiveActivity::class.java) }
        ivProfilePhoto.setOnClickListener { startActivity(ProfileActivity::class.java) }
        btnLogin.setOnClickListener { startActivity(LoginActivity::class.java) }
        btnTransfer.setOnClickListener { startActivity(TransferToFriendActivity::class.java) }
        btnNotification.setOnClickListener { it.toastNotImplemented() }
        btnSupport.setOnClickListener { it.toastNotImplemented() }
        tvViewAllTransaction.setOnClickListener { it.toastNotImplemented() }
        btnDeposit.setOnClickListener { it.toastNotImplemented() }
        ivBalanceToggle.setOnClickListener { viewModel.toggleBalanceVisibility() }
        btnRefreshTransactions.setOnClickListener { viewModel.fetchRecentTransactions("Testing") }

        viewUserCommissionBalance.setOnClickListener {
            startActivity(CommissionWithdrawalActivity::class.java)
        }
    }

    private fun initTransactionRecyclerView() = with(binding) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        rvTransactions.setLayoutManager(layoutManager)

        // Initialize adapter and set click listener
        transactionsListAdapter = TransactionListAdapter { transaction ->
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            startActivity(intent)
        }
    }

    private fun observeUserSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> {
                            // Fetch recent transactions
                            viewModel.fetchRecentTransactions(state.user.uid)
                            onUserDataReceived(state.user)
                        }

                        is UserSessionState.Loading -> showUserLoadingState()
                        is UserSessionState.UnAuthenticated -> showUnauthenticatedState()
                        is UserSessionState.Error -> handleUserErrorState()
                    }
                }
            }
        }
    }

    private fun showUserLoadingState() = with(binding) {
        listOf(
            tvUserFullName,
            tvUserBalance,
            viewUserCommissionBalance
        ).forEach { it.gone() }

        listOf(
            shimmerUserFullName,
            shimmerUserBalance,
            shimmerUserCommissionBalance
        ).forEach { it.show() }
    }

    private fun onUserDataReceived(user: HomeUiModel) = with(binding) {
        if (user.paymentId == null) {
            startActivity(CreatePaymentIdActivity::class.java)
            return
        }

        // Dismiss loading shimmer
        listOf(
            shimmerUserFullName,
            shimmerUserBalance,
            shimmerUserCommissionBalance
        ).forEach { it.gone() }

        // Show ui views
        listOf(
            tvUserFullName,
            tvUserBalance,
            viewUserCommissionBalance
        ).forEach { it.show() }

        ProfileService.loadProfilePhoto(user.photoUrl, ivProfilePhoto)
        tvUserFullName.text = user.fullName
    }

    private fun handleUserErrorState() = with(binding) {

    }

    private fun showUnauthenticatedState() = with(binding) {
        // Set unauthenticated UI
        listOf(
            shimmerUserFullName, shimmerUserBalance,
            shimmerUserCommissionBalance, ivProfilePhoto,
            ivBalanceToggle, viewGreetingContainer,
            viewPaymentButtons, viewTransactionContainer,
            viewRecentTransactionsLabel,
        ).forEach { it.gone() }

        // Hide balance
        listOf(
            tvUserBalance,
            tvUserCommissionBalance,
        ).forEach { it.setAsterisks() }

        listOf(
            tvUserBalance,
            viewUserCommissionBalance,
        ).forEach { it.show() }

        // Redirect action to auth
        listOf(
            btnReceive, ivProfilePhoto, btnLogin,
            btnTransfer, btnNotification, btnSupport,
            btnDeposit, ivBalanceToggle,
            viewUserCommissionBalance,
        ).forEach { it.setOnClickListener { startActivity(LoginActivity::class.java) } }

        btnLogin.show()
    }

    private fun observeUserBalance() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userBalance.collect { balance ->
                    val (userBalance, commissionBalance) = balance ?: return@collect
                    tvUserBalance.text = userBalance
                    tvUserCommissionBalance.text = commissionBalance
                }
            }
        }
    }

    private fun observeUserBalanceHiddenState() = with(binding) {
        // Cache drawable (init once)
        val balanceHiddenDrawable = R.drawable.ic_visibility_off
        val balanceVisibleDrawable = R.drawable.ic_visibility_on

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBalanceHidden.collect { isBalanceHidden ->
                    when {
                        isBalanceHidden -> ivBalanceToggle.setImageResource(balanceHiddenDrawable)
                        else -> ivBalanceToggle.setImageResource(balanceVisibleDrawable)
                    }
                }
            }
        }
    }

    private fun observeUserRecentTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentTransactions.collect { transactions ->
                    when (transactions) {
                        is UiState.Loading -> onTransactionsLoading()
                        is UiState.Success -> setTransactionsData(transactions.data)
                        is UiState.Failure -> onTransactionsError(transactions.exception)
                    }
                }
            }
        }
    }

    private fun onTransactionsLoading() = with(binding) {
        listOf(
            viewNoTransactionsUi,
            viewNoInternet,
            rvTransactions,
        ).forEach { it.gone() }

        shimmerTransactions.show()
    }

    private fun setTransactionsData(transactions: List<TransactionItemUiModel>?) = with(binding) {
        listOf(
            shimmerTransactions,
            viewNoInternet
        ).forEach { it.gone() }

        when (transactions?.isEmpty()) {
            true -> {
                // Clear recyclerview
                transactionsListAdapter.submitList(emptyList())
                rvTransactions.setAdapter(transactionsListAdapter)

                viewNoTransactionsUi.show()
            }

            else -> {
                // Show transactions
                transactionsListAdapter.submitList(transactions)
                rvTransactions.setAdapter(transactionsListAdapter)

                viewNoTransactionsUi.gone()
                rvTransactions.show()
            }
        }
    }

    private fun onTransactionsError(error: AppException) = with(binding) {
        listOf(shimmerTransactions, viewNoTransactionsUi, rvTransactions).forEach { it.gone() }

        when (error) {
            is AppException.NetworkException -> {
                viewNoInternet.show()
                return
            }

            else -> Unit
        }
    }

    private fun observePromotionalBanners() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bannerViewModel.banners.collect { banner ->
                    when (banner) {
                        is UiState.Loading -> promoBannerProgressBar.show()
                        is UiState.Success -> onPromoBannersSuccess(banner.data)
                        is UiState.Failure -> promoBannerProgressBar.gone()
                    }
                }
            }
        }
    }

    private fun onPromoBannersSuccess(banner: List<PromoBannerUiModel>) = with(binding) {
        promoBannerProgressBar.gone()

        if (banner.isEmpty()) {
            viewPromoBannerContainer.gone()
            return@with
        }

        val adapter = PromotionalBannerAdapter(banner)
        bannerViewPager.setAdapter(adapter)
        viewPromoBannerContainer.show()

        // Attach dots
        dotsIndicator.attachTo(bannerViewPager)
        setAutoScrollForPromoBanner(banner.size)
    }

    private fun setAutoScrollForPromoBanner(size: Int) = with(binding) {
        if (size <= 1) return@with

        autoScrollRunnable = object : Runnable {
            var currentPosition: Int = 0

            override fun run() {
                if (bannerViewPager.adapter == null) return

                currentPosition = (currentPosition + 1) % size // loop back to 0
                bannerViewPager.setCurrentItem(currentPosition, true)

                // Schedule next slide
                autoScrollHandler.postDelayed(this, 4000)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, 4000)
    }

    private fun initAppServices() = with(binding) {
        val layoutManager = GridLayoutManager(context, 4)
        rvServices.setLayoutManager(layoutManager)

        val serviceList = ServiceType.entries
            .take(8)
            .map { serviceType ->
                ServiceUiModel(
                    serviceType.displayName,
                    serviceType.iconRes,
                    serviceType.cashbackPercentage,
                    serviceType.label,
                    serviceType.transactionServiceType,
                    serviceType.destination
                )
            }
        val adapter = ServicesAdapter(false, serviceList) { serviceUiModel ->
            val destination = serviceUiModel.destination
            when (destination) {
                null -> Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT)
                    .show()

                else -> {
                    when {
                        destination.isActivity -> startActivity(
                            Intent(
                                context,
                                destination.activity
                            )
                        )

                        destination.isFragment -> {
                            val navController = NavHostFragment.findNavController(
                                this@HomeDashboardFragment
                            )
                            navController.navigate(destination.navDestinationId!!)
                        }
                    }
                }
            }
        }
        rvServices.setAdapter(adapter)
    }

    private fun startActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(requireContext(), activityClass))
    }

    private fun setupDoubleBackPressToExit() {
        requireActivity().onBackPressedDispatcher.addCallback(
            getViewLifecycleOwner(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        requireActivity().finish()
                        return
                    } else {
                        Toast.makeText(context, "Click again to exit", Toast.LENGTH_SHORT).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        )
    }
}