package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.moviemate.R;
import com.example.moviemate.models.PaymentMethod;

import java.util.List;

public class PaymentMethodListAdapter extends ArrayAdapter<PaymentMethod> {
    int resource;
    ConstraintLayout currentSelected = null;

    public PaymentMethodListAdapter(Context context, int resource, List<PaymentMethod> paymentMethods) {
        super(context, resource, paymentMethods);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null)
            v = LayoutInflater.from(getContext()).inflate(resource, parent, false);

        PaymentMethod paymentMethod = getItem(position);
        if (paymentMethod == null)
            return v;

        ImageView paymentMethodIcon = v.findViewById(R.id.logoPaymentImageView);
        paymentMethodIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), getItem(position).getImageResource()));
        TextView paymentMethodName = v.findViewById(R.id.paymentNameTextView);
        paymentMethodName.setText(paymentMethod.getName());

        ConstraintLayout paymentMethodLayout = v.findViewById(R.id.paymentMethod);
        paymentMethodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSelected != null)
                    currentSelected.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.payment_method_background));

                currentSelected = (ConstraintLayout) paymentMethodLayout;
                currentSelected.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.payment_method_background_selected));
            }
        });

        return v;
    }
}
