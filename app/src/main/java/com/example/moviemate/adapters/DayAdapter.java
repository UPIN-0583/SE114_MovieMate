package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<String> dayList;
    private Context context;
    private int selectedPosition = -1; // Vị trí đã chọn
    private OnDaySelectedListener listener;

    public interface OnDaySelectedListener {
        void onDaySelected(String day);
    }

    public DayAdapter(Context context, List<String> dayList, String selectedDay, OnDaySelectedListener listener) {
        this.context = context;
        this.dayList = dayList;
        if (selectedDay != null)
            this.selectedPosition = dayList.indexOf(selectedDay);
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.day_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        String originalDate = dayList.get(position);

        // Chuyển đổi định dạng ngày
        String[] formattedDate = formatDate(originalDate);
        holder.dayMonthTextView.setText(formattedDate[0]);  // ví dụ: "Dec"
        holder.dayNumberTextView.setText(formattedDate[1]); // ví dụ: "10"

        // Đặt trạng thái đã chọn cho item view dựa vào vị trí đã chọn
        holder.itemView.setSelected(position == selectedPosition);

        // Xử lý sự kiện click để chọn ngày
        holder.itemView.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = position;

            // Thông báo cho listener rằng ngày đã được chọn
            listener.onDaySelected(originalDate);

            // Cập nhật giao diện: chỉ gọi notifyItemChanged cho các vị trí cần thiết
            notifyItemChanged(previousSelectedPosition); // Cập nhật lại item trước đó
            notifyItemChanged(selectedPosition);         // Cập nhật lại item mới được chọn
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayMonthTextView;
        TextView dayNumberTextView;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayMonthTextView = itemView.findViewById(R.id.day_month);
            dayNumberTextView = itemView.findViewById(R.id.day_number);
        }
    }

    /**
     * Phương thức chuyển đổi định dạng ngày từ yyyy-MM-dd sang ["MMM", "dd"]
     */
    private String[] formatDate(String originalDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

            Date date = originalFormat.parse(originalDate);
            if (date != null) {
                String month = monthFormat.format(date); // ví dụ: "Dec"
                String day = dayFormat.format(date);     // ví dụ: "10"
                return new String[]{month, day};
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new String[]{"", ""}; // Trả về chuỗi rỗng nếu có lỗi
    }
}
