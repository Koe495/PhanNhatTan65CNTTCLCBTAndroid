package gk1.phannhattan;

import android.content.Intent;
import android.os.AsyncTask;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MoneyCurrencyTransfer extends AppCompatActivity {

    ImageView imageView1, imageView2;
    Spinner spinner1, spinner2;
    EditText editText1, editText2;
    Button button1;
    Button btnQuayLaiMain;

    private String[] currencies = {"USD", "VND", "JPY", "INR"};
    private String fromCurrency = "USD";
    private String toCurrency = "VND";
    private HashMap<String, Float> rates = new HashMap<>();

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
        setContentView(R.layout.activity_money_currency_transfer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.money), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findView();

        editText2.setFocusable(false);
        editText2.setClickable(false);

        // Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        spinner1.setSelection(0); // USD
        spinner2.setSelection(1); // VND
        updateFlag(imageView1, "USD");
        updateFlag(imageView2, "VND");

        // Gọi API để lấy tỷ giá thật
        new FetchRatesTask().execute();

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromCurrency = currencies[position];
                updateFlag(imageView1, fromCurrency);
                if (fromCurrency.equals(toCurrency)) {
                    int temp = spinner2.getSelectedItemPosition();
                    spinner2.setSelection((position + 1) % currencies.length);
                    spinner1.setSelection(temp);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toCurrency = currencies[position];
                updateFlag(imageView2, toCurrency);
                if (toCurrency.equals(fromCurrency)) {
                    int temp = spinner1.getSelectedItemPosition();
                    spinner1.setSelection((position + 1) % currencies.length);
                    spinner2.setSelection(temp);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        button1.setOnClickListener(v -> {
            String input = editText1.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(MoneyCurrencyTransfer.this, "Vui lòng nhập số tiền cần chuyển đổi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rates.isEmpty()) {
                Toast.makeText(MoneyCurrencyTransfer.this, "Đang tải dữ liệu tỷ giá, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float amount = Float.parseFloat(input);
                float result = convertCurrency(amount, fromCurrency, toCurrency);
                editText2.setText(String.format("%.2f", result));
            } catch (NumberFormatException e) {
                Toast.makeText(MoneyCurrencyTransfer.this, "Dữ liệu nhập không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoneyCurrencyTransfer.this, MainActivity.class));
            }
        });
    }

    private float convertCurrency(float amount, String from, String to) {
        if (from.equals(to)) return amount;
        try {
            float usdBase = rates.get(from); // 1 USD = ? fromCurrency
            float targetBase = rates.get(to); // 1 USD = ? toCurrency
            return amount / usdBase * targetBase;
        } catch (Exception e) {
            return amount;
        }
    }

    private void updateFlag(ImageView imageView, String currency) {
        switch (currency) {
            case "USD": imageView.setImageResource(R.drawable.america_flag); break;
            case "VND": imageView.setImageResource(R.drawable.vietnam_flag); break;
            case "JPY": imageView.setImageResource(R.drawable.japan_flag); break;
            case "INR": imageView.setImageResource(R.drawable.india_flag); break;
        }
    }

    // Lớp AsyncTask để tải tỷ giá từ Internet
    private class FetchRatesTask extends AsyncTask<Void, Void, HashMap<String, Float>> {
        @Override
        protected HashMap<String, Float> doInBackground(Void... voids) {
            HashMap<String, Float> result = new HashMap<>();
            try {
                URL url = new URL("https://open.er-api.com/v6/latest/USD");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONObject ratesJson = json.getJSONObject("rates");

                // 4 loại tiền
                result.put("USD", 1.0f);
                result.put("VND", (float) ratesJson.getDouble("VND"));
                result.put("JPY", (float) ratesJson.getDouble("JPY"));
                result.put("INR", (float) ratesJson.getDouble("INR"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(HashMap<String, Float> fetchedRates) {
            if (!fetchedRates.isEmpty()) {
                rates = fetchedRates;
                Toast.makeText(MoneyCurrencyTransfer.this, "Tải tỷ giá thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MoneyCurrencyTransfer.this, "Không thể tải tỷ giá!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
