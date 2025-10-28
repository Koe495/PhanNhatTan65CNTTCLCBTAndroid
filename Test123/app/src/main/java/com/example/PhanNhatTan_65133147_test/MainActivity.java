package com.example.PhanNhatTan_65133147_test; // Thay bằng package của bạn

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.PhanNhatTan_65133147_test.R;
import com.example.PhanNhatTan_65133147_test.View2;
import com.example.PhanNhatTan_65133147_test.View3;
import com.example.PhanNhatTan_65133147_test.View4;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnChucNang2, btnChucNang3, btnChucNang4, btnAboutMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các nút từ XML
        btnChucNang2 = findViewById(R.id.btnChucNang2);
        btnChucNang3 = findViewById(R.id.btnChucNang3);
        btnChucNang4 = findViewById(R.id.btnChucNang4);
        btnAboutMe = findViewById(R.id.btnAboutMe);

        // Gán sự kiện click cho các nút
        btnChucNang2.setOnClickListener(this);
        btnChucNang3.setOnClickListener(this);
        btnChucNang4.setOnClickListener(this);
        btnAboutMe.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Kiểm tra xem nút nào được bấm
        if (v.getId() == R.id.btnChucNang2) {
            // Mở màn hình Tính Điểm
            startActivity(new Intent(MainActivity.this, View2.class));
        } else if (v.getId() == R.id.btnChucNang3) {
            // Mở màn hình Danh Sách Môn
            startActivity(new Intent(MainActivity.this, View3.class));
        } else if (v.getId() == R.id.btnChucNang4) {
            // Mở màn hình Hoạt Động
            startActivity(new Intent(MainActivity.this, View4.class));
        }
    }
}