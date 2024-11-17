package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;

import java.util.List;

public class DayTimeAdapter extends RecyclerView.Adapter<DayTimeAdapter.OptionViewHolder> {

    private List<String> optionList;
    private Context context;
    private int selectedPosition = -1;
    private OnOptionSelectedListener listener;

    public interface OnOptionSelectedListener {
        void onOptionSelected(String option);
    }

    public DayTimeAdapter(Context context, List<String> optionList, OnOptionSelectedListener listener) {
        this.context = context;
        this.optionList = optionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daytime_item, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String option = optionList.get(position);
        holder.optionTextView.setText(option);

        // Thay đổi màu nền cho option đã chọn
        holder.itemView.setBackgroundColor(position == selectedPosition
                ? context.getResources().getColor(R.color.selected_item_color)
                : context.getResources().getColor(R.color.unselected_item_color));

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            listener.onOptionSelected(option);
            notifyDataSetChanged(); // Cập nhật màu sắc khi option được chọn
        });
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }

    public static class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView optionTextView;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionTextView = itemView.findViewById(R.id.text_view);
        }
    }
}
