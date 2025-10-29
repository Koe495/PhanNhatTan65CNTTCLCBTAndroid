package com.example.nhtuapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ChiTietHoatDongActivity extends AppCompatActivity {

    TextView tvTieuDeChiTiet, tvNoiDungChiTiet;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hoat_dong);

        // Ánh xạ View
        tvTieuDeChiTiet = findViewById(R.id.tvTieuDeChiTiet);
        tvNoiDungChiTiet = findViewById(R.id.tvNoiDungChiTiet);
        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        // Xử lý nút quay lại Main (dùng finish() để quay lại màn hình danh sách)
        btnQuayLaiMain.setOnClickListener(v -> finish());

        // Thêm nút quay lại (mũi tên) trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Nhận Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy dữ liệu ra khỏi Intent, dùng key ("TIEU_DE", "NOI_DUNG")
            // Key này phải TRÙNG KHỚP với key ở Adapter
            String tieuDe = intent.getStringExtra("TIEU_DE");
            String noiDung = intent.getStringExtra("NOI_DUNG");

            // Gán dữ liệu lên View
            tvTieuDeChiTiet.setText(tieuDe);
            tvNoiDungChiTiet.setText(noiDung);

            // (Tùy chọn) Đặt tiêu đề cho ActionBar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(tieuDe);
            }
        }
    }

    // Xử lý khi bấm nút mũi tên quay lại trên ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}