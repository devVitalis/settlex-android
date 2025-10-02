package com.settlex.android.ui.dashboard.fragments.dashboard;

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
import com.settlex.android.data.remote.avater.AvatarService;
import com.settlex.android.databinding.FragmentDashboardHomeBinding;
import com.settlex.android.ui.auth.activity.LoginActivity;
import com.settlex.android.ui.dashboard.activity.CommissionWithdrawalActivity;
import com.settlex.android.ui.dashboard.activity.ReceiveMoneyActivity;
import com.settlex.android.ui.dashboard.activity.TransactionActivity;
import com.settlex.android.ui.dashboard.activity.TransactionDetailsActivity;
import com.settlex.android.ui.dashboard.adapter.PromotionalBannerAdapter;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.adapter.TransactionsAdapter;
import com.settlex.android.ui.dashboard.components.GridSpacingItemDecoration;
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
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeDashboardFragment extends Fragment {

    // Dependencies
    private FragmentDashboardHomeBinding binding;
    private PromoBannerViewModel promoBannerViewModel;
    private UserViewModel userViewModel;
    private TransactionsAdapter adapter;
    private boolean isConnected = false;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;

    // Instance vars
    private final double MILLION_THRESHOLD = 999_999_999;
    private long backPressedTime;

    public HomeDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
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

    // UI ACTIONS =============
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.gray_light);
        initTransactionRecyclerView();
        setupDoubleBackToExit();

        binding.btnLogin.setOnClickListener(v -> navigateToActivity(LoginActivity.class));
        binding.btnBalanceToggle.setOnClickListener(v -> userViewModel.toggleBalanceVisibility());
        binding.btnUserCommissionBalanceLayout.setOnClickListener(v -> navigateToActivity(CommissionWithdrawalActivity.class));
        binding.btnAddMoney.setOnClickListener(v -> userViewModel.signOut());
        binding.btnReceiveMoney.setOnClickListener(v -> navigateToActivity(ReceiveMoneyActivity.class));
        binding.btnPayAFriend.setOnClickListener(v -> navigateToActivity(TransactionActivity.class));
        binding.btnNotification.setOnClickListener(v -> Toast.makeText(requireContext(), "This feature is not yet implemented", Toast.LENGTH_SHORT).show());
        binding.btnSupport.setOnClickListener(v -> Toast.makeText(requireContext(), "This feature is not yet implemented", Toast.LENGTH_SHORT).show());
        binding.btnViewAllTransaction.setOnClickListener(v -> Toast.makeText(requireContext(), "This feature is not yet implemented", Toast.LENGTH_SHORT).show());
    }

    //  OBSERVERS ===========
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
            observeAndDisplayUserData();
            observeAndLoadRecentTransactions(uid);
        });
    }

    private void observeAndDisplayUserData() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }

            switch (user.getStatus()) {
                case LOADING -> onUserDataLoading();
                case SUCCESS -> onUserDataSuccess(user.getData());
                case ERROR -> onUserDataError();
            }
        });
    }

    private void onUserDataLoading() {
        // hide details
        binding.userFullName.setVisibility(View.GONE);
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

    private void onUserDataSuccess(UserUiModel user) {
        // dismiss shimmer
        binding.userFullNameShimmer.stopShimmer();
        binding.userBalanceShimmer.stopShimmer();
        binding.userCommissionBalanceShimmer.stopShimmer();

        binding.userFullNameShimmer.setVisibility(View.GONE);
        binding.userBalanceShimmer.setVisibility(View.GONE);
        binding.userCommissionBalanceShimmer.setVisibility(View.GONE);


        // show details
        binding.userFullName.setVisibility(View.VISIBLE);
        binding.userBalance.setVisibility(View.VISIBLE);
        binding.btnUserCommissionBalanceLayout.setVisibility(View.VISIBLE);

        AvatarService.loadAvatar(user.getUserFullName(), binding.userProfilePic);
        binding.userFullName.setText(user.getUserFullName());
        loadUserPrefs(user.getBalance(), user.getCommissionBalance());
    }

    private void onUserDataError() {
        // dismiss shimmer
        binding.userFullNameShimmer.stopShimmer();
        binding.userBalanceShimmer.stopShimmer();
        binding.userCommissionBalanceShimmer.stopShimmer();

        binding.userFullNameShimmer.setVisibility(View.GONE);
        binding.userBalanceShimmer.setVisibility(View.GONE);
        binding.userCommissionBalanceShimmer.setVisibility(View.GONE);

        // display error : system busy
    }

    private void loadUserPrefs(double balance, double commissionBalance) {
        userViewModel.getIsBalanceHiddenLiveData().observe(getViewLifecycleOwner(), hidden -> {
            if (hidden) {
                // balance hidden set asterisk
                binding.btnBalanceToggle.setImageResource(R.drawable.ic_visibility_off);
                binding.userBalance.setText(StringUtil.setAsterisks());
                binding.userCommissionBalance.setText(StringUtil.setAsterisks());
                return;
            }
            // show balance
            binding.btnBalanceToggle.setImageResource(R.drawable.ic_visibility_on);
            binding.userBalance.setText((balance > MILLION_THRESHOLD) ? StringUtil.formatToNairaShort(balance) : StringUtil.formatToNaira(balance));
            binding.userCommissionBalance.setText(StringUtil.formatToNairaShort(commissionBalance));
        });
    }

    private void observeAndLoadRecentTransactions(String uid) {
        userViewModel.getRecentTransactionLiveData(uid, 2).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null) return;

            switch (transactions.getStatus()) {
                case LOADING -> onTransactionLoading();
                case SUCCESS -> onTransactionSuccess(transactions);
                case ERROR -> onTransactionError();
            }
        });
    }

    private void onTransactionLoading() {
        binding.transactionRecyclerView.setVisibility(View.GONE);
        binding.txnShimmerEffect.setVisibility(View.VISIBLE);
        binding.txnShimmerEffect.startShimmer();
    }

    private void onTransactionSuccess(Result<List<TransactionUiModel>> transactions) {
        if (transactions.getData().isEmpty()) {
            // zero transaction history
            binding.txnShimmerEffect.stopShimmer();
            binding.txnShimmerEffect.setVisibility(View.GONE);
            binding.transactionContainer.setVisibility(View.GONE);
            return;
        }

        // transaction exists
        adapter.submitList(transactions.getData());
        binding.transactionRecyclerView.setAdapter(adapter);

        binding.txnShimmerEffect.stopShimmer();
        binding.txnShimmerEffect.setVisibility(View.GONE);
        binding.transactionRecyclerView.setVisibility(View.VISIBLE);
    }

    private void onTransactionError() {
        // show error state
        binding.transactionContainer.setVisibility(View.GONE);
    }

    private void onItemTransactionClick() {
        adapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(requireContext(), TransactionDetailsActivity.class);
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

            switch (banner.getStatus()) {
                case LOADING -> onPromoBannerLoading();
                case SUCCESS -> onPromoBannersSuccess(banner.getData());
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
        binding.marqueeTxt.setSelected(false);
        binding.marqueeContainer.setVisibility(View.GONE);
        binding.btnBalanceToggle.setVisibility(View.GONE);
        binding.greetingContainer.setVisibility(View.GONE);
        binding.actionButtons.setVisibility(View.GONE);
        binding.moneyFlowContainer.setVisibility(View.GONE);
        binding.transactionContainer.setVisibility(View.GONE);

        // Explicitly reset the user data UI components
        binding.userFullName.setText("");
        binding.userBalance.setText(StringUtil.setAsterisks());
        binding.userCommissionBalance.setText(StringUtil.setAsterisks());

        // Show the logged-out UI elements
        binding.btnLogin.setVisibility(View.VISIBLE);
    }

    private void initTransactionRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.transactionRecyclerView.setLayoutManager(layoutManager);

        adapter = new TransactionsAdapter();
        onItemTransactionClick();
    }

    private void loadAppServices() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 4);
        binding.serviceRecyclerView.setLayoutManager(layoutManager);
        // Set equal spacing
        int spacingInPixels = (int) (10 * getResources().getDisplayMetrics().density);
        binding.serviceRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4, spacingInPixels, true));

        List<ServiceUiModel> services = Arrays.asList(
                new ServiceUiModel("Airtime", R.drawable.ic_service_airtime, TransactionServiceType.AIRTIME_RECHARGE),
                new ServiceUiModel("Data", R.drawable.ic_service_data, TransactionServiceType.DATA_RECHARGE),
                new ServiceUiModel("Betting", R.drawable.ic_service_betting, TransactionServiceType.BETTING_TOPUP),
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
                Toast.makeText(requireContext(), "Feature not yet implemented", Toast.LENGTH_SHORT).show();
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