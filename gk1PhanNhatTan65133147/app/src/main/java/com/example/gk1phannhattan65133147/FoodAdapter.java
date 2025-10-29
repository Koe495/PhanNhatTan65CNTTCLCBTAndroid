package com.example.gk1phannhattan65133147;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<String> monHocList;

    // Constructor để nhận dữ liệu
    public FoodViewHolder(List<String> monHocList) {
        this.monHocList = monHocList;
    }

    // Tạo ra View cho mỗi item
    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mon_hoc, parent, false);
        return new FoodViewHolder(view);
    }

    // Gán dữ liệu vào View
    @Override
    public void onBindViewHolder(@NonNull MonHocViewHolder holder, int position) {
        String tenMon = monHocList.get(position);
        holder.tvTenMonHoc.setText(tenMon);
    }

    // Trả về số lượng item
    @Override
    public int getItemCount() {
        return monHocList.size();
    }

    // Lớp ViewHolder để "giữ" các View của item
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenMonHoc;
        public MonHocViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenMonHoc = itemView.findViewById(R.id.tvTenMonHoc);
        }
    }
}