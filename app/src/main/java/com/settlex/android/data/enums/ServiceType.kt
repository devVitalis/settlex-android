package com.settlex.android.data.enums

import com.settlex.android.R
import com.settlex.android.presentation.dashboard.services.AirtimePurchaseActivity
import com.settlex.android.presentation.dashboard.services.BettingTopUpActivity
import com.settlex.android.presentation.dashboard.services.CableTvSubscriptionActivity
import com.settlex.android.presentation.dashboard.services.DataPurchaseActivity
import com.settlex.android.presentation.dashboard.services.model.ServiceDestination

enum class ServiceType(
    val displayName: String,
    val iconRes: Int,
    val cashbackPercentage: Int = 0,
    val label: String? = null,
    val transactionServiceType: TransactionServiceType,
    val destination: ServiceDestination? = null
) {
    AIRTIME(
        displayName = "Airtime",
        iconRes = R.drawable.ic_service_airtime,
        cashbackPercentage = 2,
        transactionServiceType = TransactionServiceType.AIRTIME_RECHARGE,
        destination = ServiceDestination(AirtimePurchaseActivity::class.java)
    ),
    DATA(
        displayName = "Data",
        iconRes = R.drawable.ic_service_data,
        cashbackPercentage = 6,
        transactionServiceType = TransactionServiceType.DATA_RECHARGE,
        destination = ServiceDestination(DataPurchaseActivity::class.java)
    ),
    BETTING(
        displayName = "Betting",
        iconRes = R.drawable.ic_service_betting,
        label = "Hot",
        transactionServiceType = TransactionServiceType.BETTING_TOPUP,
        destination = ServiceDestination(BettingTopUpActivity::class.java)
    ),
    TV(
        displayName = "TV",
        iconRes = R.drawable.ic_service_cable_tv,
        transactionServiceType = TransactionServiceType.CABLE_TV_SUBSCRIPTION,
        destination = ServiceDestination(CableTvSubscriptionActivity::class.java)
    ),
    ELECTRICITY(
        displayName = "Electricity",
        iconRes = R.drawable.ic_service_electricity,
        transactionServiceType = TransactionServiceType.ELECTRICITY_BILL
    ),
    INTERNET(
        displayName = "Internet",
        iconRes = R.drawable.ic_service_internet,
        transactionServiceType = TransactionServiceType.INTERNET
    ),
    GIFT_CARD(
        displayName = "Gift Card",
        iconRes = R.drawable.ic_service_gift_card,
        transactionServiceType = TransactionServiceType.GIFT_CARD
    ),
    MORE(
        displayName = "More",
        iconRes = R.drawable.ic_service_more,
        transactionServiceType = TransactionServiceType.MORE,
        destination = ServiceDestination(null, R.id.services_fragment)
    ),
    VOUCHER(
        displayName = "Voucher",
        iconRes = R.drawable.ic_service_voucher,
        transactionServiceType = TransactionServiceType.VOUCHER
    ),
    ESIM(
        displayName = "Esim",
        iconRes = R.drawable.ic_service_esim,
        transactionServiceType = TransactionServiceType.ESIM
    ),
    FLIGHT(
        displayName = "Flight",
        iconRes = R.drawable.ic_service_flight,
        transactionServiceType = TransactionServiceType.FLIGHT
    ),
    HOTEL(
        displayName = "Hotel",
        iconRes = R.drawable.ic_service_hotel,
        transactionServiceType = TransactionServiceType.HOTEL
    );
}