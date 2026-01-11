package com.settlex.android.util.string;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    /**
     * Converts large amounts to readable short forms, e.g. ₦2.5K / ₦3.2M / ₦1.1B
     */
    public static java.lang.String formatToNairaShort(long amountInKobo) {
        java.lang.String symbol = "₦";
        DecimalFormat df = new DecimalFormat("#.##");
        BigDecimal naira = BigDecimal.valueOf(amountInKobo).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (naira.compareTo(BigDecimal.valueOf(1_000)) < 0) {
            return symbol + naira.toPlainString(); // ₦999.99
        } else if (naira.compareTo(BigDecimal.valueOf(1_000_000)) < 0) {
            BigDecimal thousands = naira.divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP);
            return symbol + df.format(thousands) + "K";
        } else if (naira.compareTo(BigDecimal.valueOf(1_000_000_000)) < 0) {
            BigDecimal millions = naira.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP);
            return symbol + df.format(millions) + "M";
        } else {
            BigDecimal billions = naira.divide(BigDecimal.valueOf(1_000_000_000), 1, RoundingMode.HALF_UP);
            return symbol + df.format(billions) + "B";
        }
    }

    /**
     * Formats a BigDecimal into local currency string without ₦ symbol
     */
    public static String formatToCurrency(BigDecimal number) {
        Locale NIG_LOCAL = Locale.forLanguageTag("en-NG");
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(NIG_LOCAL);

        String formattedAmount = numberFormatter.format(number);
        String symbol = Objects.requireNonNull(numberFormatter.getCurrency()).getSymbol(NIG_LOCAL);

        return formattedAmount.replace(symbol, "").trim();
    }
}
