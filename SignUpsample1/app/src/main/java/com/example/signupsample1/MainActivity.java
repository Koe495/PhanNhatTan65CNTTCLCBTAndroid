package com.example.signupsample1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText ed1, ed2, ed3, ed4;
    TextView tvResult;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ed1 = findViewById(R.id.emailInput);
        ed2 = findViewById(R.id.phoneInput);
        ed3 = findViewById(R.id.passwordInput);
        ed4 = findViewById(R.id.confirmPasswordInput);
        btn = findViewById(R.id.button);
        tvResult = findViewById(R.id.warning);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(view);
            }
        });
    }
    public void validate(View view) {
        String email = ed1.getText().toString();
        String phone = ed2.getText().toString();
        String password = ed3.getText().toString();
        String confirmPassword = ed4.getText().toString();
        if (email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            tvResult.setText("Please enter all the fields!");
        }
        else tvResult.setText("");
    }
}