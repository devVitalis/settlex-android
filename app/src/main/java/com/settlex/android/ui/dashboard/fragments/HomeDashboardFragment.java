package com.settlex.android.ui.dashboard.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.common.util.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.activity.TransactionActivity;
import com.settlex.android.ui.dashboard.adapter.PromotionalBannerAdapter;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.adapter.TransactionsAdapter;
import com.settlex.android.ui.dashboard.components.GridSpacingItemDecoration;
import com.settlex.android.ui.dashboard.model.ServiceUiModel;
import com.settlex.android.ui.dashboard.viewmodel.TransactionsViewModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.StringUtil;

import java.util.Arrays;
import java.util.List;

public class HomeDashboardFragment extends Fragment {
    private long backPressedTime;
    private String currentUserUid;
    private SettleXProgressBarController progressBarController;
    private FragmentDashboardHomeBinding binding;
    private UserViewModel userViewModel;
    private TransactionsViewModel transactionsViewModel;

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

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionsViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    // ======================= SETUP UI COMPONENTS =======================
    private void setupUiActions() {
        observeUserState();

        loadServices();
        loadPromotionalBanners();
        setupTxnRecyclerViewLayoutManager();
        setupDoubleBackToExit();

        binding.payAFriend.setOnClickListener(v -> startActivity(new Intent(requireActivity(), TransactionActivity.class)));
        binding.addMoney.setOnClickListener(v -> userViewModel.signOut());
    }

    // ========================== OBSERVERS ============================
    private void observeUserState() {
        userViewModel.getAuthStateLiveData().observe(getViewLifecycleOwner(), authState -> {
            if (authState == null) {
                // Show logged out layout
                onNoLoggedUser();
                return;
            }
            // User is logged in fetch data
            currentUserUid = authState.getUid();
            observeAndDisplayUserData(currentUserUid);
            observeAndLoadRecentTransactions(currentUserUid);
        });
    }

    private void observeAndDisplayUserData(String uid) {
        double MILLION_THRESHOLD = 999_999_999;
        userViewModel.getUserData(uid).observe(getViewLifecycleOwner(), userData -> {
            if (userData != null) {
                binding.userDisplayName.setText(userData.getUserFullName());
                binding.userBalance.setText((userData.getBalance() > MILLION_THRESHOLD) ? StringUtil.formatToNairaShort(userData.getBalance()) : StringUtil.formatToNaira(userData.getBalance()));
                binding.userCommissionBalance.setText(StringUtil.formatToNairaShort(userData.getCommissionBalance()));
            }
        });
    }

    private void observeAndLoadRecentTransactions(String uid) {
        int MAX_TXN_DISPLAY = 3;
        transactionsViewModel.getTransactions(uid, MAX_TXN_DISPLAY).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                TransactionsAdapter adapter = new TransactionsAdapter(transactions);
                binding.transactionsRecyclerView.setAdapter(adapter);
                binding.transactionContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onNoLoggedUser() {
        Toast.makeText(requireContext(), "Session expired", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireActivity(), SignInActivity.class));
        requireActivity().finishAffinity();
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