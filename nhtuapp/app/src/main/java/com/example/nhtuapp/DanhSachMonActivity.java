package com.example.nhtuapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class DanhSachMonActivity extends AppCompatActivity {

    RecyclerView rvMonHoc;
    MonHocAdapter adapter;
    List<String> data; // Danh sách dữ liệu
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_mon);

        rvMonHoc = findViewById(R.id.rvMonHoc);

        // 1. Chuẩn bị dữ liệu (thay bằng dữ liệu thật của bạn)
        data = new ArrayList<>();
        data.add("Tin học đại cương");
        data.add("Lập trình Java");
        data.add("Phát triển Ứng dụng web");
        data.add("Khai phá dữ liệu lớn");
        data.add("Kinh tế chính trị Mác-Lênin");
        data.add("Triết học Mác-Lênin");
        // 2. Tạo Adapter
        adapter = new MonHocAdapter(data);
        // 3. Cấu hình RecyclerView
        rvMonHoc.setLayoutManager(new LinearLayoutManager(this)); // Hiển thị dạng danh sách dọc
        rvMonHoc.setAdapter(adapter);
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
}
