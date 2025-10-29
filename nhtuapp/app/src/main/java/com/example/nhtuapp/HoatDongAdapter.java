package com.example.nhtuapp;

import android.content.Context;
import android.content.Intent;
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
    public class HoatDongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAnhDaiDien;
        TextView tvTieuDe;
        TextView tvThoiGian;

        public HoatDongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAnhDaiDien = itemView.findViewById(R.id.imgAnhDaiDien);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition(); // Lấy vị trí item được click
                    if (position != RecyclerView.NO_POSITION) {
                        // Lấy đúng đối tượng HoatDong được click
                        HoatDong clickedItem = hoatDongList.get(position);

                        // Lấy Context từ View
                        Context context = v.getContext();

                        // Tạo Intent để mở Activity chi tiết
                        Intent intent = new Intent(context, ChiTietHoatDongActivity.class);

                        // Đóng gói dữ liệu để gửi đi
                        intent.putExtra("TIEU_DE", clickedItem.getTieuDe());
                        intent.putExtra("NOI_DUNG", clickedItem.getNoiDung());
                        // Bạn có thể gửi thêm thời gian, ảnh... nếu muốn

                        // Bắt đầu Activity mới
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}