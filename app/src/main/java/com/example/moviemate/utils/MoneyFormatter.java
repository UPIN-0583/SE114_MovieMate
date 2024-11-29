package com.example.moviemate.utils;

import android.content.Context;

import com.example.moviemate.R;

import java.text.NumberFormat;
import java.util.Objects;

public class MoneyFormatter {
    public static int parseMoney(Context context, String moneyString) {
        NumberFormat formatter = NumberFormat.getInstance();

        try {
            Number number = formatter.parse(moneyString);
            return Objects.requireNonNull(number).intValue();
        } catch (Exception e) {
            CustomDialog.showAlertDialog(context, R.drawable.ic_error, "Error", "Invalid money format", false);
            return Integer.parseInt(moneyString);
        }
    }
    public static String formatMoney(Context context, int money) {
        NumberFormat formatter = NumberFormat.getInstance();

        try {
            return formatter.format(money);
        } catch (Exception e) {
            CustomDialog.showAlertDialog(context, R.drawable.ic_error, "Error", "Invalid money format", false);
            return String.valueOf(money);
        }
    }
}
