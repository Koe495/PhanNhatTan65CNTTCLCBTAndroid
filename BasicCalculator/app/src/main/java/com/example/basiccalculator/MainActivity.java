package com.example.basiccalculator;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.basiccalculator.R;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private String selectedOperation = null;
    private MaterialButton lastSelectedButton = null;

    EditText editTextA, editTextB;
    TextView resultText;
    Button buttonAdd, buttonSubtract, buttonMultiply, buttonDivide, buttonConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextA = findViewById(R.id.editText_a);
        editTextB = findViewById(R.id.editText_b);
        buttonAdd = findViewById(R.id.button_add);
        buttonSubtract = findViewById(R.id.button_subtract);
        buttonMultiply = findViewById(R.id.button_multiply);
        buttonDivide = findViewById(R.id.button_divide);
        buttonConfirm = findViewById(R.id.button_confirm);
        resultText = findViewById(R.id.result_text);

        View.OnClickListener operationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialButton clickedButton = (MaterialButton) view;
                selectedOperation = clickedButton.getText().toString();
                highlightSelectedButton(clickedButton);
            }
        };

        buttonAdd.setOnClickListener(operationClickListener);
        buttonSubtract.setOnClickListener(operationClickListener);
        buttonMultiply.setOnClickListener(operationClickListener);
        buttonDivide.setOnClickListener(operationClickListener);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation();
            }
        });
    }

    private void highlightSelectedButton(MaterialButton clickedButton) {
        if (lastSelectedButton != null) {
            lastSelectedButton.setTypeface(Typeface.DEFAULT);
            lastSelectedButton.setBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_primary));
        }

        clickedButton.setTypeface(Typeface.DEFAULT_BOLD);
        clickedButton.setBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_primary_dark));

        lastSelectedButton = clickedButton;
    }

    private void performCalculation() {
        String strA = editTextA.getText().toString();
        String strB = editTextB.getText().toString();

        if (strA.isEmpty() || strB.isEmpty()) {
            resultText.setText("Vui lòng nhập cả hai số A và B.");
            Toast.makeText(this, "Vui lòng nhập cả hai số A và B.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedOperation == null) {
            resultText.setText("Vui lòng chọn một phép toán.");
            Toast.makeText(this, "Vui lòng chọn một phép toán.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double numA = Double.parseDouble(strA);
            double numB = Double.parseDouble(strB);
            Double result = null;

            switch (selectedOperation) {
                case "+":
                    result = numA + numB;
                    break;
                case "-":
                    result = numA - numB;
                    break;
                case "*":
                    result = numA * numB;
                    break;
                case "/":
                    if (numB == 0) {
                        resultText.setText("Không thể chia cho 0.");
                        Toast.makeText(this, "Không thể chia cho 0.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    result = numA / numB;
                    break;
            }

            if (result != null) {
                if (result == (long) result.doubleValue()) { // Kiểm tra số nguyên
                    resultText.setText("Kết quả: " + (long) result.doubleValue());
                } else {
                    resultText.setText("Kết quả: " + result);
                }
            }

        } catch (NumberFormatException e) {
            resultText.setText("Dữ liệu nhập không hợp lệ.");
            Toast.makeText(this, "Dữ liệu nhập không hợp lệ.", Toast.LENGTH_SHORT).show();
        }
    }
}
