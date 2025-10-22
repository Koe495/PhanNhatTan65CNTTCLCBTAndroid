package com.example.phannhattan_65_cntt_clc_listviewexampple;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ListView helloworldlist;
//    String[] values = new String[] {"English", "Vietnamese", "French", "German", "Italian", "Spanish", "Chinese"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        textView= findViewById(R.id.textView);
        helloworldlist= findViewById(R.id.helloworldlist);

        // Khai báo List
        ArrayList<String> list = new ArrayList<String>();
        // Đưa dữ liệu vào list
        list = getData();
        //Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        helloworldlist.setAdapter(adapter);

        helloworldlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = helloworldlist.getItemAtPosition(position).toString();
                textView.setText(selected);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    ArrayList<String> getData(){
        ArrayList<String> tempList = new ArrayList<String>();
        tempList.add("English");
        tempList.add("Vietnamese");
        tempList.add("French");
        tempList.add("German");
        tempList.add("Italian");
        tempList.add("Spanish");
        tempList.add("Chinese");
        return tempList;
    }
}