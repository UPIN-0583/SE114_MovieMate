package com.example.moviemate.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.moviemate.R;

import java.util.Objects;

public class CustomDialog {
    public static void showAlertDialog(Context context, int icon, String title, String message, Boolean closeActivityOnOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_layout, null);

        ImageView iconView = view.findViewById(R.id.dialog_icon);
        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView messageView = view.findViewById(R.id.dialog_content);
        Button okButton = view.findViewById(R.id.dialog_ok);

        iconView.setImageResource(icon);
        titleView.setText(title);
        messageView.setText(message);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (closeActivityOnOk) {
                ((Activity) context).finish();
            }
        });

        dialog.show();
    }
}