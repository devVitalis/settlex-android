package com.settlex.android.data.enums;

import com.settlex.android.R;

/**
 * Enum representing different transaction services with their associated icons
 */
public enum TransactionServiceType {
    ADD_FUNDS("Funds Added", R.drawable.ic_service_payment_sent),  //TODO: update with icon
    PAY_A_FRIEND("Payment Sent", R.drawable.ic_service_payment_sent),
    AIRTIME_RECHARGE("Airtime Recharge", R.drawable.ic_service_airtime),
    DATA_RECHARGE("Data Recharge", R.drawable.ic_service_data),
    CABLE_TV_SUBSCRIPTION("Cable TV Subscription", R.drawable.ic_service_cable_tv),
    ELECTRICITY_BILL("Electricity Bill Payment", R.drawable.ic_service_electricity),
    BETTING_TOPUP("Betting Top-up", R.drawable.ic_service_betting),
    INTERNET("Internet Subscription", R.drawable.ic_service_internet),
    GIFT_CARD("Gift Card Purchase", R.drawable.ic_service_gift_card),
    VOUCHER("Redeem Voucher" ,R.drawable.ic_service_voucher),
    MORE("More" ,R.drawable.ic_service_more),
    ESIM("Esim" ,R.drawable.ic_service_esim),
    FLIGHT("Flight Booking" ,R.drawable.ic_service_flight),
    HOTEL("Hotel" ,R.drawable.ic_service_hotel);

    private final String displayName;
    private final int iconRes;

    TransactionServiceType(String displayName, int iconRes) {
        this.iconRes = iconRes;
        this.displayName = displayName;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getDisplayName() {
        return displayName;
    }
}