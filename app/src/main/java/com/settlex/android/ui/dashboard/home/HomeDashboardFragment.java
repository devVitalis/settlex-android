package com.settlex.android.ui.dashboard.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.FragmentDashboardHomeBinding;
import com.settlex.android.ui.auth.login.LoginActivity;
import com.settlex.android.ui.dashboard.adapter.PromotionalBannerAdapter;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.adapter.TransactionsAdapter;
import com.settlex.android.ui.dashboard.model.PromoBannerUiModel;
import com.settlex.android.ui.dashboard.model.ServiceDestination;
import com.settlex.android.ui.dashboard.model.ServiceUiModel;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.services.AirtimePurchaseActivity;
import com.settlex.android.ui.dashboard.services.BettingTopUpActivity;
import com.settlex.android.ui.dashboard.services.CableTvSubscriptionActivity;
import com.settlex.android.ui.dashboard.services.DataPurchaseActivity;
import com.settlex.android.ui.dashboard.viewmodel.PromoBannerViewModel;
import com.settlex.android.ui.dashboard.viewmodel.TransactionViewModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.event.UiState;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.CurrencyFormatter;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeDashboardFragment extends Fragment {

    private final long MILLION_THRESHOLD_KOBO = 999_999_999L * 100;
    private long backPressedTime;

    // dependencies
    private FragmentDashboardHomeBinding binding;
    private TransactionsAdapter adapter;
    private UserViewModel userViewModel;
    private TransactionViewModel transactionViewModel;
    private boolean isConnected = false;
    private PromoBannerViewModel promoBannerViewModel;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;

    public HomeDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        promoBannerViewModel = new ViewModelProvider(requireActivity()).get(PromoBannerViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardHomeBinding.inflate(inflater, container, false);

        setupUiActions();
        loadAppServices();
        observeNetworkState();
        observeUserAuthState();
        observeAndLoadPromoBanners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearResources();
    }

    private void clearResources() {
        stopAutoScroll();
        binding = null;
    }

    // UI ACTIONS =======
    private void setupUiActions() {
        StatusBar.setColor(requireActivity(), R.color.gray_light);
        initTransactionRecyclerView();
        setupDoubleBackToExit();

        binding.btnProfilePic.setOnClickListener(v -> navigateToActivity(ProfileActivity.class));
        binding.btnLogin.setOnClickListener(v -> navigateToActivity(LoginActivity.class));
        binding.btnBalanceToggle.setOnClickListener(v -> userViewModel.toggleBalanceVisibility());
        binding.btnUserCommissionBalanceLayout.setOnClickListener(v -> navigateToActivity(CommissionWithdrawalActivity.class));
        binding.btnDeposit.setOnClickListener(v -> toggleBrandAwareness());
        binding.btnReceive.setOnClickListener(v -> navigateToActivity(ReceiveActivity.class));
        binding.btnTransfer.setOnClickListener(v -> navigateToActivity(WalletTransferActivity.class));
        binding.btnNotification.setOnClickListener(v -> StringFormatter.showNotImplementedToast(requireContext()));
        binding.btnSupport.setOnClickListener(v -> StringFormatter.showNotImplementedToast(requireContext()));
        binding.btnViewAllTransaction.setOnClickListener(v -> StringFormatter.showNotImplementedToast(requireContext()));
    }

    private void toggleBrandAwareness() {
        boolean isVisible = binding.marqueeContainer.getVisibility() == View.VISIBLE;
        binding.marqueeContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.marqueeTxt.setSelected(!isVisible);
    }

    //  OBSERVERS ======
    private void observeNetworkState() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            binding.connectionLost.setVisibility((!isConnected) ? View.VISIBLE : View.GONE);
            this.isConnected = isConnected;
        });
    }

    private void observeUserAuthState() {
        userViewModel.getAuthStateLiveData().observe(getViewLifecycleOwner(), uid -> {
            if (uid == null) {
                // logged out/session expired
                showLoggedOutLayout();
                return;
            }
            // user is logged in fetch data
            observeUserDataStatus();
            observeAndLoadRecentTransactions(uid);
        });
    }

    private void observeUserDataStatus() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            switch (user.status) {
                case LOADING -> onUserDataStatusLoading();
                case SUCCESS -> onUserDataStatusSuccess(user.data);
                case FAILURE -> onUserDataStatusError();
            }
        });
    }

    private void onUserDataStatusLoading() {
        // hide details
        binding.fullName.setVisibility(View.GONE);
        binding.userBalance.setVisibility(View.GONE);
        binding.btnUserCommissionBalanceLayout.setVisibility(View.GONE);

        // start and show shimmer
        binding.userFullNameShimmer.startShimmer();
        binding.userBalanceShimmer.startShimmer();
        binding.userCommissionBalanceShimmer.startShimmer();

        binding.userFullNameShimmer.setVisibility(View.VISIBLE);
        binding.userBalanceShimmer.setVisibility(View.VISIBLE);
        binding.userCommissionBalanceShimmer.setVisibility(View.VISIBLE);
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        hasPaymentId(user.getPaymentId());

        // dismiss shimmer
        binding.userFullNameShimmer.stopShimmer();
        binding.userBalanceShimmer.stopShimmer();
        binding.userCommissionBalanceShimmer.stopShimmer();

        binding.userFullNameShimmer.setVisibility(View.GONE);
        binding.userBalanceShimmer.setVisibility(View.GONE);
        binding.userCommissionBalanceShimmer.setVisibility(View.GONE);


        // show details
        binding.fullName.setVisibility(View.VISIBLE);
        binding.userBalance.setVisibility(View.VISIBLE);
        binding.btnUserCommissionBalanceLayout.setVisibility(View.VISIBLE);

        ProfileService.loadProfilePic(user.getPhotoUrl(), binding.btnProfilePic);
        binding.fullName.setText(user.getFullName());
        observeAndLoadUserPrefs(user.getBalance(), user.getCommissionBalance());
    }

    private void hasPaymentId(java.lang.String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) {
            navigateToActivity(CreatePaymentIdActivity.class);
        }
    }

    private void onUserDataStatusError() {
        // dismiss shimmer
        binding.userFullNameShimmer.stopShimmer();
        binding.userBalanceShimmer.stopShimmer();
        binding.userCommissionBalanceShimmer.stopShimmer();

        binding.userFullNameShimmer.setVisibility(View.GONE);
        binding.userBalanceShimmer.setVisibility(View.GONE);
        binding.userCommissionBalanceShimmer.setVisibility(View.GONE);

        // display error : system busy
    }

    private void observeAndLoadUserPrefs(long balance, long commissionBalance) {
        userViewModel.getBalanceHiddenLiveData().observe(getViewLifecycleOwner(), hidden -> {
            if (hidden) {
                // balance hidden set asterisk
                binding.btnBalanceToggle.setImageResource(R.drawable.ic_visibility_off);
                binding.userBalance.setText(StringFormatter.setAsterisks());
                binding.userCommissionBalance.setText(StringFormatter.setAsterisks());
                return;
            }
            // show balance
            binding.btnBalanceToggle.setImageResource(R.drawable.ic_visibility_on);
            binding.userBalance.setText((balance > MILLION_THRESHOLD_KOBO) ? CurrencyFormatter.formatToNairaShort(balance) : CurrencyFormatter.formatToNaira(balance));
            binding.userCommissionBalance.setText(CurrencyFormatter.formatToNairaShort(commissionBalance));
        });
    }

    private void observeAndLoadRecentTransactions(java.lang.String uid) {
        transactionViewModel.fetchTransactionsLiveData(uid, 2).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null) return;

            switch (transactions.status) {
                case LOADING -> onTransactionStatusLoading();
                case SUCCESS -> onTransactionStatusSuccess(transactions);
                case FAILURE -> onTransactionStatusError();
            }
        });
    }

    private void onTransactionStatusLoading() {
        binding.emptyTransactionsState.setVisibility(View.GONE);
        binding.txnRecyclerView.setVisibility(View.GONE);
        binding.txnShimmerEffect.setVisibility(View.VISIBLE);
        binding.txnShimmerEffect.startShimmer();
    }

    private void onTransactionStatusSuccess(UiState<List<TransactionUiModel>> transactions) {
        if (transactions.data.isEmpty()) {
            // zero transaction history
            binding.txnShimmerEffect.stopShimmer();
            binding.txnShimmerEffect.setVisibility(View.GONE);

            // clear recyclerview
            adapter.submitList(Collections.emptyList());
            binding.txnRecyclerView.setAdapter(adapter);

            binding.btnViewAllTransaction.setVisibility(View.GONE);
            binding.emptyTransactionsState.setVisibility(View.VISIBLE);
            return;
        }

        // transaction exists
        adapter.submitList(transactions.data);
        binding.txnRecyclerView.setAdapter(adapter);

        binding.txnShimmerEffect.stopShimmer();
        binding.txnShimmerEffect.setVisibility(View.GONE);
        binding.emptyTransactionsState.setVisibility(View.GONE);
        binding.txnRecyclerView.setVisibility(View.VISIBLE);
    }

    private void onTransactionStatusError() {
        // show error state
        binding.emptyTransactionsState.setVisibility(View.VISIBLE);
    }

    private void onItemTransactionClick() {
        adapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(requireContext(), TransactionActivity.class);
            intent.putExtra("transaction", transaction);
            startActivity(intent);
        });
    }

    private void observeAndLoadPromoBanners() {
        promoBannerViewModel.getPromoBanners().observe(getViewLifecycleOwner(), banner -> {

            if (banner == null) {
                binding.promoBannerContainer.setVisibility(View.GONE);
                return;
            }

            switch (banner.status) {
                case LOADING -> onPromoBannerLoading();
                case SUCCESS -> onPromoBannersSuccess(banner.data);
            }
        });
    }

    private void onPromoBannerLoading() {
        binding.promoProgressBar.setVisibility(View.VISIBLE);
        binding.promoProgressBar.show();
    }

    private void onPromoBannersSuccess(List<PromoBannerUiModel> banner) {
        binding.promoProgressBar.hide();
        binding.promoProgressBar.setVisibility(View.GONE);

        if (banner.isEmpty()) {
            binding.promoBannerContainer.setVisibility(View.GONE);
            return;
        }

        PromotionalBannerAdapter adapter = new PromotionalBannerAdapter(banner);
        binding.promoViewPager.setAdapter(adapter);
        binding.promoBannerContainer.setVisibility(View.VISIBLE);

        // Attach dots
        binding.dotsIndicator.attachTo(binding.promoViewPager);
        setAutoScrollForPromoBanner(banner.size());
    }

    private void setAutoScrollForPromoBanner(int size) {
        if (size <= 1) return;

        autoScrollRunnable = new Runnable() {
            int currentPosition = 0;

            @Override
            public void run() {
                if (binding.promoViewPager.getAdapter() == null) return;

                currentPosition = (currentPosition + 1) % size; // loop back to 0
                binding.promoViewPager.setCurrentItem(currentPosition, true);

                // schedule next slide
                autoScrollHandler.postDelayed(this, 4000);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 4000);
    }

    private void stopAutoScroll() {
        // onDestroyView
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private void showLoggedOutLayout() {
        // Hide all logged-in UI elements
        binding.btnProfilePic.setVisibility(View.GONE);
        binding.marqueeContainer.setVisibility(View.GONE);
        binding.btnBalanceToggle.setVisibility(View.GONE);
        binding.greetingContainer.setVisibility(View.GONE);
        binding.actionButtons.setVisibility(View.GONE);
        binding.txnContainer.setVisibility(View.GONE);
        binding.userBalance.setText(StringFormatter.setAsterisks());
        binding.userCommissionBalance.setText(StringFormatter.setAsterisks());

        // Show the logged-out UI elements
        binding.btnLogin.setVisibility(View.VISIBLE);
    }

    private void initTransactionRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.txnRecyclerView.setLayoutManager(layoutManager);

        adapter = new TransactionsAdapter();
        onItemTransactionClick();
    }

    private void loadAppServices() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 4);
        binding.serviceRecyclerView.setLayoutManager(layoutManager);

        List<ServiceUiModel> services = Arrays.asList(
                new ServiceUiModel("Airtime", R.drawable.ic_service_airtime, 2, TransactionServiceType.AIRTIME_RECHARGE),
                new ServiceUiModel("Data", R.drawable.ic_service_data, 6, TransactionServiceType.DATA_RECHARGE),
                new ServiceUiModel("Betting", R.drawable.ic_service_betting, "Hot", TransactionServiceType.BETTING_TOPUP),
                new ServiceUiModel("TV", R.drawable.ic_service_cable_tv, TransactionServiceType.CABLE_TV_SUBSCRIPTION),
                new ServiceUiModel("Electricity", R.drawable.ic_service_electricity, TransactionServiceType.ELECTRICITY_BILL),
                new ServiceUiModel("Internet", R.drawable.ic_service_internet, TransactionServiceType.INTERNET),
                new ServiceUiModel("Gift Card", R.drawable.ic_service_gift_card, TransactionServiceType.GIFT_CARD),
                new ServiceUiModel("More", R.drawable.ic_service_more, TransactionServiceType.MORE)
        );

        // Map services to destinations
        Map<TransactionServiceType, ServiceDestination> serviceMap = new HashMap<>();
        serviceMap.put(TransactionServiceType.AIRTIME_RECHARGE, new ServiceDestination(AirtimePurchaseActivity.class));
        serviceMap.put(TransactionServiceType.DATA_RECHARGE, new ServiceDestination(DataPurchaseActivity.class));
        serviceMap.put(TransactionServiceType.BETTING_TOPUP, new ServiceDestination(BettingTopUpActivity.class));
        serviceMap.put(TransactionServiceType.CABLE_TV_SUBSCRIPTION, new ServiceDestination(CableTvSubscriptionActivity.class));
        serviceMap.put(TransactionServiceType.ELECTRICITY_BILL, null);
        serviceMap.put(TransactionServiceType.INTERNET, null);
        serviceMap.put(TransactionServiceType.GIFT_CARD, null);
        serviceMap.put(TransactionServiceType.MORE, new ServiceDestination(R.id.servicesFragment));

        ServicesAdapter adapter = new ServicesAdapter(false, services, serviceUiModel -> {
            ServiceDestination serviceDestination = serviceMap.get(serviceUiModel.getType());
            if (serviceDestination == null) {
                StringFormatter.showNotImplementedToast(requireContext());
                return;
            }

            if (serviceDestination.isActivity()) {
                navigateToActivity(serviceDestination.getActivity());
            } else {
                navigateToFragment(serviceDestination.getNavDestinationId());
            }
        });

        // Set adapter
        binding.serviceRecyclerView.setAdapter(adapter);
    }

    private void navigateToActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    private void navigateToFragment(int navigationId) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(navigationId);
    }

    private void setupDoubleBackToExit() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    requireActivity().finish();
                    return;
                } else {
                    Toast.makeText(requireActivity(), "Click again to exit", Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
    }
}