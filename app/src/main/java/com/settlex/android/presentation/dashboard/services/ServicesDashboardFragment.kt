package com.settlex.android.presentation.dashboard.services

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.settlex.android.R
import com.settlex.android.data.enums.ServiceType
import com.settlex.android.data.enums.TransactionServiceType
import com.settlex.android.databinding.FragmentDashboardServicesBinding
import com.settlex.android.presentation.dashboard.services.adapter.ServicesAdapter
import com.settlex.android.presentation.dashboard.services.model.ServiceDestination
import com.settlex.android.presentation.dashboard.services.model.ServiceUiModel
import com.settlex.android.util.ui.StatusBar

class ServicesDashboardFragment : Fragment() {
    private var _binding: FragmentDashboardServicesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardServicesBinding.inflate(inflater, container, false)
        Log.d("HomeDashboardFragment", " ServicesDashboardFragment onViewCreated - Fragment is alive")
        initViews()

        Log.d("HomeDashboardFragment", "onCreateView - binding=${binding != null}")
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.colorSurface)
        initTelecomRecyclerView()
        initEntertainmentRecyclerView()
        initUtilitiesRecyclerView()
        initTravelRecyclerView()
    }

    private fun setupRecyclerView(
        services: List<ServiceUiModel>,
        recyclerView: RecyclerView
    ) {
        val layoutManager = GridLayoutManager(context, 4)
        recyclerView.setLayoutManager(layoutManager)

        // Adapter with click handling
        val adapter = ServicesAdapter(false, services) { initServiceClickListener(it.destination) }
        recyclerView.setAdapter(adapter)
    }

    private fun initTelecomRecyclerView() = with(binding) {
        val services = ServiceType.entries
            .filter { serviceType ->
                serviceType.transactionServiceType == TransactionServiceType.AIRTIME_RECHARGE ||
                        serviceType.transactionServiceType == TransactionServiceType.DATA_RECHARGE ||
                        serviceType.transactionServiceType == TransactionServiceType.INTERNET
            }
            .map { serviceType ->
                ServiceUiModel(
                    serviceType.displayName,
                    serviceType.iconRes,
                    serviceType.cashbackPercentage,
                    serviceType.label,
                    serviceType.transactionServiceType,
                    serviceType.destination
                )
            }

        setupRecyclerView(services, telecomAndDigitalRecyclerView)
    }

    private fun initEntertainmentRecyclerView() = with(binding) {
        val services = ServiceType.entries
            .filter { serviceType ->
                serviceType.transactionServiceType == TransactionServiceType.CABLE_TV_SUBSCRIPTION ||
                        serviceType.transactionServiceType == TransactionServiceType.BETTING_TOPUP ||
                        serviceType.transactionServiceType == TransactionServiceType.VOUCHER ||
                        serviceType.transactionServiceType == TransactionServiceType.GIFT_CARD
            }
            .map { serviceType ->
                ServiceUiModel(
                    serviceType.displayName,
                    serviceType.iconRes,
                    serviceType.cashbackPercentage,
                    serviceType.label,
                    serviceType.transactionServiceType,
                    serviceType.destination
                )
            }

        setupRecyclerView(services, entertainmentRecyclerView)
    }

    private fun initUtilitiesRecyclerView() = with(binding) {
        val services = ServiceType.entries
            .filter { serviceType ->
                serviceType.transactionServiceType == TransactionServiceType.ELECTRICITY_BILL
            }
            .map { serviceType ->
                ServiceUiModel(
                    serviceType.displayName,
                    serviceType.iconRes,
                    serviceType.cashbackPercentage,
                    serviceType.label,
                    serviceType.transactionServiceType,
                    serviceType.destination
                )
            }
        setupRecyclerView(services, utilitiesAndBillsRecyclerView)
    }

    private fun initTravelRecyclerView() = with(binding) {
        val services = ServiceType.entries
            .filter { serviceType ->
                serviceType.transactionServiceType == TransactionServiceType.FLIGHT ||
                        serviceType.transactionServiceType == TransactionServiceType.HOTEL
            }
            .map { serviceType ->
                ServiceUiModel(
                    serviceType.displayName,
                    serviceType.iconRes,
                    serviceType.cashbackPercentage,
                    serviceType.label,
                    serviceType.transactionServiceType,
                    serviceType.destination
                )
            }

        setupRecyclerView(services, travelAndLifestyleRecyclerView)
    }

    private fun initServiceClickListener(destination: ServiceDestination?) {
        when (destination) {
            null -> Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT)
                .show()

            else -> {
                when {
                    destination.isActivity -> startActivity(
                        Intent(
                            context,
                            destination.activity
                        )
                    )

                    destination.isFragment -> {
                        val navController = NavHostFragment.findNavController(
                            this@ServicesDashboardFragment
                        )
                        navController.navigate(destination.navDestinationId!!)
                    }
                }
            }
        }
    }
}