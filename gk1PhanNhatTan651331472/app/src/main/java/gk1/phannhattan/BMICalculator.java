package gk1.phannhattan;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;

public class BMICalculator extends AppCompatActivity {

    EditText edtChieuCao, edtCanNang;
    Button btnTinhBmi;
    TextView tvKetQuaBmi, tvPhanLoaiBmi;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tính BMI");
        }

        edtChieuCao = findViewById(R.id.edtChieuCao);
        edtCanNang = findViewById(R.id.edtCanNang);
        btnTinhBmi = findViewById(R.id.btnTinhBmi);
        tvKetQuaBmi = findViewById(R.id.tvKetQuaBmi);
        tvPhanLoaiBmi = findViewById(R.id.tvPhanLoaiBmi);

        btnTinhBmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinhToanBmi();
            }
        });
        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void tinhToanBmi() {
        String chieuCaoStr = edtChieuCao.getText().toString();
        String canNangStr = edtCanNang.getText().toString();

        if (chieuCaoStr.isEmpty() || canNangStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ chiều cao và cân nặng", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double chieuCao = Double.parseDouble(chieuCaoStr);
            double canNang = Double.parseDouble(canNangStr);

            if (chieuCao <= 0 || canNang <= 0) {
                Toast.makeText(this, "Chiều cao và cân nặng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            double bmi = canNang / (chieuCao * chieuCao);

            DecimalFormat df = new DecimalFormat("#.0");
            tvKetQuaBmi.setText("Kết quả BMI: " + df.format(bmi));

            phanLoaiBmi(bmi);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void phanLoaiBmi(double bmi) {
        String phanLoai;
        int color;

        if (bmi < 18.5) {
            phanLoai = "Dưới chuẩn (Gầy)";
            color = Color.BLUE;
        } else if (bmi >= 18.5 && bmi <= 24.9) {
            phanLoai = "Bình thường";
            color = Color.GREEN;
        } else if (bmi >= 25 && bmi <= 29.9) {
            phanLoai = "Thừa cân";
            color = Color.rgb(255, 165, 0);
        } else if (bmi >= 30 && bmi <= 34.9) {
            phanLoai = "Béo phì độ 1";
            color = Color.RED;
        } else if (bmi >= 35 && bmi <= 39.9) {
            phanLoai = "Béo phì độ 2 (Nguy hiểm)";
            color = Color.RED;
        } else {
            phanLoai = "Béo phì độ 3 (Rất nguy hiểm)";
            color = Color.RED;
        }

        tvPhanLoaiBmi.setText("Phân loại: " + phanLoai);
        tvPhanLoaiBmi.setTextColor(color);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}