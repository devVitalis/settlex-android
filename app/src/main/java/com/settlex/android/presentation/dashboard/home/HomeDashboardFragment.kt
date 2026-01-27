package com.settlex.android.presentation.dashboard.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.settlex.android.presentation.transactions.TransactionHistoryActivity
import com.settlex.android.presentation.transactions.TransferToFriendActivity
import com.settlex.android.presentation.transactions.adapter.TransactionListAdapter
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.presentation.wallet.CommissionWithdrawalActivity
import com.settlex.android.presentation.wallet.ReceiveActivity
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeDashboardFragment : Fragment() {
    private var backPressedTime: Long = 0
    private var bannerScrollJob: Job? = null
    private var hasFetchRecentTransactions = false

    private var _binding: FragmentDashboardHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionsListAdapter: TransactionListAdapter
    private lateinit var promotionalBannerAdapter: PromotionalBannerAdapter

    private val navController by lazy { NavHostFragment.findNavController(this) }
    private val viewModel: HomeViewModel by activityViewModels()
    private val bannerViewModel: PromoBannerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        StatusBar.setColor(requireActivity(), R.color.colorSurfaceDim)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeDashboardFragment", "Fragment is destroyed")
        bannerScrollJob?.cancel()
        binding.rvTransactions.adapter = null
        binding.rvServices.adapter = null
        _binding = null
    }

    private fun initObservers() {
        observeUserSessionWithBalance()
        observeUserBalanceHiddenState()
        observeUserRecentTransactions()
        observePromotionalBanners()
    }

    private fun initViews() {
        initListeners()
        initAppServices()
        initTransactionList()
        initPromoBannersList()
        setupDoubleBackPressToExit()
    }

    private fun initListeners() = with(binding) {
        btnReceive.setOnClickListener { startActivity(ReceiveActivity::class.java) }
        ivProfilePhoto.setOnClickListener { startActivity(ProfileActivity::class.java) }
        btnLogin.setOnClickListener { startActivity(LoginActivity::class.java) }
        btnTransfer.setOnClickListener { startActivity(TransferToFriendActivity::class.java) }
        btnNotification.setOnClickListener { requireContext().toastNotImplemented() }
        btnSupport.setOnClickListener { requireContext().toastNotImplemented() }
        tvViewAllTransaction.setOnClickListener { startActivity(TransactionHistoryActivity::class.java) }
        btnDeposit.setOnClickListener { requireContext().toastNotImplemented() }
        ivBalanceToggle.setOnClickListener { viewModel.toggleBalanceVisibility() }
        btnRefreshTransactions.setOnClickListener { viewModel.fetchRecentTransactions() }
        viewUserCommissionBalance.setOnClickListener { startActivity(CommissionWithdrawalActivity::class.java) }
    }

    private fun observeUserSessionWithBalance() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> {
                            if (hasFetchRecentTransactions.not()) {
                                viewModel.fetchRecentTransactions()
                                hasFetchRecentTransactions = true
                            }
                            onUserDataReceived(state.user)
                        }

                        is UserSessionState.Loading -> showUserLoadingState()
                        is UserSessionState.UnAuthenticated -> showUnauthenticatedState()
                        is UserSessionState.Error -> handleUserErrorState()
                    }
                }
            }
        }

        // Collect balance in same scope to batch UI updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userBalance.collect { balance ->
                    val (userBalance, commissionBalance) = balance ?: return@collect
                    binding.tvUserBalance.text = userBalance
                    binding.tvUserCommissionBalance.text = commissionBalance
                }
            }
        }
    }

    private fun showUserLoadingState() = with(binding) {
        listOf(tvUserFullName, tvUserBalance, viewUserCommissionBalance).forEach { it.gone() }
        listOf(
            shimmerUserFullName,
            shimmerUserBalance,
            shimmerUserCommissionBalance
        ).forEach { it.show() }
    }

    private fun onUserDataReceived(user: HomeUiModel) = with(binding) {
        if (user.paymentId == null) {
            startActivity(CreatePaymentIdActivity::class.java)
            return@with
        }

        listOf(
            shimmerUserFullName,
            shimmerUserBalance,
            shimmerUserCommissionBalance
        ).forEach { it.gone() }
        listOf(tvUserFullName, tvUserBalance, viewUserCommissionBalance).forEach { it.show() }

        ProfileService.loadProfilePhoto(user.photoUrl, ivProfilePhoto)
        tvUserFullName.text = user.fullName
    }

    private fun handleUserErrorState() {}

    private fun showUnauthenticatedState() = with(binding) {
        listOf(
            shimmerUserFullName, shimmerUserBalance,
            shimmerUserCommissionBalance, ivProfilePhoto,
            ivBalanceToggle, viewGreetingContainer,
            viewPaymentButtons, viewTransactionContainer,
            viewRecentTransactionsLabel,
        ).forEach { it.gone() }

        listOf(tvUserBalance, tvUserCommissionBalance).forEach { it.setAsterisks() }
        listOf(tvUserBalance, viewUserCommissionBalance).forEach { it.show() }

        listOf(
            btnReceive, ivProfilePhoto, btnLogin,
            btnTransfer, btnNotification, btnSupport,
            btnDeposit, ivBalanceToggle,
            viewUserCommissionBalance,
        ).forEach { it.setOnClickListener { startActivity(LoginActivity::class.java) } }

        btnLogin.show()
    }

    private fun observeUserBalanceHiddenState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBalanceHidden.collect { isBalanceHidden ->
                    binding.ivBalanceToggle.isSelected = isBalanceHidden
                }
            }
        }
    }

    private fun initTransactionList() = with(binding) {
        transactionsListAdapter =
            TransactionListAdapter(object : TransactionListAdapter.OnTransactionClickListener {
                override fun onClick(transaction: TransactionItemUiModel) {
                    val intent = Intent(context, TransactionActivity::class.java)
                    intent.putExtra("transaction", transaction)
                    startActivity(intent)
                }
            })

        rvTransactions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = transactionsListAdapter
            setHasFixedSize(true)
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
        listOf(viewNoTransactionsUi, viewNoInternet, rvTransactions).forEach { it.gone() }
        shimmerTransactions.show()
    }

    private fun setTransactionsData(transactions: List<TransactionItemUiModel>?) =
        with(binding) {
            listOf(shimmerTransactions, viewNoInternet).forEach { it.gone() }

            if (transactions?.isEmpty() == true) {
                transactionsListAdapter.submitList(emptyList())
                viewNoTransactionsUi.show()
            } else {
                transactionsListAdapter.submitList(transactions)
                viewNoTransactionsUi.gone()
                rvTransactions.show()
            }
        }

    private fun onTransactionsError(error: AppException) = with(binding) {
        listOf(shimmerTransactions, viewNoTransactionsUi, rvTransactions).forEach { it.gone() }

        if (error is AppException.NetworkException) {
            viewNoInternet.show()
        }
    }

    private fun initPromoBannersList() = with(binding) {
        promotionalBannerAdapter = PromotionalBannerAdapter(emptyList())
        vpPromotionaBanner.setAdapter(promotionalBannerAdapter)
        dotsIndicator.attachTo(vpPromotionaBanner)
    }

    private fun observePromotionalBanners() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            bannerViewModel.banners.collect { banner ->
                when (banner) {
                    is UiState.Loading -> binding.pbPromoBanner.show()
                    is UiState.Success -> onPromoBannersSuccess(banner.data)
                    is UiState.Failure -> binding.pbPromoBanner.gone()
                }
            }
        }
    }

    private fun onPromoBannersSuccess(bannerList: List<PromoBannerUiModel>) = with(binding) {
        pbPromoBanner.gone()

        if (bannerList.isEmpty()) {
            viewPromoBannerContainer.gone()
            return@with
        }

        promotionalBannerAdapter.updatePromoBanners(bannerList)
        viewPromoBannerContainer.show()
        startAutoScroll(bannerList.size)
    }

    private fun startAutoScroll(bannerSize: Int) {
        if (bannerSize <= 1 || bannerScrollJob?.isActive == true) return

        bannerScrollJob?.cancel()
        bannerScrollJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(4000)
                val nextPosition = (binding.vpPromotionaBanner.currentItem + 1) % bannerSize
                binding.vpPromotionaBanner.setCurrentItem(nextPosition, true)
            }
        }
    }

    private fun initAppServices() {
        binding.rvServices.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = ServicesAdapter(true, viewModel.homeServiceList, ::onServiceClicked)
            setHasFixedSize(true)
        }
    }

    private fun onServiceClicked(serviceUiModel: ServiceUiModel) {
        when (val destination = serviceUiModel.destination) {
            null -> requireContext().toastNotImplemented()
            else -> {
                when {
                    destination.isActivity -> startActivity(destination.activity)
                    destination.isFragment -> navController.navigate(destination.navDestinationId!!)
                }
            }
        }
    }

    private fun startActivity(activityClass: Class<out Activity>?) {
        startActivity(Intent(requireContext(), activityClass))
    }

    private fun setupDoubleBackPressToExit() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        requireActivity().finish()
                        return
                    }
                    Toast.makeText(context, "Click again to exit", Toast.LENGTH_SHORT).show()
                    backPressedTime = System.currentTimeMillis()
                }
            }
        )
    }
}