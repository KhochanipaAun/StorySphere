package com.example.storysphere_appbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StorysphereDatabase.db";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_WRITINGS = "writings";

    // ตาราง users
    //public static final String TABLE_USERS = "users";

    // ตาราง writings (เพิ่มใหม่)
    //public static final String TABLE_WRITINGS = "writings";

    //public DBHelper(Context context) {
     //   super(context, DATABASE_NAME, null, 3);
    //}

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);  // ✅ เวอร์ชัน 4 เพื่อรองรับ image_uri
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // users table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "username TEXT, " +
                "password TEXT, " +
                "image_uri TEXT)");

        // writings table (เวอร์ชันสมบูรณ์)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WRITINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "tagline TEXT, " +
                "tag TEXT, " +
                "category TEXT, " +
                "image_path TEXT, " +
                "content TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_WRITINGS + " ADD COLUMN content TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN image_uri TEXT");
        }
    }

    // ======================== Users ========================
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
    }

    public boolean updateUser(String email, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("password", newPassword);
        int rows = db.update(TABLE_USERS, values, "email = ?", new String[]{email});
        return rows > 0;
    }

    public boolean updateUser(String email, String newUsername, String newPassword, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("password", newPassword);
        if (imageUri != null) {
            values.put("image_uri", imageUri);
        }
        int rows = db.update(TABLE_USERS, values, "email = ?", new String[]{email});
        return rows > 0;
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, "email = ?", new String[]{email}) > 0;
    }

    public boolean insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        return db.insert(TABLE_USERS, null, values) != -1;
    }

    // ======================== Writings ========================
    public long insertWriting(String title, String tagline, String tag, String category, String imagePath, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("tagline", tagline);
        cv.put("tag", tag);
        cv.put("category", category);
        cv.put("image_path", imagePath);
        cv.put("content", content);
        return db.insert(TABLE_WRITINGS, null, cv);
    }

    public Cursor getAllWritings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " ORDER BY id DESC", null);
    }

    public boolean deleteWriting (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_WRITINGS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateWriting(int id, String title, String tagline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("tagline", tagline);
        int result = db.update("writings", values, "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public List<WritingItem> getAllWritingItems() {
        List<WritingItem> writingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String tagline = cursor.getString(cursor.getColumnIndexOrThrow("tagline"));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow("tag"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));

                WritingItem item = new WritingItem(id, title, tagline, tag, category, imagePath);
                item.setTag(content); // ✅ ต้องมี setter ใน WritingItem
                writingList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return writingList;
    }

    public Cursor getWritingById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM writings WHERE id = ?", new String[]{String.valueOf(id)});
    }

    // 📌 เพิ่ม method นี้เข้าไปใน DBHelper (storysphere_appbar)
    public boolean insertBook(String title, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("image_path", imageUri); // ใช้ image_path แทน image_uri
        long result = db.insert(TABLE_WRITINGS, null, values);
        return result != -1;
    }
}
