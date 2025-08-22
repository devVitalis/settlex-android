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

import com.bumptech.glide.Glide;
import com.settlex.android.R;
import com.settlex.android.ui.dashboard.model.ServicesModel;
import com.settlex.android.databinding.FragmentDashboardHomeBinding;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;

import java.util.Arrays;
import java.util.List;

public class HomeDashboardFragment extends Fragment {
    private long backPressedTime;

    private FragmentDashboardHomeBinding binding;


    public HomeDashboardFragment() {
        // Required empty public constructor
    }

    // =============================== LIFECYCLE ===================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardHomeBinding.inflate(inflater, container, false);

        setupStatusBar();
        loadServices();
        setupDoubleBackToExit();

        binding.awareness.setSelected(true);

        return binding.getRoot();
    }

    private void loadServices() {
        // 4 columns grid
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 4);
        binding.servicesRecyclerView.setLayoutManager(layoutManager);

        List<ServicesModel> services = Arrays.asList(
                new ServicesModel("Airtime", R.drawable.ic_airtime),
                new ServicesModel("Data", R.drawable.ic_data),
                new ServicesModel("Betting", R.drawable.ic_betting),
                new ServicesModel("TV", R.drawable.ic_tv),
                new ServicesModel("Electricity", R.drawable.ic_electricity),
                new ServicesModel("Internet", R.drawable.ic_internet),
                new ServicesModel("Flight", R.drawable.ic_flight),
                new ServicesModel("Gift Card", R.drawable.ic_gift_card),
                new ServicesModel("Esim", R.drawable.ic_esim),
                new ServicesModel("Education", R.drawable.ic_education),
                new ServicesModel("Hotel", R.drawable.ic_hotel),
                new ServicesModel("Voucher", R.drawable.ic_redeem_voucher),
                new ServicesModel("More", R.drawable.ic_more)
        );

        ServicesAdapter adapter = new ServicesAdapter(services);
        binding.servicesRecyclerView.setAdapter(adapter);

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
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.gray_whitish_blue));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}