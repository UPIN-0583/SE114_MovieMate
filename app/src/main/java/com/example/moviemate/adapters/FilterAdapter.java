package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moviemate.R;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {
    private List<String> filterItems;
    private List<String> selectedItems;
    private Context context;
    private OnFilterSelectedListener listener;

    public interface OnFilterSelectedListener {
        void onFilterSelected(String filter);
        void onFilterDeselected(String filter);
    }

    public FilterAdapter(Context context, List<String> items, List<String> selectedItems, OnFilterSelectedListener listener) {
        this.context = context;
        this.filterItems = items;
        this.selectedItems = selectedItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.filter_item, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        String item = filterItems.get(position);
        holder.filterText.setText(item);

        // Cập nhật trạng thái được chọn
        boolean isSelected = selectedItems.contains(item);
        updateFilterAppearance(holder.itemView, isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
                listener.onFilterDeselected(item);
                updateFilterAppearance(holder.itemView, false);
            } else {
                selectedItems.add(item);
                listener.onFilterSelected(item);
                updateFilterAppearance(holder.itemView, true);
            }
        });
    }

    private void updateFilterAppearance(View itemView, boolean isSelected) {
        itemView.setBackgroundResource(isSelected ? R.drawable.filter_selected_background : R.drawable.filter_background);
        TextView filterText = itemView.findViewById(R.id.filterText);
        filterText.setTextColor(ContextCompat.getColor(context,
                isSelected ? R.color.white : R.color.gray));
    }

    @Override
    public int getItemCount() {
        return filterItems.size();
    }

    public List<String> getFilterItems() {
        return filterItems;
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView filterText;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            filterText = itemView.findViewById(R.id.filterText);
        }
    }
}