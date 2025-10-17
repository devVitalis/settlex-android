package com.settlex.android.ui.dashboard.fragment.services;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.databinding.FragmentDashboardServicesBinding;
import com.settlex.android.ui.dashboard.adapter.ServicesAdapter;
import com.settlex.android.ui.dashboard.components.GridSpacingItemDecoration;
import com.settlex.android.ui.dashboard.model.ServiceDestination;
import com.settlex.android.ui.dashboard.model.ServiceUiModel;
import com.settlex.android.ui.dashboard.fragment.home.activity.services.AirtimePurchaseActivity;
import com.settlex.android.ui.dashboard.fragment.home.activity.services.BettingTopUpActivity;
import com.settlex.android.ui.dashboard.fragment.home.activity.services.CableTvSubscriptionActivity;
import com.settlex.android.ui.dashboard.fragment.home.activity.services.DataPurchaseActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicesDashboardFragment extends Fragment {
    private FragmentDashboardServicesBinding binding;
    private ServiceMapper serviceMapper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardServicesBinding.inflate(inflater, container, false);
        serviceMapper = new ServiceMapper();

        setupUiActions();
        setupTelecomRecyclerView();
        setupEntertainmentRecyclerView();
        setupUtilitiesRecyclerView();
        setupTravelRecyclerView();

        return binding.getRoot();
    }

    private void setupUiActions() {
        binding.btnBackBefore.setOnClickListener(view -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupRecyclerView(List<ServiceUiModel> services, androidx.recyclerview.widget.RecyclerView recyclerView) {
        // Grid layout with 4 columns
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        // Set equal spacing
        int spacingInPixels = (int) (5 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, spacingInPixels, true));

        // Adapter with click handling
        ServicesAdapter adapter = new ServicesAdapter(true, services, serviceUiModel -> {
            ServiceDestination destination = serviceMapper.getDestination(serviceUiModel.getType());
            handleServiceClick(destination);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupTelecomRecyclerView() {
        List<ServiceUiModel> services = Arrays.asList(
                new ServiceUiModel("Airtime", R.drawable.ic_service_airtime, 0,TransactionServiceType.AIRTIME_RECHARGE),
                new ServiceUiModel("Data", R.drawable.ic_service_data, 0, TransactionServiceType.DATA_RECHARGE),
                new ServiceUiModel("Internet", R.drawable.ic_service_internet, 0, TransactionServiceType.INTERNET),
                new ServiceUiModel("Esim", R.drawable.ic_service_esim, 0, TransactionServiceType.ESIM)
        );
        setupRecyclerView(services, binding.telecomAndDigitalRecyclerView);
    }

    private void setupEntertainmentRecyclerView() {
        List<ServiceUiModel> services = Arrays.asList(
                new ServiceUiModel("TV", R.drawable.ic_service_cable_tv, 0, TransactionServiceType.CABLE_TV_SUBSCRIPTION),
                new ServiceUiModel("Betting", R.drawable.ic_service_betting, 0, TransactionServiceType.BETTING_TOPUP),
                new ServiceUiModel("Voucher", R.drawable.ic_service_voucher, 0, TransactionServiceType.VOUCHER),
                new ServiceUiModel("Gift Card", R.drawable.ic_service_gift_card, 0, TransactionServiceType.GIFT_CARD)
        );
        setupRecyclerView(services, binding.entertainmentRecyclerView);
    }

    private void setupUtilitiesRecyclerView() {
        List<ServiceUiModel> services = List.of(
                new ServiceUiModel("Electricity", R.drawable.ic_service_electricity, 0, TransactionServiceType.ELECTRICITY_BILL)
        );
        setupRecyclerView(services, binding.utilitiesAndBillsRecyclerView);
    }

    private void setupTravelRecyclerView() {
        List<ServiceUiModel> services = Arrays.asList(
                new ServiceUiModel("Flight", R.drawable.ic_service_flight, 0, TransactionServiceType.FLIGHT),
                new ServiceUiModel("Hotel", R.drawable.ic_service_hotel, 0, TransactionServiceType.HOTEL)
        );
        setupRecyclerView(services, binding.travelAndLifestyleRecyclerView);
    }

    private void handleServiceClick(ServiceDestination destination) {
        if (destination == null) {
            Toast.makeText(requireContext(), "Feature not yet implemented", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destination.isActivity()) {
            startActivity(new Intent(requireContext(), destination.getActivity()));
        } else if (destination.isFragment()) {
            navigateToFragment(destination.getNavDestinationId());
        }
    }

    private void navigateToFragment(int navigationResId) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(navigationResId);
    }

    // Inner class for mapping responsibility
    private static class ServiceMapper {
        private final Map<TransactionServiceType, ServiceDestination> serviceMap = new HashMap<>();

        ServiceMapper() {
            serviceMap.put(TransactionServiceType.AIRTIME_RECHARGE, new ServiceDestination(AirtimePurchaseActivity.class));
            serviceMap.put(TransactionServiceType.DATA_RECHARGE, new ServiceDestination(DataPurchaseActivity.class));
            serviceMap.put(TransactionServiceType.BETTING_TOPUP, new ServiceDestination(BettingTopUpActivity.class));
            serviceMap.put(TransactionServiceType.CABLE_TV_SUBSCRIPTION, new ServiceDestination(CableTvSubscriptionActivity.class));
            serviceMap.put(TransactionServiceType.ELECTRICITY_BILL, null);
            serviceMap.put(TransactionServiceType.INTERNET, null);
            serviceMap.put(TransactionServiceType.ESIM, null);
            serviceMap.put(TransactionServiceType.FLIGHT, null);
            serviceMap.put(TransactionServiceType.HOTEL, null);
            serviceMap.put(TransactionServiceType.VOUCHER, null);
            serviceMap.put(TransactionServiceType.GIFT_CARD, null);
        }

        ServiceDestination getDestination(TransactionServiceType type) {
            return serviceMap.get(type);
        }
    }
}