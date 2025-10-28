package com.example.nhtuapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HoatDongAdapter extends RecyclerView.Adapter<HoatDongAdapter.HoatDongViewHolder> {

    private List<HoatDong> hoatDongList;

    public HoatDongAdapter(List<HoatDong> hoatDongList) {
        this.hoatDongList = hoatDongList;
    }

    @NonNull
    @Override
    public HoatDongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hoat_dong, parent, false);
        return new HoatDongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoatDongViewHolder holder, int position) {
        // Lấy đối tượng HoatDong tại vị trí
        HoatDong hoatDong = hoatDongList.get(position);

        // Gán dữ liệu lên View
        holder.tvTieuDe.setText(hoatDong.getTieuDe());
        holder.tvThoiGian.setText(hoatDong.getThoiGian());
        holder.imgAnhDaiDien.setImageResource(hoatDong.getAnhDaiDien());
    }

    @Override
    public int getItemCount() {
        return hoatDongList.size();
    }

    // ViewHolder giữ 3 View
    public static class HoatDongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAnhDaiDien;
        TextView tvTieuDe;
        TextView tvThoiGian;

        public HoatDongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAnhDaiDien = itemView.findViewById(R.id.imgAnhDaiDien);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
        }
    }
}