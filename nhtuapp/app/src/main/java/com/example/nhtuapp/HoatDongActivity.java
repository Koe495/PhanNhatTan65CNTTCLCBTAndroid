package com.example.nhtuapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class HoatDongActivity extends AppCompatActivity {

    RecyclerView rvHoatDong;
    HoatDongAdapter adapter;
    List<HoatDong> dataHoatDong;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoat_dong);

        rvHoatDong = findViewById(R.id.rvHoatDong);

        // 1. Chuẩn bị dữ liệu
        taoDuLieuAo();

        // 2. Tạo Adapter
        adapter = new HoatDongAdapter(dataHoatDong);

        // 3. Cấu hình RecyclerView
        rvHoatDong.setLayoutManager(new LinearLayoutManager(this));
        rvHoatDong.setAdapter(adapter);

        // (Tùy chọn) Thêm dòng kẻ
        rvHoatDong.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 1. Ánh xạ nút mới
        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        // 2. Gán sự kiện click cho nút quay lại
        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lệnh finish() sẽ đóng Activity hiện tại
                // và quay về Activity đã gọi nó (chính là MainActivity)
                finish();
            }
        });
    }

    private void taoDuLieuAo() {
        dataHoatDong = new ArrayList<>();
        // Tạo các đối tượng HoatDong
        // Nhớ dùng ảnh placeholder bạn đã thêm vào drawable
        dataHoatDong.add(new HoatDong("Tiêu đề hoạt động 1", "08:00 - 20/10/2025", R.drawable.placeholder));
        dataHoatDong.add(new HoatDong("Lễ khai giảng năm học mới", "07:30 - 05/09/2025", R.drawable.placeholder));
        dataHoatDong.add(new HoatDong("Hội thao sinh viên", "Cả ngày - 26/03/2026", R.drawable.placeholder));
        dataHoatDong.add(new HoatDong("Tiêu đề hoạt động 4", "14:00 - 30/11/2025", R.drawable.placeholder));
        dataHoatDong.add(new HoatDong("Tiêu đề hoạt động 5", "19:00 - 24/12/2025", R.drawable.placeholder));
    }
}