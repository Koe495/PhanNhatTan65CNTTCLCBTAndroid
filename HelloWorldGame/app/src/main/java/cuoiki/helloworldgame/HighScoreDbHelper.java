package cuoiki.helloworldgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class HighScoreDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HelloWorldGame.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng và các cột
    private static final String TABLE_NAME = "high_scores";
    private static final String COL_ID = "id";
    private static final String COL_SCORE = "score";
    private static final String COL_MODE = "game_mode";
    private static final String COL_TIMESTAMP = "timestamp";

    public HighScoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SCORE + " INTEGER, " +
                COL_MODE + " TEXT, " +
                COL_TIMESTAMP + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addHighScore(int diffValue, String mode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Lưu giá trị Diff vào cột Score
        values.put(COL_SCORE, diffValue);
        values.put(COL_MODE, mode);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<String> getTopScores(String mode) {
        List<String> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Lấy Top 5 Diff cao nhất
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COL_SCORE, COL_TIMESTAMP},
                COL_MODE + "=?",
                new String[]{mode},
                null, null,
                COL_SCORE + " DESC", // Sắp xếp theo Diff giảm dần
                "5"
        );

        if (cursor != null && cursor.moveToFirst()) {
            int rank = 1;
            do {
                int diffVal = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCORE));
                scores.add("#" + rank + " - Diff: " + diffVal);
                rank++;
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            scores.add("null");
        }
        db.close();
        return scores;
    }
}