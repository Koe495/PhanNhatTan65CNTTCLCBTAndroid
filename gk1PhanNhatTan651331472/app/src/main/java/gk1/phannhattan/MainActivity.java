package gk1.phannhattan;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnChucNang2, btnChucNang3, btnChucNang4, btnAboutMe, btnMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChucNang2 = findViewById(R.id.btnChucNang2);
        btnChucNang3 = findViewById(R.id.btnChucNang3);
        btnChucNang4 = findViewById(R.id.btnChucNang4);
        btnAboutMe = findViewById(R.id.btnAboutMe);
        btnMoney = findViewById(R.id.btnMoney);
        btnChucNang2.setOnClickListener(this);
        btnChucNang3.setOnClickListener(this);
        btnChucNang4.setOnClickListener(this);
        btnAboutMe.setOnClickListener(this);
        btnMoney.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnChucNang2) {
            startActivity(new Intent(MainActivity.this, BMICalculator.class));
        } else if (v.getId() == R.id.btnChucNang3) {
            startActivity(new Intent(MainActivity.this, FoodList.class));
        } else if (v.getId() == R.id.btnChucNang4) {
            startActivity(new Intent(MainActivity.this, MedList.class));
        } else if (v.getId() == R.id.btnAboutMe) {
            startActivity(new Intent(MainActivity.this, Profile.class));
        } else if (v.getId() == R.id.btnMoney) {
            startActivity(new Intent(MainActivity.this, MoneyCurrencyTransfer.class));
        }
    }
}