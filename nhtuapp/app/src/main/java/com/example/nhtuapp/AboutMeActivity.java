package com.example.nhtuapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutMeActivity extends AppCompatActivity {
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        // Ẩn ActionBar mặc định (nếu theme của bạn có)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Tùy chọn: Gán dữ liệu cho các hàng (để trông thật hơn)
        // Nếu không làm bước này, 5 hàng sẽ hiển thị "Tiêu đề" và "Giá trị"
        setupInfoRows();

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

    // Hàm này gán dữ liệu tĩnh vào các hàng
    private void setupInfoRows() {
        // Hàng 1: Họ Tên
        View rowHoTen = findViewById(R.id.llPersonalInfo).findViewWithTag("row_hoten"); // Cần đặt tag trong XML
        // Tạm thời, chúng ta sẽ dùng cách tìm kiếm phức tạp hơn một chút
        // vì chúng ta có 5 <include> giống hệt nhau.

        // --- CÁCH ĐƠN GIẢN HƠN: Đặt ID cho từng <include> ---
        // 1. Sửa activity_about_me.xml:
        //    <include android:id="@+id/infoHoTen" layout="@layout/item_profile_info" />
        //    <include android:id="@+id/infoMSSV" layout="@layout/item_profile_info" />
        //    ...

        // 2. Quay lại file Java này:
        setupRow(findViewById(R.id.infoHoTen), R.drawable.user, "Họ & Tên", "Phan Nhật Tấn");
        setupRow(findViewById(R.id.infoMSSV), R.drawable.paper, "Mã số SV", "65133147");
        setupRow(findViewById(R.id.infoLop), R.drawable.home, "Lớp", "65.CNTT_CLC");
        setupRow(findViewById(R.id.infoEmail), R.drawable.mail, "Email", "tan.pn@ntu.edu.vn");
        setupRow(findViewById(R.id.infoPhone), R.drawable.telephone, "Số điện thoại", "0845 034 799");
    }

    /**
     * Hàm trợ giúp để gán dữ liệu cho một hàng thông tin
     * @param rowView View của hàng (chính là <include> đã được tìm thấy)
     * @param iconResId ID của icon trong drawable
     * @param title Tiêu đề (ví dụ: "Email")
     * @param value Giá trị (ví dụ: "abc@gmail.com")
     */
    private void setupRow(View rowView, int iconResId, String title, String value) {
        if (rowView != null) {
            ImageView icon = rowView.findViewById(R.id.itemIcon);
            TextView tvTitle = rowView.findViewById(R.id.itemTitle);
            TextView tvValue = rowView.findViewById(R.id.itemValue);

            icon.setImageResource(iconResId);
            tvTitle.setText(title);
            tvValue.setText(value);
        }
    }
}