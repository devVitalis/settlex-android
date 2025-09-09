package com.settlex.android.ui.dashboard.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.settlex.android.R;
import com.settlex.android.SettleXApp;
import com.settlex.android.data.remote.avater.AvatarService;
import com.settlex.android.databinding.FragmentDashboardHomeBinding;
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.common.util.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.activity.TransactionActivity;
import com.settlex.android.ui.dashboard.adapter.PromotionalBannerAdapter;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.adapter.TransactionsAdapter;
import com.settlex.android.ui.dashboard.components.GridSpacingItemDecoration;
import com.settlex.android.ui.dashboard.model.ServiceUiModel;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.ui.dashboard.viewmodel.PromoBannerViewModel;
import com.settlex.android.ui.dashboard.viewmodel.TransactionsViewModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.util.Arrays;
import java.util.List;

public class HomeDashboardFragment extends Fragment {
    private long backPressedTime;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private SettleXProgressBarController progressBarController;
    private FragmentDashboardHomeBinding binding;
    private UserViewModel userViewModel;
    private TransactionsViewModel transactionsViewModel;
    private PromoBannerViewModel promoBannerViewModel;

    public HomeDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardHomeBinding.inflate(inflater, container, false);

        userViewModel = ((SettleXApp) requireActivity().getApplication()).getSharedUserViewModel();
        transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionsViewModel.class);
        promoBannerViewModel = new ViewModelProvider(requireActivity()).get(PromoBannerViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();
        observeAndLoadPromoBanners();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
        binding = null;
    }

    // ======================= SETUP UI COMPONENTS =======================
    private void setupUiActions() {
        observeCurrentUserState();

        loadServices();
        setupTransactionsRecyclerView();
        setupDoubleBackToExit();

        binding.btnLogin.setOnClickListener(v -> startActivity(new Intent(requireActivity(), SignInActivity.class)));
        binding.addMoney.setOnClickListener(v -> userViewModel.signOut());
        binding.receiveMoney.setOnClickListener(v -> {
        });
        binding.payAFriend.setOnClickListener(v -> startActivity(new Intent(requireActivity(), TransactionActivity.class)));
    }

    //  OBSERVERS ===========
    private void observeCurrentUserState() {
        userViewModel.getAuthStateLiveData().observe(getViewLifecycleOwner(), currentUserUid -> {
            if (currentUserUid == null) {
                // User is logged out
                showLoggedOutLayout();
                return;
            }
            // User is logged in fetch data
            int TXN_QUERY_LIMIT = 3;
            observeAndDisplayUserData();
            transactionsViewModel.fetchTransactions(currentUserUid, TXN_QUERY_LIMIT);
            observeAndLoadRecentTransactions();
        });
    }

    private void observeAndDisplayUserData() {
        double MILLION_THRESHOLD = 999_999_999;
        userViewModel.getUserData().observe(getViewLifecycleOwner(), userData -> {
            if (userData == null) return;

            AvatarService.loadAvatar(userData.getUserFullName(), binding.userProfilePic);
            binding.userDisplayName.setText(userData.getUserFullName());
            binding.userBalance.setText((userData.getBalance() > MILLION_THRESHOLD) ? StringUtil.formatToNairaShort(userData.getBalance()) : StringUtil.formatToNaira(userData.getBalance()));
            binding.userCommissionBalance.setText(StringUtil.formatToNairaShort(userData.getCommissionBalance()));
        });
    }

    private void observeAndLoadRecentTransactions() {
        transactionsViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions.getStatus() == null) return;

            switch (transactions.getStatus()) {
                case LOADING -> caseTransactionLoading();
                case SUCCESS -> caseTransactionSuccess(transactions);
                case FAILED -> caseTransactionError();
            }
        });
    }

    private void caseTransactionLoading() {
        Log.d("Fragment", "Transaction history is loading");
        binding.transactionRecyclerView.setVisibility(View.GONE);
        binding.txnShimmerEffect.setVisibility(View.VISIBLE);
        binding.txnShimmerEffect.startShimmer();
    }

    private void caseTransactionSuccess(Result<List<TransactionUiModel>> transactions) {
        if (transactions.getData().isEmpty()) {
            Log.d("Fragment", "Transaction history is empty");
            binding.transactionRecyclerView.setVisibility(View.GONE);
            binding.txnShimmerEffect.stopShimmer();
            binding.txnShimmerEffect.setVisibility(View.GONE);
            return;
        }

        Log.d("Fragment", "Transaction history is not empty");
        TransactionsAdapter adapter = new TransactionsAdapter(transactions.getData());
        binding.transactionRecyclerView.setAdapter(adapter);
        binding.txnShimmerEffect.stopShimmer();
        binding.txnShimmerEffect.setVisibility(View.GONE);
        binding.transactionRecyclerView.setVisibility(View.VISIBLE);
    }

    private void caseTransactionError() {
        binding.transactionContainer.setVisibility(View.GONE);
    }

    private void observeAndLoadPromoBanners() {
        promoBannerViewModel.getPromoBanners().observe(getViewLifecycleOwner(), banner -> {
            if (banner == null || banner.isEmpty()) {
                binding.promoBannerContainer.setVisibility(View.GONE);
                return;
            }

            PromotionalBannerAdapter adapter = new PromotionalBannerAdapter(banner);
            binding.promoViewPager.setAdapter(adapter);

            // Attach dots
            binding.dotsIndicator.attachTo(binding.promoViewPager);
            setAutoScrollForPromoBanner(banner.size());
        });
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
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private void showLoggedOutLayout() {
        caseTransactionError();
        // Hide user balances

        binding.userBalance.setText(StringUtil.setAsterisks());
        binding.userCommissionBalance.setText(StringUtil.setAsterisks());
        binding.balanceToggle.setVisibility(View.GONE);
        binding.greetingContainer.setVisibility(View.GONE);
        binding.actionButtons.setVisibility(View.GONE);
        binding.brandAwareness.setSelected(false);
        binding.marqueeContainer.setVisibility(View.GONE);
        binding.btnLogin.setVisibility(View.VISIBLE);
    }

    private void setupTransactionsRecyclerView() {
        LinearLayoutManager txnLayoutManager = new LinearLayoutManager(getContext());
        txnLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.transactionRecyclerView.setLayoutManager(txnLayoutManager);
    }

    // ======================== PREVIEW TOOLS (DELETE LATER) ==========================
    private void loadServices() {
        binding.brandAwareness.setSelected(true);

        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 4);
        binding.serviceRecyclerView.setLayoutManager(layoutManager);
        // Set equal spacing
        int spacingInPixels = (int) (10 * getResources().getDisplayMetrics().density);
        binding.serviceRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4, spacingInPixels, true));

        List<ServiceUiModel> services = Arrays.asList(
                new ServiceUiModel("Airtime", R.drawable.ic_airtime),
                new ServiceUiModel("Data", R.drawable.ic_data),
                new ServiceUiModel("Betting", R.drawable.ic_betting),
                new ServiceUiModel("TV", R.drawable.ic_cable_tv),
                new ServiceUiModel("Electricity", R.drawable.ic_electricity),
                new ServiceUiModel("Internet", R.drawable.ic_internet),
                new ServiceUiModel("Gift Card", R.drawable.ic_gift_card),
                new ServiceUiModel("More", R.drawable.ic_more)
        );
        ServicesAdapter adapter = new ServicesAdapter(services);
        binding.serviceRecyclerView.setAdapter(adapter);
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

    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.gray_light));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}