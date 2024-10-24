package com.example.newyogaapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.HistoryItem;

import java.util.List;

public class YogaHistoryAdapter extends RecyclerView.Adapter<YogaHistoryAdapter.HistoryViewHolder> {
    private List<HistoryItem> historyList;

    // Constructor
    public YogaHistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    // ViewHolder class
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView checkoutDate, className, pricePerClass, quantity, totalPrice, email;  // Đổi username thành email

        public HistoryViewHolder(View itemView) {
            super(itemView);
            checkoutDate = itemView.findViewById(R.id.tvCheckoutDate);
            className = itemView.findViewById(R.id.tvClassName);
            pricePerClass = itemView.findViewById(R.id.tvPricePerClass);
            quantity = itemView.findViewById(R.id.tvQuantity);
            totalPrice = itemView.findViewById(R.id.tvTotalPrice);
            email = itemView.findViewById(R.id.tvEmail);  // Đổi tên biến username thành email
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder holder, int position) {
        HistoryItem currentItem = historyList.get(position);

        // Thiết lập dữ liệu cho các TextView
        holder.checkoutDate.setText(currentItem.getCheckoutDate());
        holder.className.setText(currentItem.getClassName());
        holder.pricePerClass.setText(String.valueOf(currentItem.getPricePerClass()));
        holder.quantity.setText(String.valueOf(currentItem.getQuantity()));
        holder.totalPrice.setText(String.valueOf(currentItem.getTotalPrice()));

        // Hiển thị email trực tiếp từ HistoryItem
        holder.email.setText(currentItem.getEmail());  // Lấy email từ HistoryItem và hiển thị
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
