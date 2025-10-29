package gk1.phannhattan;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ChiTietMedicineActivity extends AppCompatActivity {

    TextView tvTieuDeChiTiet, tvNoiDungChiTiet;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_medicine);

        tvTieuDeChiTiet = findViewById(R.id.tvTieuDeChiTiet);
        tvNoiDungChiTiet = findViewById(R.id.tvNoiDungChiTiet);

        Intent intent = getIntent();
        if (intent != null) {
            String tieuDe = intent.getStringExtra("TIEU_DE");
            String noiDung = intent.getStringExtra("NOI_DUNG");

            tvTieuDeChiTiet.setText(tieuDe);
            tvNoiDungChiTiet.setText(noiDung);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(tieuDe);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}