package com.example.nhtuapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TinhDiemActivity extends AppCompatActivity {

    EditText edtDiemGiuaKy, edtDiemCuoiKy;
    Button btnTinhTB;
    TextView tvKetQua;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tinh_diem);

        // Ánh xạ View
        edtDiemGiuaKy = findViewById(R.id.edtDiemGiuaKy);
        edtDiemCuoiKy = findViewById(R.id.edtDiemCuoiKy);
        btnTinhTB = findViewById(R.id.btnTinhTB);
        tvKetQua = findViewById(R.id.tvKetQua);

        // Bắt sự kiện click nút
        btnTinhTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinhDiemTrungBinh();
            }
        });
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

    private void tinhDiemTrungBinh() {
        String strGiuaKy = edtDiemGiuaKy.getText().toString();
        String strCuoiKy = edtDiemCuoiKy.getText().toString();

        // Kiểm tra xem người dùng đã nhập đủ chưa
        if (strGiuaKy.isEmpty() || strCuoiKy.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ cả 2 cột điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Chuyển đổi String sang double
            double diemGiuaKy = Double.parseDouble(strGiuaKy);
            double diemCuoiKy = Double.parseDouble(strCuoiKy);

            // Kiểm tra điểm hợp lệ (0-10)
            if (diemGiuaKy < 0 || diemGiuaKy > 10 || diemCuoiKy < 0 || diemCuoiKy > 10) {
                Toast.makeText(this, "Điểm phải nằm trong khoảng từ 0 đến 10", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tính điểm (theo tỉ lệ 50% - 50%)
            double diemTB = (diemGiuaKy * 0.5) + (diemCuoiKy * 0.5);

            // Hiển thị kết quả, làm tròn 2 chữ số
            tvKetQua.setText("Điểm TB: " + String.format("%.2f", diemTB));

        } catch (NumberFormatException e) {
            // Bắt lỗi nếu người dùng nhập không phải là số (ví dụ: "abc" hay "5.5.5")
            Toast.makeText(this, "Định dạng số không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}