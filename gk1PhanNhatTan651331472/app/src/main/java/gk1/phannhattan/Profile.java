package gk1.phannhattan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Profile extends AppCompatActivity {
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupInfoRows();

        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, MainActivity.class));
            }
        });
    }

    private void setupInfoRows() {
        View rowHoTen = findViewById(R.id.llPersonalInfo).findViewWithTag("row_hoten");
        setupRow(findViewById(R.id.infoHoTen), R.drawable.user, "Họ & Tên", "Phan Nhật Tấn");
        setupRow(findViewById(R.id.infoMSSV), R.drawable.paper, "Mã số SV", "65133147");
        setupRow(findViewById(R.id.infoLop), R.drawable.home, "Lớp", "65.CNTT_CLC");
        setupRow(findViewById(R.id.infoEmail), R.drawable.mail, "Email", "tan.pn@ntu.edu.vn");
        setupRow(findViewById(R.id.infoPhone), R.drawable.telephone, "Số điện thoại", "0845 034 799");
    }

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