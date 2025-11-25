package com.example.sqliteexample;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLiteDatabase database = openOrCreateDatabase("books", MODE_PRIVATE, null);

        String sqlDeleteTable = "DROP TABLE IF EXISTS BOOKS";

        String sqlCreateTable = "CREATE TABLE BOOKS(BookID integer PRIMARY KEY, Page integer, Price float, Title text, Author text)";

        database.execSQL(sqlDeleteTable);
        database.execSQL(sqlCreateTable);

        String sqlInsert1 = "INSERT INTO BOOKS(Page, Price, Title, Author) VALUES(200, 20, 'Java Android 1', 'Tony')";
        String sqlInsert2 = "INSERT INTO BOOKS(Page, Price, Title, Author) VALUES(400, 30, 'Support guide', 'Koe')";
        database.execSQL(sqlInsert1);
        database.execSQL(sqlInsert2);

        database.close();

    }
}