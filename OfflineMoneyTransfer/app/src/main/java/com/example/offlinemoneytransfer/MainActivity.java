package com.example.offlinemoneytransfer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    ImageView imageView1, imageView2;
    Spinner spinner1, spinner2;
    EditText editText1, editText2;
    Button button1;

    private String[] currencies = {"USD", "VND", "JPY", "INR"};
    private String fromCurrency = "USD";
    private String toCurrency = "VND";

    private void findView() {
        imageView1 = findViewById(R.id.flag1);
        imageView2 = findViewById(R.id.flag2);
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        editText1 = findViewById(R.id.edittext1);
        editText2 = findViewById(R.id.edittext2);
        button1 = findViewById(R.id.button1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findView();

        // Không cho nhập vào editText2
        editText2.setFocusable(false);
        editText2.setClickable(false);

        // Adapter cho Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        // Thiết lập mệnh giá và lá cờ mặc định
        spinner1.setSelection(0); // USD
        spinner2.setSelection(1); // VND
        updateFlag(imageView1, "USD");
        updateFlag(imageView2, "VND");

        // Xử lý sự kiện khi chọn spinner1
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromCurrency = currencies[position];
                updateFlag(imageView1, fromCurrency);
                if (fromCurrency.equals(toCurrency)) {
                    int newPos = (position + 1) % currencies.length;
                    spinner2.setSelection(newPos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Xử lý sự kiện khi chọn spinner2
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toCurrency = currencies[position];
                updateFlag(imageView2, toCurrency);
                if (toCurrency.equals(fromCurrency)) {
                    int newPos = (position + 1) % currencies.length;
                    spinner1.setSelection(newPos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Xử lý khi bấm nút Transfer
        button1.setOnClickListener(v -> {
            String input = editText1.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập số tiền cần chuyển đổi!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float amount = Float.parseFloat(input);
                float result = convertCurrency(amount, fromCurrency, toCurrency);
                editText2.setText(String.format("%.2f", result));
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Dữ liệu nhập không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Chuyển đổi tiền tệ
    private float convertCurrency(float amount, String from, String to) {
        if (from.equals(to)) return amount;

        switch (from + "_" + to) {
            case "USD_VND": return amount * 25000f;
            case "VND_USD": return amount / 25000f;

            case "USD_JPY": return amount * 150f;
            case "JPY_USD": return amount / 150f;

            case "VND_JPY": return amount / 200f;
            case "JPY_VND": return amount * 200f;

            case "USD_INR": return amount * 83f;
            case "INR_USD": return amount / 83f;

            case "VND_INR": return amount / 300f;
            case "INR_VND": return amount * 300f;

            case "JPY_INR": return amount * 0.55f;
            case "INR_JPY": return amount / 0.55f;

            default: return amount;
        }
    }

    // Đổi hình cờ theo loại tiền
    private void updateFlag(ImageView imageView, String currency) {
        switch (currency) {
            case "USD":
                imageView.setImageResource(R.drawable.america_flag);
                break;
            case "VND":
                imageView.setImageResource(R.drawable.vietnam_flag);
                break;
            case "JPY":
                imageView.setImageResource(R.drawable.japan_flag);
                break;
            case "INR":
                imageView.setImageResource(R.drawable.india_flag);
                break;
        }
    }
}
