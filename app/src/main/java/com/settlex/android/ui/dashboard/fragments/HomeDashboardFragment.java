package com.settlex.android.ui.dashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentDashboardHomeBinding;
import com.settlex.android.ui.dashboard.adapter.PromoAdapter;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.adapter.TransactionsAdapter;
import com.settlex.android.ui.dashboard.components.GridSpacingItemDecoration;
import com.settlex.android.ui.dashboard.model.ServiceModel;
import com.settlex.android.ui.dashboard.model.TransactionModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeDashboardFragment extends Fragment {
    private long backPressedTime;

    private FragmentDashboardHomeBinding binding;

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

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    // ========================== SETUP UI COMPONENTS ============================
    private void setupUiActions() {
        setupDoubleBackToExit();
        loadServices();
        loadPromoBanners();
        loadRecentTransactions();
    }


    // ========================== PREVIEW TOOLS (DELETE LATER) ============================
    private void loadServices() {
        binding.awareness.setSelected(true);

        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 4);
        binding.serviceRecyclerView.setLayoutManager(layoutManager);
        // Set equal spacing
        int spacingInPixels = (int) (10 * getResources().getDisplayMetrics().density);
        binding.serviceRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4, spacingInPixels, true));

        List<ServiceModel> services = Arrays.asList(
                new ServiceModel("Airtime", R.drawable.ic_airtime),
                new ServiceModel("Data", R.drawable.ic_data),
                new ServiceModel("Betting", R.drawable.ic_betting),
                new ServiceModel("TV", R.drawable.ic_tv),
                new ServiceModel("Electricity", R.drawable.ic_electricity),
                new ServiceModel("Internet", R.drawable.ic_internet),
                new ServiceModel("Gift Card", R.drawable.ic_gift_card),
                new ServiceModel("More", R.drawable.ic_more)
        );
        ServicesAdapter adapter = new ServicesAdapter(services);
        binding.serviceRecyclerView.setAdapter(adapter);
    }

    private void loadRecentTransactions() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.transactionsRecyclerView.setLayoutManager(layoutManager);

        List<TransactionModel> transactions = Arrays.asList(
                new TransactionModel("Add Money", "credit", 200000, "Success", new Date().getTime()),
                new TransactionModel("Send Money", "debit/send", 3000, "Success", new Date().getTime()),
                new TransactionModel("Receive Money", "received", 1567.34, "Success", new Date().getTime())
        );
        TransactionsAdapter adapter = new TransactionsAdapter(transactions);
        binding.transactionsRecyclerView.setAdapter(adapter);
    }

    private void loadPromoBanners() {
        // Example images in drawable
        List<Integer> promos = Arrays.asList(
                R.drawable.promo_banner,
                R.drawable.promo_banner,
                R.drawable.promo_banner,
                R.drawable.promo_banner
        );

        PromoAdapter adapter = new PromoAdapter(promos);
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