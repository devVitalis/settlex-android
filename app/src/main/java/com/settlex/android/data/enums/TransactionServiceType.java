package com.settlex.android.data.enums;

import com.settlex.android.R;

/**
 * Enum representing different transaction services with their associated icons
 */
public enum TransactionServiceType {
    ADD_MONEY("Wallet Funding", R.drawable.ic_money_sent),  //TODO: update with icon
    PAY_A_FRIEND("Transfer sent", R.drawable.ic_money_sent),
    AIRTIME_RECHARGE("Airtime Recharge", R.drawable.ic_airtime),
    DATA_RECHARGE("Data Recharge", R.drawable.ic_data),
    CABLE_TV_SUBSCRIPTION("Cable TV Subscription", R.drawable.ic_cable_tv),
    ELECTRICITY_BILL("Electricity Bill Payment", R.drawable.ic_electricity),
    BETTING_TOPUP("Betting Topup", R.drawable.ic_betting),
    VOUCHER("Redeem Voucher" ,R.drawable.ic_voucher);

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