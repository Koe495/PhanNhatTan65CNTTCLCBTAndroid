package com.example.midterm_practice_1_65133147;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnChucNang2, btnChucNang3, btnChucNang4, btnAboutMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View (tìm các nút bằng ID của chúng)
        btnChucNang2 = findViewById(R.id.btnChucNang2);
        btnChucNang3 = findViewById(R.id.btnChucNang3);
        btnChucNang4 = findViewById(R.id.btnChucNang4);
        btnAboutMe = findViewById(R.id.btnAboutMe);

        // Gán sự kiện Click cho nút Chức năng 2
        btnChucNang2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang TinhDiemActivity
                Intent intent = new Intent(MainActivity.this, );
                startActivity(intent);
            }
        });

        // Gán sự kiện Click cho nút Chức năng 3
        btnChucNang3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DanhSachMonActivity.class);
                startActivity(intent);
            }
        });

        // Gán sự kiện Click cho nút Chức năng 4
        btnChucNang4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HoatDongActivity.class);
                startActivity(intent);
            }
        });

        // Gán sự kiện Click cho nút About Me
        btnAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutMeActivity.class);
                startActivity(intent);
            }
        });
    }
}