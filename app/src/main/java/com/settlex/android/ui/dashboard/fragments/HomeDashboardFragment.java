package com.settlex.android.ui.dashboard.fragments;

import android.os.Bundle;
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
import com.settlex.android.databinding.FragmentDashboardHomeBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.adapter.PromotionalBannerAdapter;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.adapter.TransactionsAdapter;
import com.settlex.android.ui.dashboard.components.GridSpacingItemDecoration;
import com.settlex.android.ui.dashboard.model.ServiceUiModel;
import com.settlex.android.ui.dashboard.util.TxnIdGenerator;
import com.settlex.android.ui.dashboard.viewmodel.DashboardViewModel;
import com.settlex.android.util.event.Result;

import java.util.Arrays;
import java.util.List;

public class HomeDashboardFragment extends Fragment {
    private long backPressedTime;
    private String currentUserUid;
    private SettleXProgressBarController progressBarController;
    private FragmentDashboardHomeBinding binding;
    private DashboardViewModel dashboardViewModel;
    private AuthViewModel authViewModel;

    public HomeDashboardFragment() {
        // Required empty public constructor
    }

    // ========================== LIFECYCLE ============================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardHomeBinding.inflate(inflater, container, false);

        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observePayFriendResult();
    }

    // ======================= SETUP UI COMPONENTS =======================
    private void setupUiActions() {
        observeUserState();

        loadServices();
        loadPromotionalBanners();
        setupTxnRecyclerViewLayoutManager();
        setupDoubleBackToExit();

        binding.payAFriend.setOnClickListener(view -> dashboardViewModel.payFriend(
                currentUserUid,
                "benjamin213",
                TxnIdGenerator.generate("benjamin213"),
                150000,
                "SEND_MONEY",
                "Payment"
        ));
    }

    // ========================== OBSERVERS ============================
    private void observeUserState() {
        authViewModel.getUserState().observe(getViewLifecycleOwner(), userState -> {
            if (userState == null) {
                // Show logged out layout
                return;
            }
            // User is logged in fetch data
            currentUserUid = userState.getUid();
            observeAndDisplayUserData(currentUserUid);
            observeAndLoadRecentTransactions(currentUserUid);
        });
    }

    private void observeAndDisplayUserData(String uid) {
        dashboardViewModel.getUser(uid).observe(getViewLifecycleOwner(), userData -> {
            if (userData != null) {
                binding.userDisplayName.setText(userData.getUserFullName());
                binding.userBalance.setText(userData.getBalance());
                binding.userCommissionBalance.setText(userData.getCommissionBalance());
            }
        });
    }

    private void observeAndLoadRecentTransactions(String uid) {
        int MAX_TXN_DISPLAY = 5;
        dashboardViewModel.getTransactions(uid, MAX_TXN_DISPLAY).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                TransactionsAdapter adapter = new TransactionsAdapter(transactions);
                binding.transactionsRecyclerView.setAdapter(adapter);
                binding.transactionContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void observePayFriendResult() {
        dashboardViewModel.getPayFriendResult().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.hide();
                    case SUCCESS -> onPaySuccess();
                    case ERROR -> onPayFailure(result.getMessage());
                }
            }
        });
    }

    private void onPaySuccess() {
        Toast.makeText(requireContext(), "Payment success", Toast.LENGTH_LONG).show();
        progressBarController.hide();
    }

    private void onPayFailure(String error) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        progressBarController.hide();
    }

    // ======================== PREVIEW TOOLS (DELETE LATER) ==========================
    private void setupTxnRecyclerViewLayoutManager() {
        LinearLayoutManager txnLayoutManager = new LinearLayoutManager(getContext());
        txnLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.transactionsRecyclerView.setLayoutManager(txnLayoutManager);
    }

    private void loadServices() {
        binding.awareness.setSelected(true);

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

    private void loadPromotionalBanners() {
        List<Integer> promos = Arrays.asList(
                R.drawable.promo_banner,
                R.drawable.promo_banner,
                R.drawable.promo_banner
        );

        PromotionalBannerAdapter adapter = new PromotionalBannerAdapter(promos);
        binding.promoViewPager.setAdapter(adapter);

        // attach dots
        binding.dotsIndicator.attachTo(binding.promoViewPager);
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

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.gray_light));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}