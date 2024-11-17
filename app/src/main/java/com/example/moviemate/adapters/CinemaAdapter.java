package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.models.Cinema;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private Context context;
    private List<Cinema> cinemaList;
    private OnCinemaSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Vị trí rạp được chọn

    public CinemaAdapter(Context context, List<Cinema> cinemaList, OnCinemaSelectedListener listener) {
        this.context = context;
        this.cinemaList = cinemaList;
        this.listener = listener;
    }

    // Interface để thông báo khi một rạp được chọn
    public interface OnCinemaSelectedListener {
        void onCinemaSelected(Cinema cinema);
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cinema_item, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);

        // Thiết lập thông tin cho từng item
        holder.cinemaName.setText(cinema.getCinemaName());
        holder.cinemaAddress.setText(cinema.getAddress());

        // Tải logo của rạp chiếu phim bằng Picasso
        Picasso.get().load(cinema.getBrandLogo()).into(holder.cinemaLogo);

        // Thay đổi nền của item dựa trên vị trí được chọn
        if (position == selectedPosition) {
            holder.cinemaContainer.setBackgroundResource(R.drawable.cinema_background_selected); // Nền khi được chọn
        } else {
            holder.cinemaContainer.setBackgroundResource(R.drawable.cinema_background_unselected); // Nền khi không được chọn
        }

        // Xử lý sự kiện click cho từng item
        holder.itemView.setOnClickListener(v -> {
            // Lưu lại vị trí đã chọn trước đó
            int previousPosition = selectedPosition;
            // Cập nhật vị trí đã chọn
            selectedPosition = holder.getAdapterPosition();

            // Cập nhật lại item cũ và item mới
            notifyItemChanged(previousPosition); // Làm mới item trước đó
            notifyItemChanged(selectedPosition); // Làm mới item hiện tại

            // Gọi listener để thông báo rằng một item đã được chọn
            if (listener != null) {
                listener.onCinemaSelected(cinema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (cinemaList != null) ? cinemaList.size() : 0;
    }

    // ViewHolder cho CinemaAdapter
    public static class CinemaViewHolder extends RecyclerView.ViewHolder {
        ImageView cinemaLogo;
        TextView cinemaName, cinemaAddress, cinemaDistance;
        View cinemaContainer;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cinemaLogo = itemView.findViewById(R.id.cinema_logo);
            cinemaName = itemView.findViewById(R.id.cinema_name);
            cinemaAddress = itemView.findViewById(R.id.cinema_address);
            cinemaContainer = itemView.findViewById(R.id.cinema_container); // Tham chiếu đến layout chứa để thay đổi nền
        }
    }
}
