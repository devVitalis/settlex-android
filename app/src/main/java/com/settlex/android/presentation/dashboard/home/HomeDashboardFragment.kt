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
import com.settlex.android.util.string.CurrencyFormatter
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
    private var adapter: TransactionListAdapter? = null

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
        observeUserState()
        observeRecentTransactions()
        observePromotionalBanners()
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.gray_light)
        setupListeners()
        setupDoubleBackPressToExit()
        initTransactionRecyclerView()
    }

    private fun comingSoon() {
        StringFormatter.showNotImplementedToast(
            requireContext()
        )
    }

    private fun toggleBrandAwareness() {
        val isVisible = binding.marqueeContainer.isVisible
        binding.marqueeContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.marqueeTxt.isSelected = !isVisible
    }

    private fun setupListeners() = with(binding) {
        btnProfilePic.setOnClickListener {
            startActivity(
                ProfileActivity::class.java
            )
        }

        btnLogin.setOnClickListener {
            startActivity(
                LoginActivity::class.java
            )
        }

        btnUserCommissionBalanceLayout.setOnClickListener {
            startActivity(
                CommissionWithdrawalActivity::class.java
            )
        }

        btnReceive.setOnClickListener {
            startActivity(
                ReceiveActivity::class.java
            )
        }

        btnTransfer.setOnClickListener { v: View? ->
            startActivity(
                TransferToFriendActivity::class.java
            )
        }

        btnNotification.setOnClickListener { comingSoon() }
        btnSupport.setOnClickListener { comingSoon() }
        btnViewAllTransaction.setOnClickListener { comingSoon() }
        btnDeposit.setOnClickListener { toggleBrandAwareness() }
        btnBalanceToggle.setOnClickListener { /** userViewModel.toggleBalanceVisibility() */ }
    }

    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> {
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

    private fun showUserLoadingState() {
        with(binding) {
            // Hide details
            fullName.gone()
            userBalance.gone()
            btnUserCommissionBalanceLayout.gone()

            // Start shimmer
            userFullNameShimmer.startShimmer()
            userBalanceShimmer.startShimmer()
            userCommissionBalanceShimmer.startShimmer()

            userFullNameShimmer.show()
            userBalanceShimmer.show()
            userCommissionBalanceShimmer.show()
        }
    }

    private fun onUserDataReceived(user: HomeUiModel) {
        with(binding) {
            if (user.paymentId == null) startActivity(CreatePaymentIdActivity::class.java)

            // Dismiss shimmer
            userFullNameShimmer.stopShimmer()
            userBalanceShimmer.stopShimmer()
            userCommissionBalanceShimmer.stopShimmer()

            userFullNameShimmer.visibility = View.GONE
            userBalanceShimmer.visibility = View.GONE
            userCommissionBalanceShimmer.visibility = View.GONE

            // Show details
            fullName.visibility = View.VISIBLE
            userBalance.visibility = View.VISIBLE
            btnUserCommissionBalanceLayout.visibility = View.VISIBLE

            ProfileService.loadProfilePic(user.photoUrl, btnProfilePic)
            fullName.text = user.fullName
            observeAndLoadUserPrefs(user.balance, user.commissionBalance)
        }
    }

    private fun handleUserErrorState() {
        // Dismiss shimmer
        with(binding) {
            userFullNameShimmer.stopShimmer()
            userBalanceShimmer.stopShimmer()
            userCommissionBalanceShimmer.stopShimmer()

            userFullNameShimmer.visibility = View.GONE
            userBalanceShimmer.visibility = View.GONE
            userCommissionBalanceShimmer.visibility = View.GONE
        }
    }

    private fun observeAndLoadUserPrefs(balance: Long, commissionBalance: Long) {
        with(binding) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.isBalanceHidden.collect { isHidden ->
                        if (isHidden) {
                            // balance hidden set asterisk
                            btnBalanceToggle.setImageResource(R.drawable.ic_visibility_off)
                            userBalance.text = StringFormatter.setAsterisks()
                            userCommissionBalance.text = StringFormatter.setAsterisks()
                            return@collect
                        }
                        // show balance
                        btnBalanceToggle.setImageResource(R.drawable.ic_visibility_on)
                        userBalance.text =
                            if (balance > MILLION_THRESHOLD_KOBO) CurrencyFormatter.formatToNairaShort(
                                balance
                            ) else CurrencyFormatter.formatToNaira(balance)
                        userCommissionBalance.text = CurrencyFormatter.formatToNairaShort(
                            commissionBalance
                        )
                    }
                }
            }
        }
    }

    private fun observeRecentTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentTransactions.collect { transactions ->
                    when (transactions) {
                        is UiState.Loading -> onTransactionsLoading()
                        is UiState.Success -> showTransactions(transactions.data)
                        is UiState.Failure -> onTransactionsError()
                    }
                }
            }
        }
    }

    private fun onTransactionsLoading() {
        with(binding) {
            emptyTransactionsState.gone()
            txnRecyclerView.gone()
            txnShimmerEffect.show()
            txnShimmerEffect.startShimmer()
        }
    }


    private fun showTransactions(transactions: List<TransactionItemUiModel>?) {
        with(binding) {
            if (transactions?.isEmpty() == true) {
                // zero transaction history
                txnShimmerEffect.stopShimmer()
                txnShimmerEffect.visibility = View.GONE

                // clear recyclerview
                adapter!!.submitList(mutableListOf())
                txnRecyclerView.setAdapter(adapter)

                btnViewAllTransaction.visibility = View.GONE
                emptyTransactionsState.visibility = View.VISIBLE
                return
            }

            // Transaction exists
            adapter!!.submitList(transactions)
            txnRecyclerView.setAdapter(adapter)

            txnShimmerEffect.stopShimmer()
            txnShimmerEffect.gone()
            emptyTransactionsState.gone()
            txnRecyclerView.show()
        }
    }

    private fun onTransactionsError() = with(binding) {
        emptyTransactionsState.show()
    }

    private fun onItemTransactionClick() {
        adapter!!.setOnTransactionClickListener { transaction: TransactionItemUiModel? ->
            val intent = Intent(requireContext(), TransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            startActivity(intent)
        }
    }

    private fun observePromotionalBanners() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bannerViewModel.banners.collect { banner ->
                    when (banner) {
                        is UiState.Loading -> onPromoBannerLoading()
                        is UiState.Success -> onPromoBannersSuccess(banner.data)
                        is UiState.Failure -> {
                            binding!!.promoProgressBar.visibility = View.VISIBLE
                            binding!!.promoProgressBar.show()
                        }
                    }
                }
            }
        }
    }

    private fun onPromoBannerLoading() = with(binding) {
        promoProgressBar.visibility = View.VISIBLE
        promoProgressBar.show()
    }

    private fun onPromoBannersSuccess(banner: MutableList<PromoBannerUiModel>?) {
        with(binding) {
            promoProgressBar.hide()
            promoProgressBar.visibility = View.GONE

            if (banner?.isEmpty() ?: return) {
                promoBannerContainer.visibility = View.GONE
                return
            }

            val adapter = PromotionalBannerAdapter(banner)
            bannerViewPager.setAdapter(adapter)
            promoBannerContainer.visibility = View.VISIBLE

            // Attach dots
            dotsIndicator.attachTo(bannerViewPager)
            setAutoScrollForPromoBanner(banner.size)
        }
    }

    private fun setAutoScrollForPromoBanner(size: Int) = with(binding) {
        if (size <= 1) return@with

        autoScrollRunnable = object : Runnable {
            var currentPosition: Int = 0

            override fun run() {
                if (bannerViewPager.adapter == null) return

                currentPosition = (currentPosition + 1) % size // loop back to 0
                bannerViewPager.setCurrentItem(currentPosition, true)

                // schedule next slide
                autoScrollHandler.postDelayed(this, 4000)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, 4000)
    }

    private fun showUnauthenticatedState() = with(binding) {
        // Hide all logged-in UI elements
        btnProfilePic.gone()
        marqueeContainer.gone()
        btnBalanceToggle.gone()
        greetingContainer.gone()
        actionButtons.gone()
        txnContainer.gone()
        userBalance.text = StringFormatter.setAsterisks()
        userCommissionBalance.text = StringFormatter.setAsterisks()

        // Show the logged-out UI elements
        btnLogin.show()
    }

    private fun initTransactionRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        binding.txnRecyclerView.setLayoutManager(layoutManager)

        adapter = TransactionListAdapter()
        onItemTransactionClick()
    }

    private fun initAppServices() {
        val layoutManager = GridLayoutManager(requireContext(), 4)
        binding.serviceRecyclerView.setLayoutManager(layoutManager)

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

        // Set adapter
        binding.serviceRecyclerView.setAdapter(adapter)
    }

    private fun startActivity(activityClass: Class<out Activity?>?) {
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

    companion object {
        private const val MILLION_THRESHOLD_KOBO = 999999999L * 100
    }
}