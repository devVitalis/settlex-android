package com.settlex.android.presentation.dashboard.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionServiceType
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.FragmentDashboardHomeBinding
import com.settlex.android.presentation.auth.login.LoginActivity
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.account.ProfileActivity
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import com.settlex.android.presentation.dashboard.home.viewmodel.HomeViewModel
import com.settlex.android.presentation.dashboard.home.viewmodel.PromoBannerViewModel
import com.settlex.android.presentation.dashboard.services.AirtimePurchaseActivity
import com.settlex.android.presentation.dashboard.services.BettingTopUpActivity
import com.settlex.android.presentation.dashboard.services.CableTvSubscriptionActivity
import com.settlex.android.presentation.dashboard.services.DataPurchaseActivity
import com.settlex.android.presentation.dashboard.services.adapter.ServicesAdapter
import com.settlex.android.presentation.dashboard.services.adapter.ServicesAdapter.onItemClickedListener
import com.settlex.android.presentation.dashboard.services.model.ServiceDestination
import com.settlex.android.presentation.dashboard.services.model.ServiceUiModel
import com.settlex.android.presentation.settings.CreatePaymentIdActivity
import com.settlex.android.presentation.transactions.TransactionActivity
import com.settlex.android.presentation.transactions.TransferToFriendActivity
import com.settlex.android.presentation.transactions.adapter.TransactionListAdapter
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.presentation.wallet.CommissionWithdrawalActivity
import com.settlex.android.presentation.wallet.ReceiveActivity
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeDashboardFragment : Fragment() {
    private var backPressedTime: Long = 0
    private var autoScrollRunnable: Runnable? = null
    private val autoScrollHandler = Handler(Looper.getMainLooper())

    // dependencies
    private var _binding: FragmentDashboardHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TransactionListAdapter

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
        observeUserSessionState()
        observeUserBalance()
        observeUserBalanceHiddenState()
        observeUserRecentTransactions()
        observePromotionalBanners()
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.gray_light)
        initListeners()
        initTransactionRecyclerView()
        setupDoubleBackPressToExit()
    }

    private fun comingSoon() {
        // TODO: remove
        StringFormatter.showNotImplementedToast(
            requireContext()
        )
    }

    private fun toggleBrandAwareness() {
        // TODO: remove once brand awareness is implemented
        val isVisible = binding.viewMarqueeContainer.isVisible
        binding.viewMarqueeContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.marqueeTxt.isSelected = !isVisible
    }

    private fun initListeners() = with(binding) {
        btnReceive.setOnClickListener { startActivity(ReceiveActivity::class.java) }
        ivProfilePhoto.setOnClickListener { startActivity(ProfileActivity::class.java) }
        btnLogin.setOnClickListener { startActivity(LoginActivity::class.java) }
        btnTransfer.setOnClickListener { startActivity(TransferToFriendActivity::class.java) }
        btnNotification.setOnClickListener { comingSoon() }
        btnSupport.setOnClickListener { comingSoon() }
        btnViewAllTransaction.setOnClickListener { comingSoon() }
        btnDeposit.setOnClickListener { toggleBrandAwareness() }
        ivBalanceToggle.setOnClickListener { /** userViewModel.toggleBalanceVisibility() */ }

        viewUserCommissionBalance.setOnClickListener {
            startActivity(CommissionWithdrawalActivity::class.java)
        }
    }

    private fun initTransactionRecyclerView() = with(binding) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        rvTransactions.setLayoutManager(layoutManager)

        adapter = TransactionListAdapter()
        setUpTransactionClickListener()
    }

    private fun setUpTransactionClickListener() {
        adapter.setOnTransactionClickListener { transaction: TransactionItemUiModel ->
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            startActivity(intent)
        }
    }

    private fun observeUserSessionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> {
                            // Fetch recent transactions
                            viewModel.loadRecentTransactions(state.user.uid)
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

        ProfileService.loadProfilePic(user.photoUrl, ivProfilePhoto)
        tvUserFullName.text = user.fullName
    }

    private fun handleUserErrorState() = with(binding) {

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
                        is UiState.Failure -> onTransactionsError()
                    }
                }
            }
        }
    }

    private fun onTransactionsLoading() = with(binding) {
        listOf(
            viewNoTransactionsUi,
            rvTransactions
        ).forEach { it.gone() }

        shimmerTransactions.show()
    }

    private fun setTransactionsData(transactions: List<TransactionItemUiModel>?) = with(binding) {
        shimmerTransactions.gone()
        when (transactions?.isEmpty()) {
            true -> {
                // Clear recyclerview
                adapter.submitList(emptyList())
                rvTransactions.setAdapter(adapter)

                btnViewAllTransaction.gone()
                viewNoTransactionsUi.show()
            }

            else -> {
                // Show transactions
                adapter.submitList(transactions)
                rvTransactions.setAdapter(adapter)

                viewNoTransactionsUi.gone()
                rvTransactions.show()
            }
        }
    }

    private fun onTransactionsError() = with(binding) {
        viewNoTransactionsUi.show()
    }

    private fun observePromotionalBanners() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bannerViewModel.banners.collect { banner ->
                    when (banner) {
                        is UiState.Loading -> shimmerPromoBanner.show()
                        is UiState.Success -> onPromoBannersSuccess(banner.data)
                        is UiState.Failure -> shimmerPromoBanner.gone()
                    }
                }
            }
        }
    }

    private fun onPromoBannersSuccess(banner: List<PromoBannerUiModel>) = with(binding) {
        shimmerPromoBanner.gone()

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
                Log.d("Home", " Item size $size")
                Log.d("Home", " Current position $currentPosition")
                bannerViewPager.setCurrentItem(currentPosition, true)

                // Schedule next slide
                autoScrollHandler.postDelayed(this, 4000)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, 4000)
    }

    private fun showUnauthenticatedState() = with(binding) {
        // Hide all authenticated UI elements
        listOf(
            ivProfilePhoto, viewMarqueeContainer,
            ivBalanceToggle, viewGreetingContainer,
            viewPaymentButtons, viewTransactionContainer
        ).forEach { it.gone() }

        tvUserBalance.text = StringFormatter.setAsterisks()
        tvUserCommissionBalance.text = StringFormatter.setAsterisks()

        // Show the unauthenticated UI elements
        btnLogin.show()
    }

    private fun initAppServices() = with(binding) {
        val layoutManager = GridLayoutManager(context, 4)
        rvServices.setLayoutManager(layoutManager)

        val services = listOf(
            ServiceUiModel(
                "Airtime",
                R.drawable.ic_service_airtime,
                2,
                TransactionServiceType.AIRTIME_RECHARGE
            ),
            ServiceUiModel(
                "Data",
                R.drawable.ic_service_data,
                6,
                TransactionServiceType.DATA_RECHARGE
            ),
            ServiceUiModel(
                "Betting",
                R.drawable.ic_service_betting,
                "Hot",
                TransactionServiceType.BETTING_TOPUP
            ),
            ServiceUiModel(
                "TV",
                R.drawable.ic_service_cable_tv,
                TransactionServiceType.CABLE_TV_SUBSCRIPTION
            ),
            ServiceUiModel(
                "Electricity",
                R.drawable.ic_service_electricity,
                TransactionServiceType.ELECTRICITY_BILL
            ),
            ServiceUiModel(
                "Internet",
                R.drawable.ic_service_internet,
                TransactionServiceType.INTERNET
            ),
            ServiceUiModel(
                "Gift Card",
                R.drawable.ic_service_gift_card,
                TransactionServiceType.GIFT_CARD
            ),
            ServiceUiModel("More", R.drawable.ic_service_more, TransactionServiceType.MORE)
        )

        // Map services to destinations
        val serviceMap: MutableMap<TransactionServiceType, ServiceDestination?> = HashMap()
        serviceMap.put(
            TransactionServiceType.AIRTIME_RECHARGE,
            ServiceDestination(AirtimePurchaseActivity::class.java)
        )
        serviceMap.put(
            TransactionServiceType.DATA_RECHARGE,
            ServiceDestination(DataPurchaseActivity::class.java)
        )
        serviceMap.put(
            TransactionServiceType.BETTING_TOPUP,
            ServiceDestination(BettingTopUpActivity::class.java)
        )
        serviceMap.put(
            TransactionServiceType.CABLE_TV_SUBSCRIPTION,
            ServiceDestination(CableTvSubscriptionActivity::class.java)
        )
        serviceMap.put(TransactionServiceType.ELECTRICITY_BILL, null)
        serviceMap.put(TransactionServiceType.INTERNET, null)
        serviceMap.put(TransactionServiceType.GIFT_CARD, null)
        serviceMap.put(TransactionServiceType.MORE, ServiceDestination(R.id.services_fragment))

        val adapter = ServicesAdapter(
            false,
            services,
            onItemClickedListener { serviceUiModel: ServiceUiModel? ->
                val serviceDestination = serviceMap[serviceUiModel!!.type]

                if (serviceDestination == null) {
                    StringFormatter.showNotImplementedToast(requireContext())
                    return@onItemClickedListener
                } else if (serviceDestination.isActivity) {
                    startActivity(serviceDestination.activity)
                } else {
                    navigateToFragment(serviceDestination.navDestinationId)
                }
            })

        rvServices.setAdapter(adapter)
    }

    private fun startActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(requireContext(), activityClass))
    }

    private fun navigateToFragment(navigationId: Int) {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(navigationId)
    }

    private fun setupDoubleBackPressToExit() {
        requireActivity().onBackPressedDispatcher.addCallback(
            getViewLifecycleOwner(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        requireActivity().finish()
                        return
                    } else {
                        Toast.makeText(requireActivity(), "Click again to exit", Toast.LENGTH_SHORT)
                            .show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            })
    }
}