package com.example.intentexample_65133147;

import android.content.Intent;
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

    TextView textView1;
    EditText editText1;
    Button button1;
    void declare(){
        textView1 = findViewById(R.id.textView);
        editText1 = findViewById(R.id.edittext_input);
        button1 = findViewById(R.id.button_confirm);
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
        declare();
        button1.setOnClickListener(view -> {
            if (!editText1.getText().toString().isEmpty()) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("myName", editText1.getText().toString());
                startActivity(intent);
            }
        });
    }

}