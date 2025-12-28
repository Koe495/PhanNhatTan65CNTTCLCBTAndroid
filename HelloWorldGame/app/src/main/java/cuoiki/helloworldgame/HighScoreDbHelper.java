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
    // Tăng version lên 3 để cập nhật cấu trúc bảng mới
    private static final int DATABASE_VERSION = 3;

    // Tên bảng và các cột
    private static final String TABLE_NAME = "high_scores";
    private static final String COL_ID = "id";
    private static final String COL_HP = "hp";
    private static final String COL_DIFF = "difficulty";
    private static final String COL_MODE = "game_mode";
    private static final String COL_THEME = "theme_name"; // CỘT MỚI: Lưu tên Theme
    private static final String COL_TIMESTAMP = "timestamp";

    public HighScoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HP + " INTEGER, " +
                COL_DIFF + " INTEGER, " +
                COL_MODE + " TEXT, " +
                COL_THEME + " TEXT, " + // Thêm vào câu lệnh tạo bảng
                COL_TIMESTAMP + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ và tạo lại (Dữ liệu cũ sẽ mất, chấp nhận trong quá trình dev)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Cập nhật hàm thêm điểm: Thêm tham số themeName
    public void addHighScore(int hp, int diff, String mode, String themeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_HP, hp);
        values.put(COL_DIFF, diff);
        values.put(COL_MODE, mode);
        values.put(COL_THEME, themeName); // Lưu theme
        values.put(COL_TIMESTAMP, System.currentTimeMillis());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Cập nhật hàm lấy điểm: Lọc theo cả Mode và Theme
    public List<String> getTopScores(String mode, String themeName) {
        List<String> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String orderBy;

        // Logic sắp xếp giữ nguyên
        if (mode.equalsIgnoreCase("CLASSIC")) {
            orderBy = COL_HP + " DESC, " + COL_DIFF + " ASC";
        } else {
            orderBy = COL_DIFF + " DESC, " + COL_HP + " DESC";
        }

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COL_HP, COL_DIFF},
                COL_MODE + "=? AND " + COL_THEME + "=?", // Lọc theo Mode VÀ Theme
                new String[]{mode, themeName},
                null, null,
                orderBy,
                "5"
        );

        if (cursor != null && cursor.moveToFirst()) {
            int rank = 1;
            do {
                int hpVal = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HP));
                int diffVal = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DIFF));

                if (mode.equalsIgnoreCase("CLASSIC")) {
                    scores.add("#" + rank + " - HP: " + hpVal + " (Diff: " + diffVal + ")");
                } else {
                    scores.add("#" + rank + " - Diff: " + diffVal + " (HP: " + hpVal + ")");
                }
                rank++;
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            scores.add("who?");
        }
        db.close();
        return scores;
    }
}