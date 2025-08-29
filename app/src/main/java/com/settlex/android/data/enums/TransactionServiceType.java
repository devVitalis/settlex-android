package com.settlex.android.data.enums;

import com.settlex.android.R;

/**
 * Enum representing different transaction services with their associated icons
 */
public enum TransactionServiceType {
    ADD_MONEY("Wallet Funding", R.drawable.ic_money_received),
    SEND_MONEY("Pay a friend ", R.drawable.ic_money_sent),
    RECEIVE_MONEY("Received from ", R.drawable.ic_money_received),
    AIRTIME_RECHARGE("Airtime Recharge", R.drawable.ic_airtime),
    DATA_RECHARGE("Data Recharge", R.drawable.ic_data),
    CABLE_TV_SUBSCRIPTION("Cable TV Recharge", R.drawable.ic_cable_tv),
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