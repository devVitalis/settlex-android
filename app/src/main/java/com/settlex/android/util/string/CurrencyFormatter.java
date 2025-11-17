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
     * Converts Kobo (long) to a formatted Naira currency string, e.g. 123450 → ₦1,234.50
     */
    public static java.lang.String formatToNaira(long amountInKobo) {
        BigDecimal kobo = BigDecimal.valueOf(amountInKobo);
        BigDecimal naira = kobo.divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        return formatter.format(naira);
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
    public static java.lang.String formatToCurrency(BigDecimal number) {
        Locale NIG_LOCAL = Locale.forLanguageTag("en-NG");
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(NIG_LOCAL);

        java.lang.String formattedAmount = numberFormatter.format(number);
        java.lang.String symbol = Objects.requireNonNull(numberFormatter.getCurrency()).getSymbol(NIG_LOCAL);

        return formattedAmount.replace(symbol, "").trim();
    }

    /**
     * Converts a Naira string like "120.50" to its equivalent Kobo value (12050L)
     */
    public static long convertNairaStringToKobo(java.lang.String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) {
            return 0L;
        }
        try {
            BigDecimal amount = new BigDecimal(amountString.trim());
            BigDecimal amountInKobo = amount.multiply(new BigDecimal("100"));

            // Round to nearest whole Kobo
            BigDecimal roundedKobo = amountInKobo.setScale(0, RoundingMode.HALF_UP);

            return roundedKobo.longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            return 0L;
        }
    }
}
