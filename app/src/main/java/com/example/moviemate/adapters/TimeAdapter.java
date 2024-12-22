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

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private List<String> timeList;
    private Context context;
    private int selectedPosition = -1;
    private OnTimeSelectedListener listener;

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    public TimeAdapter(Context context, List<String> timeList, String selectedTime, OnTimeSelectedListener listener) {
        this.context = context;
        this.timeList = timeList;
        if (selectedTime != null)
            this.selectedPosition = timeList.indexOf(selectedTime);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.time_item, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.timeTextView.setText(time);

        // Đặt trạng thái đã chọn cho item view dựa vào vị trí đã chọn
        holder.itemView.setSelected(position == selectedPosition);

        // Xử lý sự kiện click để chọn thời gian
        holder.itemView.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = position;

            // Thông báo cho listener rằng thời gian đã được chọn
            listener.onTimeSelected(time);

            // Cập nhật giao diện: chỉ gọi notifyItemChanged cho các vị trí cần thiết
            notifyItemChanged(previousSelectedPosition); // Cập nhật lại item trước đó
            notifyItemChanged(selectedPosition);         // Cập nhật lại item mới được chọn
        });
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.time_text_view);
        }
    }
}
