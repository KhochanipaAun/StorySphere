package com.example.storysphere_appbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StorysphereDatabase.db";

    public static final String TABLE_USERS           = "users";
    public static final String TABLE_WRITINGS        = "writings";
    public static final String TABLE_CURRENT_SESSION = "current_session";
    public static final String TABLE_EPISODES        = "episodes";
    public static final String TABLE_BOOKMARKS       = "bookmarks";
    public static final String TABLE_HISTORY         = "reading_history";
    public static final String TABLE_FOLLOWS         = "follows";
    public static final String TABLE_LIKES_LOG = "user_likes";
    public static final String TABLE_COMMENTS  = "comments";
    public static final String TABLE_BANNERS = "banners";

    // ใหม่
    public static final String TABLE_REPORTS = "reports";
    public static final String TABLE_NOTIFICATIONS   = "notifications";

    public static final String COL_LIKES = "likes_count";
    public static final String COL_PROFILE_URI = "profile_image_uri";

    // users new columns
    public static final String COL_IS_BANNED = "is_banned";
    public static final String COL_BAN_REASON = "ban_reason";



    private static final int DATABASE_VERSION = 25;


    public DBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        // เปิด FK constraints ให้ทำงาน
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    // ======================== CREATE ========================
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // users
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE," +
                    "username TEXT," +
                    "password TEXT," +
                    "image_uri TEXT," +
                    "role TEXT DEFAULT 'user'," +
                    COL_PROFILE_URI + " TEXT," +
                    COL_IS_BANNED + " INTEGER NOT NULL DEFAULT 0," +
                    COL_BAN_REASON + " TEXT" +
                    ")");

            // writings
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WRITINGS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "tagline TEXT," +
                    "tag TEXT," +
                    "category TEXT," +
                    "image_path TEXT," +
                    "content TEXT," +
                    "author_email TEXT," +
                    COL_LIKES + " INTEGER NOT NULL DEFAULT 0," +
                    "views_count INTEGER NOT NULL DEFAULT 0," +
                    "FOREIGN KEY (author_email) REFERENCES " + TABLE_USERS + "(email) ON DELETE SET NULL" +
                    ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_likes ON " + TABLE_WRITINGS + "(" + COL_LIKES + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_views ON " + TABLE_WRITINGS + "(views_count)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_author_email ON " + TABLE_WRITINGS + "(author_email)");

            // session
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CURRENT_SESSION + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT)");

            // core tables
            ensureEpisodesTable(db);
            ensureBookmarksTable(db);
            ensureHistoryTable(db);
            ensureFollowsTable(db);

            // per-user likes log
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LIKES_LOG + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT NOT NULL," +
                    "writing_id INTEGER NOT NULL," +
                    "created_at INTEGER NOT NULL," +
                    "UNIQUE(user_email, writing_id) ON CONFLICT IGNORE," +
                    "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_ul_user_time ON " + TABLE_LIKES_LOG + "(user_email, created_at DESC)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_ul_writing ON " + TABLE_LIKES_LOG + "(writing_id)");

            // comments
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COMMENTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT NOT NULL," +
                    "writing_id INTEGER NOT NULL," +
                    "episode_id INTEGER," +
                    "content TEXT NOT NULL," +
                    "created_at INTEGER NOT NULL," +
                    "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (episode_id) REFERENCES " + TABLE_EPISODES + "(episode_id) ON DELETE SET NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_c_user_time ON " + TABLE_COMMENTS + "(user_email, created_at DESC)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_c_writing ON " + TABLE_COMMENTS + "(writing_id)");

            // banners
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BANNERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "image_path TEXT NOT NULL," +
                    "title TEXT," +
                    "deeplink TEXT," +
                    "is_active INTEGER NOT NULL DEFAULT 1," +
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))" +
                    ")");

// reports (เก็บรีพอร์ตคอมเมนต์/งานเขียน)
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REPORTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "comment_id INTEGER," +                 // อ้างถึง comments.id (ถ้ามี)
                    "writing_id INTEGER," +                 // อ้างถึง writings.id (ทางเลือก)
                    "reporter_email TEXT NOT NULL," +
                    "reason TEXT," +
                    "status TEXT NOT NULL DEFAULT 'OPEN'," +// OPEN / RESOLVED
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                    "resolved_at INTEGER," +
                    "moderator_note TEXT," +
                    "FOREIGN KEY (comment_id) REFERENCES " + TABLE_COMMENTS + "(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE" +
                    ")");


            // notifications
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT NOT NULL," +     // ผู้รับ
                    "type TEXT NOT NULL," +           // FOLLOW / NEW_EPISODE / SYSTEM
                    "title TEXT," +
                    "message TEXT," +
                    "payload_json TEXT," +
                    "created_at INTEGER NOT NULL," +
                    "read_at INTEGER" +
                    ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_notif_user_time ON " + TABLE_NOTIFICATIONS + "(user_email, created_at DESC)");

            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }


    // ======================== UPGRADE ========================
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // (ของเดิม) migrations เดิมคงไว้…
        if (oldVersion < 3)  db.execSQL("ALTER TABLE " + TABLE_WRITINGS + " ADD COLUMN content TEXT");
        if (oldVersion < 4)  db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN image_uri TEXT");
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT_SESSION);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CURRENT_SESSION + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT)");
        }
        if (oldVersion < 6)  db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN role TEXT DEFAULT 'user'");
        if (oldVersion < 7)  ensureEpisodesTable(db);
        if (oldVersion < 8)  ensureBookmarksTable(db);
        if (oldVersion < 9)  safeAddColumn(db, TABLE_WRITINGS, COL_LIKES, "INTEGER NOT NULL DEFAULT 0");
        ensureColumnExists(db, TABLE_WRITINGS, COL_LIKES, "INTEGER NOT NULL DEFAULT 0");
        if (oldVersion < 12){
            safeAddColumn(db, TABLE_WRITINGS, "views_count", "INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_views ON " + TABLE_WRITINGS + "(views_count)");
        }
        ensureColumnExists(db, TABLE_WRITINGS, "views_count", "INTEGER NOT NULL DEFAULT 0");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_likes ON " + TABLE_WRITINGS + "(" + COL_LIKES + ")");
        if (oldVersion < 13) ensureHistoryTable(db);

        if (oldVersion < 17) {
            safeAddColumn(db, TABLE_WRITINGS, "author_email", "TEXT");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_author_email ON " + TABLE_WRITINGS + "(author_email)");
        }
        if (oldVersion < 24) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BANNERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "image_path TEXT NOT NULL," +
                    "title TEXT," +
                    "deeplink TEXT," +
                    "is_active INTEGER NOT NULL DEFAULT 1," +
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))" +
                    ")");
        }

        // likes log & comments (safety)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LIKES_LOG + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_email TEXT NOT NULL," +
                "writing_id INTEGER NOT NULL," +
                "created_at INTEGER NOT NULL," +
                "UNIQUE(user_email, writing_id) ON CONFLICT IGNORE," +
                "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ul_user_time ON " + TABLE_LIKES_LOG + "(user_email, created_at DESC)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ul_writing ON " + TABLE_LIKES_LOG + "(writing_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COMMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_email TEXT NOT NULL," +
                "writing_id INTEGER NOT NULL," +
                "episode_id INTEGER," +
                "content TEXT NOT NULL," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE," +
                "FOREIGN KEY (episode_id) REFERENCES " + TABLE_EPISODES + "(episode_id) ON DELETE SET NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_c_user_time ON " + TABLE_COMMENTS + "(user_email, created_at DESC)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_c_writing ON " + TABLE_COMMENTS + "(writing_id)");

        if (oldVersion < 23) {
            db.beginTransaction();
            try {
                db.execSQL(
                        "CREATE TABLE IF NOT EXISTS writings_v23 (" +
                                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "  title TEXT," +
                                "  tagline TEXT," +
                                "  tag TEXT," +
                                "  category TEXT," +
                                "  image_path TEXT," +
                                "  content TEXT," +
                                "  author_email TEXT," +
                                "  " + COL_LIKES + " INTEGER NOT NULL DEFAULT 0," +
                                "  views_count INTEGER NOT NULL DEFAULT 0," +
                                "  FOREIGN KEY (author_email) REFERENCES " + TABLE_USERS + "(email) ON DELETE SET NULL" +
                                ")"
                );
                db.execSQL(
                        "INSERT INTO writings_v23 " +
                                "(id, title, tagline, tag, category, image_path, content, author_email, " + COL_LIKES + ", views_count) " +
                                "SELECT id, title, tagline, tag, category, image_path, content, author_email, " + COL_LIKES + ", IFNULL(views_count,0) " +
                                "FROM " + TABLE_WRITINGS
                );
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_WRITINGS);
                db.execSQL("ALTER TABLE writings_v23 RENAME TO " + TABLE_WRITINGS);
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_likes ON " + TABLE_WRITINGS + "(" + COL_LIKES + ")");
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_views ON " + TABLE_WRITINGS + "(views_count)");
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_writings_author_email ON " + TABLE_WRITINGS + "(author_email)");
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }

        // v25+: เพิ่มคอลัมน์แบน + ตาราง reports/notifications
        if (oldVersion < 25) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REPORTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "comment_id INTEGER," +
                    "writing_id INTEGER," +
                    "reporter_email TEXT NOT NULL," +
                    "reason TEXT," +
                    "status TEXT NOT NULL DEFAULT 'OPEN'," +
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                    "resolved_at INTEGER," +
                    "moderator_note TEXT," +
                    "FOREIGN KEY (comment_id) REFERENCES " + TABLE_COMMENTS + "(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE" +
                    ")");
        }

        if (oldVersion < 26) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "title TEXT," +
                    "message TEXT," +
                    "payload_json TEXT," +
                    "created_at INTEGER NOT NULL," +
                    "read_at INTEGER)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_notif_user_time ON " + TABLE_NOTIFICATIONS + "(user_email, created_at DESC)");
        }
        if (oldVersion < 25) {
            ensureReportsTable(db);
        }

        // safety net
        ensureEpisodesTable(db);
        ensureBookmarksTable(db);
        ensureHistoryTable(db);
        ensureFollowsTable(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no-op
    }

    private void ensureHistoryTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_email TEXT NOT NULL," +
                "writing_id INTEGER NOT NULL," +
                "episode_id INTEGER," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE," +
                "FOREIGN KEY (episode_id) REFERENCES " + TABLE_EPISODES + "(episode_id) ON DELETE SET NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hist_user_time ON " + TABLE_HISTORY + "(user_email, created_at DESC)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hist_writing ON " + TABLE_HISTORY + "(writing_id)");
    }


    // ======================== MODELS ========================
    public static class HistoryItem {
        public int id;
        public int writingId;
        public Integer episodeId;
        public long createdAt;
        public String writingTitle;
        public String episodeTitle;
        public String imagePath;
    }

    public static class LikeLog {
        public int id;
        public int writingId;
        public String userEmail;
        public long createdAt;
    }

    public static class Comment {
        public int id;
        public int writingId;
        public Integer episodeId;
        public String userEmail;
        public String content;
        public long createdAt;
    }

    public static class CommentItem {
        public int id;
        public int writingId;
        public Integer episodeId; // nullable
        public String userEmail;
        public String writingTitle;   // for display
        public String episodeTitle;   // for display
        public String content;
        public long createdAt;
    }

    public static class Banner { public int id; public String imagePath, title, deeplink; public boolean active; }


    public static class Notification {
        public int id;
        public String userEmail, type, title, message, payloadJson;
        public long createdAt;
        public Long readAt; // nullable
    }

    public static class ReportItem {
        public int id;
        public int commentId;
        public String reporterEmail;
        public String reason;
        public long createdAt;
        public String status; // open/cleared/banned
        // joined info (optional display)
        public String commentContent;
        public String commentUserEmail;
        public String writingTitle;
        public String episodeTitle;
    }

    // ======================== HISTORY APIs ========================
    /** บันทึกเมื่อผู้ใช้อ่านตอน */
    public void addHistory(String email, int writingId, @Nullable Integer episodeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", email);
        values.put("writing_id", writingId);
        if (episodeId != null) values.put("episode_id", episodeId);
        values.put("created_at", System.currentTimeMillis());
        db.insert(TABLE_HISTORY, null, values);
    }

    public List<HistoryItem> getReadingHistory(String userEmail, int limit) {
        return getReadingHistory(userEmail, null, limit);
    }

    /** รองรับค้นหา title เรื่อง/ตอน + คืน history.id */
    public List<HistoryItem> getReadingHistory(String userEmail, @Nullable String query, int limit) {
        String q = (query==null? "" : query.trim());
        String where = "WHERE h.user_email=? ";
        ArrayList<String> args = new ArrayList<>();
        args.add(userEmail);

        if (!q.isEmpty()){
            where += "AND (w.title LIKE ? OR EXISTS(SELECT 1 FROM " + TABLE_EPISODES + " e " +
                    "WHERE e.episode_id=h.episode_id AND e.title LIKE ?)) ";
            args.add("%"+q+"%");
            args.add("%"+q+"%");
        }

        String sql =
                "SELECT " +
                        "  h.id, " +
                        "  h.writing_id, " +
                        "  h.episode_id, " +
                        "  h.created_at, " +
                        "  w.title       AS w_title, " +
                        "  w.image_path  AS w_image, " +
                        " (SELECT e.title FROM " + TABLE_EPISODES + " e WHERE e.episode_id = h.episode_id) AS e_title " +
                        "FROM " + TABLE_HISTORY + " h " +
                        "JOIN " + TABLE_WRITINGS + " w ON w.id = h.writing_id " +
                        where +
                        "ORDER BY h.created_at DESC " + (limit > 0 ? "LIMIT " + limit : "");

        ArrayList<HistoryItem> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) {
                HistoryItem hi = new HistoryItem();
                hi.id           = c.getInt(0);
                hi.writingId    = c.getInt(1);
                hi.episodeId    = c.isNull(2) ? null : c.getInt(2);
                hi.createdAt    = c.getLong(3);
                hi.writingTitle = c.getString(4);
                hi.imagePath    = c.getString(5);
                hi.episodeTitle = c.getString(6);
                out.add(hi);
            }
        }
        return out;
    }

    /** ลบประวัติรายแถวด้วย id */
    public boolean deleteHistoryById(int historyId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_HISTORY, "id=?", new String[]{String.valueOf(historyId)}) > 0;
    }

    // ======================== SESSION ========================
    public boolean saveLoginSession(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", email);
        db.delete(TABLE_CURRENT_SESSION, null, null);
        long result = db.insert(TABLE_CURRENT_SESSION, null, values);
        return result != -1;
    }

    public String getLoggedInUserEmail() {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT user_email FROM " + TABLE_CURRENT_SESSION + " LIMIT 1", null)) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("user_email");
                if (idx != -1) return c.getString(idx);
            }
        }
        return null;
    }

    public boolean clearLoginSession() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CURRENT_SESSION, null, null) > 0;
    }


    // ==== BAN API ====
    public boolean banUser(String email, @Nullable String reason) {
        if (email == null || email.trim().isEmpty()) return false;
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_BANNED, 1);
        if (reason != null) cv.put(COL_BAN_REASON, reason);
        return getWritableDatabase().update(TABLE_USERS, cv, "email=?", new String[]{ email }) > 0;
    }
    public boolean unbanUser(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_BANNED, 0);
        cv.putNull(COL_BAN_REASON);
        return getWritableDatabase().update(TABLE_USERS, cv, "email=?", new String[]{ email }) > 0;
    }
    public boolean isUserBanned(String email) {
        if (email == null) return false;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_IS_BANNED + " FROM " + TABLE_USERS + " WHERE email=? LIMIT 1",
                new String[]{ email })) {
            return (c.moveToFirst() && c.getInt(0) == 1);
        }
    }
    public boolean canUserWriteOrComment(String email) {
        return !isUserBanned(email);
    }

    // ======================== USERS ========================
    public boolean insertUser(String username, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("email", email);
        v.put("password", password);
        v.put("role", role);
        return db.insert(TABLE_USERS, null, v) != -1;
    }

    public boolean insertUser(String username, String email, String password) {
        return insertUser(username, email, password, "user");
    }

    public boolean updateUser(String email, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", newUsername);
        v.put("password", newPassword);
        return db.update(TABLE_USERS, v, "email = ?", new String[]{email}) > 0;
    }

    public boolean updateUser(String email, String newUsername, String newPassword, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        if (newUsername != null) v.put("username", newUsername);
        if (newPassword != null) v.put("password", newPassword);
        if (imageUri != null)  v.put("image_uri", imageUri);
        if (v.size() == 0) return false;
        return db.update(TABLE_USERS, v, "email = ?", new String[]{email}) > 0;
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, "email = ?", new String[]{email}) > 0;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
    }

    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ?", new String[]{username});
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE email = ? LIMIT 1", new String[]{email})) {
            return c.moveToFirst();
        }
    }

    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE username = ? LIMIT 1", new String[]{username})) {
            return c.moveToFirst();
        }
    }

    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        return db.update(TABLE_USERS, cv, "email = ?", new String[]{email}) > 0;
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String role = null;
        try (Cursor c = db.rawQuery("SELECT role FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("role");
                if (idx != -1) role = c.getString(idx);
            }
        }
        Log.d("DBHelper", "getUserRole for " + email + ": " + role);
        return role;
    }

    // คืนชื่อแสดงผลของผู้ใช้ (display_name ถ้ามี, ถ้าไม่มีใช้ email)
    public String getUserDisplayName(String email) {
        if (email == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT display_name FROM users WHERE email=?", new String[]{email});
        String name = null;
        if (c.moveToFirst()) {
            name = c.getString(0);
        }
        c.close();
        return (name != null && !name.trim().isEmpty()) ? name : email;
    }

    // ตรวจสอบว่า user เป็น writer ของเรื่องนี้หรือไม่
    public boolean isUserWriter(String email, int writingId) {
        if (email == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM writings WHERE id=? AND author_email=? LIMIT 1",
                new String[]{String.valueOf(writingId), email});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }


    public String getUserImageUri(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT image_uri FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("image_uri");
                if (idx != -1) return c.getString(idx);
            }
        }
        return null;
    }

    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean ok;
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " WHERE email = ? AND password = ? LIMIT 1",
                new String[]{email, password})) {
            ok = c.moveToFirst();
        }
        Log.d("DBHelper", "Credentials check result: " + ok);
        return ok;
    }

    /** คืน username ตาม email (ถ้าไม่พบคืน null) */
    public String getUsernameByEmail(String email) {
        if (email == null) return null;
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT username FROM " + TABLE_USERS + " WHERE email=? LIMIT 1",
                new String[]{ email })) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    /** คืนอีเมลผู้เขียนของเรื่อง */
    public String getAuthorEmailForWriting(int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT author_email FROM " + TABLE_WRITINGS + " WHERE id=? LIMIT 1",
                new String[]{ String.valueOf(writingId) })) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    /** ดึงรายการนิยายของผู้เขียนตามอีเมล */
    public List<WritingItem> getWritingItemsByAuthorEmail(String email) {
        ArrayList<WritingItem> list = new ArrayList<>();
        if (email == null || email.trim().isEmpty()) return list;

        SQLiteDatabase db = getReadableDatabase();
        if (!hasColumn(db, TABLE_WRITINGS, "author_email")) return list;

        try (Cursor c = db.rawQuery(
                "SELECT id, title, tagline, tag, category, image_path " +
                        "FROM " + TABLE_WRITINGS + " WHERE author_email=? ORDER BY id DESC",
                new String[]{ email })) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5)
                ));
            }
        }
        return list;
    }

    /** คืนชื่อผู้เขียน (username) ของเรื่องตาม writingId; ถ้าไม่มี คืน local-part ของอีเมล; ถ้าไม่มีทั้งคู่ คืน null */
    public @Nullable String getAuthorNameForWritingId(int writingId) {
        SQLiteDatabase db = getReadableDatabase();

        String email = null;
        try (Cursor c = db.rawQuery("SELECT author_email FROM " + TABLE_WRITINGS + " WHERE id=? LIMIT 1",
                new String[]{ String.valueOf(writingId) })) {
            if (c.moveToFirst()) email = c.isNull(0) ? null : c.getString(0);
        }
        if (email == null || email.trim().isEmpty()) return null;

        String name = null;
        try (Cursor u = db.rawQuery("SELECT username FROM " + TABLE_USERS + " WHERE email=? LIMIT 1",
                new String[]{ email })) {
            if (u.moveToFirst()) name = u.isNull(0) ? null : u.getString(0);
        }
        if (name != null && !name.trim().isEmpty()) return name.trim();

        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    // ======================== WRITINGS ========================
    public long insertWriting(String title, String tagline, String tag, String category, String imagePath, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("tagline", tagline);
        cv.put("tag", tag);
        cv.put("category", category);
        cv.put("image_path", imagePath);
        cv.put("content", content);

        String authorEmail = getLoggedInUserEmail();
        if (authorEmail == null || authorEmail.trim().isEmpty()) {
            Log.e("DBHelper", "insertWriting blocked: user not logged in");
            return -1;
        }
        if (hasColumn(db, TABLE_WRITINGS, "author_email")) {
            cv.put("author_email", authorEmail.trim());
        }
        return db.insert(TABLE_WRITINGS, null, cv);
    }

    public boolean updateWriting(int id, String title, String tagline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("tagline", tagline);
        return db.update(TABLE_WRITINGS, v, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteWriting(int writingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            String[] args = new String[]{ String.valueOf(writingId) };

            try { db.delete("comments",        "writing_id=?", args); } catch (Throwable ignore) {}
            try { db.delete("episodes",        "writing_id=?", args); } catch (Throwable ignore) {}
            try { db.delete("bookmarks",       "writing_id=?", args); } catch (Throwable ignore) {}
            try { db.delete("user_likes",      "writing_id=?", args); } catch (Throwable ignore) {}
            try { db.delete("reading_history", "writing_id=?", args); } catch (Throwable ignore) {}
            try { db.delete("likes_log",       "writing_id=?", args); } catch (Throwable ignore) {}
            try { db.delete("views_log",       "writing_id=?", args); } catch (Throwable ignore) {}

            int affected = db.delete(TABLE_WRITINGS, "id=?", args);

            db.setTransactionSuccessful();
            return affected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getAllWritingsCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " ORDER BY id DESC", null);
    }
    public Cursor getAllWritings() { return getAllWritingsCursor(); }

    public List<WritingItem> getAllWritingItems() {
        List<WritingItem> list = new ArrayList<>();
        try (Cursor c = getAllWritingsCursor()) {
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String title = safeGet(c, "title");
                    String tagline = safeGet(c, "tagline");
                    String tag = safeGet(c, "tag");
                    String category = safeGet(c, "category");
                    String imagePath = safeGet(c, "image_path");
                    list.add(new WritingItem(id, title, tagline, tag, category, imagePath));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public Cursor getWritingById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " WHERE id = ?", new String[]{String.valueOf(id)});
    }

    public boolean insertBook(String title, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("image_path", imageUri);
        return db.insert(TABLE_WRITINGS, null, v) != -1;
    }

    public boolean writingExists(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_WRITINGS + " WHERE id=? LIMIT 1",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst();
        }
    }

    public List<WritingItem> getWritingItemsByTag(String rawTag, int limit) {
        if (rawTag == null) rawTag = "";
        String norm = rawTag.trim().toLowerCase();
        String alt = norm.replace("-", "");

        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT id, title, tagline, tag, category, image_path " +
                        "FROM " + TABLE_WRITINGS + " " +
                        "WHERE " +
                        "  LOWER(IFNULL(category,'')) = ? " +
                        "  OR LOWER(REPLACE(IFNULL(category,''),'-','')) = ? " +
                        "  OR (',' || LOWER(REPLACE(IFNULL(tag,''), ' ', '')) || ',') LIKE ? " +
                        "  OR (',' || LOWER(REPLACE(IFNULL(tag,''), ' ', '')) || ',') LIKE ? " +
                        "ORDER BY id DESC " +
                        (limit > 0 ? "LIMIT " + limit : "");
        String like1 = "%," + norm + ",%";
        String like2 = "%," + alt + ",%";

        List<WritingItem> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, new String[]{norm, alt, like1, like2})) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5)
                ));
            }
        }
        return list;
    }

    public List<WritingItem> searchWritings(String keyword, int limit) {
        ArrayList<WritingItem> list = new ArrayList<>();
        if (keyword == null) keyword = "";
        String k = "%" + keyword.trim() + "%";

        String sql = "SELECT id, title, tagline, tag, category, image_path " +
                "FROM " + TABLE_WRITINGS + " " +
                "WHERE title LIKE ? OR tagline LIKE ? OR tag LIKE ? " +
                "ORDER BY id DESC " +
                (limit > 0 ? "LIMIT " + limit : "");

        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, new String[]{ k, k, k });
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5)
                ));
            }
        } finally { if (c != null) c.close(); }
        return list;
    }

    public List<WritingItem> getRecentWritings(int limit) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, title, tagline, tag, category, image_path FROM " + TABLE_WRITINGS +
                " ORDER BY id DESC " + (limit > 0 ? "LIMIT " + limit : "");
        List<WritingItem> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5)
                ));
            }
        }
        return list;
    }

    /** Top Chart by likes (>0) */
    public List<WritingItem> getTopWritingsByLikes(int limit) {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT w.id, w.title, w.tagline, w.tag, w.category, w.image_path, " +
                        "  (SELECT COUNT(*) FROM " + TABLE_BOOKMARKS + " b WHERE b.writing_id = w.id) AS bmks, " +
                        "  w." + COL_LIKES + " AS likes, " +
                        "  IFNULL(w.views_count,0) AS views " +
                        "FROM " + TABLE_WRITINGS + " w " +
                        "WHERE w." + COL_LIKES + " > 0 " +
                        "ORDER BY w." + COL_LIKES + " DESC, w.id DESC " +
                        (limit > 0 ? "LIMIT " + limit : "");
        ArrayList<WritingItem> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5),
                        c.getInt(6), c.getInt(7), c.getInt(8)
                ));
            }
        }
        return list;
    }

    /** Top Chart by views (>0) */
    public List<WritingItem> getTopWritingsByViews(int limit) {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT w.id, w.title, w.tagline, w.tag, w.category, w.image_path, " +
                        "  (SELECT COUNT(*) FROM " + TABLE_BOOKMARKS + " b WHERE b.writing_id = w.id) AS bmks, " +
                        "  w." + COL_LIKES + " AS likes, " +
                        "  IFNULL(w.views_count,0) AS views " +
                        "FROM " + TABLE_WRITINGS + " w " +
                        "WHERE w.views_count > 0 " +
                        "ORDER BY views DESC, w.id DESC " +
                        (limit > 0 ? "LIMIT " + limit : "");
        ArrayList<WritingItem> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5),
                        c.getInt(6), c.getInt(7), c.getInt(8)
                ));
            }
        }
        return list;
    }

    // ======================== WRITINGS: By Username ========================
    public List<WritingItem> getWritingItemsByUsername(String username) {
        ArrayList<WritingItem> list = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return getAllWritingItems();
        }
        String key = username.trim();

        SQLiteDatabase db = this.getReadableDatabase();

        boolean hasAuthorUsername = hasColumn(db, TABLE_WRITINGS, "author_username");
        boolean hasAuthorEmail    = hasColumn(db, TABLE_WRITINGS, "author_email");
        boolean hasUserIdFk       = hasColumn(db, TABLE_WRITINGS, "user_id");

        Cursor c = null;
        try {
            if (hasAuthorUsername) {
                c = db.rawQuery(
                        "SELECT id,title,tagline,tag,category,image_path " +
                                "FROM " + TABLE_WRITINGS + " WHERE author_username=? ORDER BY id DESC",
                        new String[]{ key }
                );
            } else if (hasAuthorEmail) {
                String email = null;
                try (Cursor u = db.rawQuery(
                        "SELECT email FROM " + TABLE_USERS + " WHERE username=? LIMIT 1",
                        new String[]{ key })) {
                    if (u.moveToFirst()) email = u.getString(0);
                }
                if (email != null) {
                    c = db.rawQuery(
                            "SELECT id,title,tagline,tag,category,image_path " +
                                    "FROM " + TABLE_WRITINGS + " WHERE author_email=? ORDER BY id DESC",
                            new String[]{ email }
                    );
                }
            } else if (hasUserIdFk) {
                Integer uid = null;
                try (Cursor u = db.rawQuery(
                        "SELECT id FROM " + TABLE_USERS + " WHERE username=? LIMIT 1",
                        new String[]{ key })) {
                    if (u.moveToFirst()) uid = u.getInt(0);
                }
                if (uid != null) {
                    c = db.rawQuery(
                            "SELECT id,title,tagline,tag,category,image_path " +
                                    "FROM " + TABLE_WRITINGS + " WHERE user_id=? ORDER BY id DESC",
                            new String[]{ String.valueOf(uid) }
                    );
                }
            }

            if (c == null) {
                c = db.rawQuery(
                        "SELECT id,title,tagline,tag,category,image_path FROM " + TABLE_WRITINGS + " ORDER BY id DESC",
                        null
                );
            }

            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5)
                ));
            }
        } finally {
            if (c != null) c.close();
        }
        return list;
    }

    // ======================== BOOKMARKS ========================
    public boolean setBookmark(String userEmail, int writingId, boolean enable) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getLoggedInUserEmail();
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = "guest";
        }

        SQLiteDatabase db = getWritableDatabase();
        if (enable) {
            if (isBookmarked(userEmail, writingId)) return true;

            ContentValues cv = new ContentValues();
            cv.put("user_email", userEmail);
            cv.put("writing_id", writingId);
            cv.put("created_at", System.currentTimeMillis());

            long rowId = db.insertWithOnConflict(TABLE_BOOKMARKS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            return rowId != -1 || isBookmarked(userEmail, writingId);
        } else {
            return db.delete(TABLE_BOOKMARKS, "user_email=? AND writing_id=?",
                    new String[]{userEmail, String.valueOf(writingId)}) > 0;
        }
    }

    public boolean isBookmarked(String userEmail, int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_BOOKMARKS + " WHERE user_email=? AND writing_id=? LIMIT 1",
                new String[]{userEmail, String.valueOf(writingId)})) {
            return c.moveToFirst();
        }
    }

    public int countBookmarks(int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_BOOKMARKS + " WHERE writing_id=?",
                new String[]{String.valueOf(writingId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public List<WritingItem> getBookmarkedWritings(String userEmail) {
        return getBookmarkedWritings(userEmail, null);
    }

    public List<WritingItem> getBookmarkedWritings(String userEmail, @Nullable String query) {
        List<WritingItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String q = (query==null? "" : query.trim());
        String where = "WHERE b.user_email=? ";
        String[] args;

        if (!q.isEmpty()){
            where += "AND (w.title LIKE ? OR IFNULL(w.tagline,'') LIKE ?)";
            args = new String[]{ userEmail, "%"+q+"%", "%"+q+"%" };
        } else {
            args = new String[]{ userEmail };
        }

        String sql = "SELECT w.id, w.title, w.tagline, w.tag, w.category, w.image_path " +
                "FROM " + TABLE_BOOKMARKS + " b " +
                "JOIN " + TABLE_WRITINGS + " w ON w.id=b.writing_id " +
                where + " ORDER BY b.created_at DESC";

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5)));
            }
        }
        return list;
    }



    public boolean isFollowing(String followerEmail, String authorEmail) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_FOLLOWS +
                        " WHERE follower_email=? AND author_email=? LIMIT 1",
                new String[]{followerEmail, authorEmail})) {
            return c.moveToFirst();
        }
    }

    public int countFollowers(String authorEmail) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOLLOWS + " WHERE author_email=?",
                new String[]{authorEmail})) {
            return (c.moveToFirst() ? c.getInt(0) : 0);
        }
    }

    // ======================== LIKES / VIEWS ========================
    public int getLikes(int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT " + COL_LIKES + " FROM " + TABLE_WRITINGS + " WHERE id=?",
                new String[]{String.valueOf(writingId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public int addLikeOnce(int writingId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_WRITINGS + " SET " + COL_LIKES + " = " + COL_LIKES + " + 1 WHERE id=?",
                new Object[]{ writingId });
        return getLikes(writingId);
    }

    public int getViews(int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT views_count FROM " + TABLE_WRITINGS + " WHERE id=?",
                new String[]{String.valueOf(writingId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public int addViewOnce(int writingId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_WRITINGS + " SET views_count = views_count + 1 WHERE id=?",
                new Object[]{ writingId });
        return getViews(writingId);
    }

    // ======================== USER LIKES ========================
    /** toggle like โดยระบุอีเมลเอง */
    public boolean setUserLike(String userEmail, int writingId, boolean enable) {
        if (userEmail == null || userEmail.trim().isEmpty()) return false;
        SQLiteDatabase db = getWritableDatabase();

        if (enable) {
            // insert log
            ContentValues cv = new ContentValues();
            cv.put("user_email", userEmail);
            cv.put("writing_id", writingId);
            cv.put("created_at", System.currentTimeMillis());
            long rowId = db.insertWithOnConflict(TABLE_LIKES_LOG, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

            // update counter
            if (rowId != -1) {
                db.execSQL("UPDATE " + TABLE_WRITINGS + " SET " + COL_LIKES + " = " + COL_LIKES + " + 1 WHERE id=?",
                        new Object[]{ writingId });
            }
            return true;
        } else {
            // delete log
            int deleted = db.delete(TABLE_LIKES_LOG, "user_email=? AND writing_id=?",
                    new String[]{ userEmail, String.valueOf(writingId) });
            if (deleted > 0) {
                db.execSQL("UPDATE " + TABLE_WRITINGS + " SET " + COL_LIKES + " = CASE WHEN " + COL_LIKES + " > 0 THEN " + COL_LIKES + " - 1 ELSE 0 END WHERE id=?",
                        new Object[]{ writingId });
                return true;
            }
            return false;
        }
    }

    /** toggle like โดยใช้ session อัตโนมัติ */
    public boolean setUserLike(int writingId, boolean enable) {
        String email = getLoggedInUserEmail();
        if (email == null || email.trim().isEmpty()) email = "guest";
        return setUserLike(email, writingId, enable);
    }

    /** ผู้ใช้กดใจแล้วหรือยัง */
    public boolean isUserLiked(String userEmail, int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_LIKES_LOG +
                " WHERE user_email=? AND writing_id=? LIMIT 1", new String[]{ userEmail, String.valueOf(writingId) })) {
            return c.moveToFirst();
        }
    }

    /** รายการเรื่องที่ผู้ใช้เคยกดใจ (ล่าสุดก่อน) */
    public List<WritingItem> getLikedWritings(String userEmail) {
        ArrayList<WritingItem> list = new ArrayList<>();
        if (userEmail == null || userEmail.trim().isEmpty()) return list;

        String sql = "SELECT w.id, w.title, w.tagline, w.tag, w.category, w.image_path " +
                "FROM " + TABLE_LIKES_LOG + " l " +
                "JOIN " + TABLE_WRITINGS + " w ON w.id=l.writing_id " +
                "WHERE l.user_email=? ORDER BY l.created_at DESC";

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ userEmail })) {
            while (c.moveToNext()) {
                list.add(new WritingItem(
                        c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5)
                ));
            }
        }
        return list;
    }

    /** ประวัติการกดใจ (คืน created_at ด้วย) */
    public List<LikeLog> getUserLikeHistory(String userEmail, int limit) {
        ArrayList<LikeLog> out = new ArrayList<>();
        if (userEmail == null || userEmail.trim().isEmpty()) return out;

        String sql = "SELECT id, writing_id, user_email, created_at " +
                "FROM " + TABLE_LIKES_LOG + " WHERE user_email=? " +
                "ORDER BY created_at DESC " + (limit > 0 ? "LIMIT " + limit : "");
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ userEmail })) {
            while (c.moveToNext()) {
                LikeLog log = new LikeLog();
                log.id = c.getInt(0);
                log.writingId = c.getInt(1);
                log.userEmail = c.getString(2);
                log.createdAt = c.getLong(3);
                out.add(log);
            }
        }
        return out;
    }

    // ======================== COMMENTS ========================
    /** เพิ่มคอมเมนต์ */
    public void ensureCommentTables() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS comments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "writing_id INTEGER NOT NULL," +
                "episode_id INTEGER," +                       // อนุญาต NULL ได้
                "user_email TEXT NOT NULL," +
                "content TEXT NOT NULL," +                    // หรือ text TEXT NOT NULL (ให้ใช้ key ให้ตรง)
                "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))" +
                ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_comments_episode ON comments(episode_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_comments_writing ON comments(writing_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_comments_user ON comments(user_email)");

        // ตาราง like คอมเมนต์ (ออปชัน ถ้ายังไม่มี)
        db.execSQL("CREATE TABLE IF NOT EXISTS comment_likes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "comment_id INTEGER NOT NULL," +
                "user_email TEXT NOT NULL," +
                "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                "UNIQUE(comment_id, user_email) ON CONFLICT IGNORE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_comment_likes_c ON comment_likes(comment_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_comment_likes_u ON comment_likes(user_email)");
    }

    public static class CommentRow {
        public int id;
        public int writingId;
        public int episodeId;
        public String userEmail;
        public String userDisplayName;
        public String avatarUrl;     // ถ้าเก็บใน users
        public boolean isWriter;     // author ของ writing?
        public String text;
        public long createdAt;
        public boolean likedByMe;
        public int likeCount;
        public boolean mine;         // เป็นคอมเมนต์ของผู้ใช้เองไหม (ไว้โชว์เมนู delete)
        public String content;
    }

    public long addComment(@Nullable String userEmail, int writingId, @Nullable Integer episodeId, @Nullable String content) {
        // 1) resolve user
        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getLoggedInUserEmail();
            if (userEmail == null || userEmail.trim().isEmpty()) userEmail = "guest";
        }
        userEmail = userEmail.trim();

        // 2) block if banned
        if (!canUserWriteOrComment(userEmail)) {
            Log.e("DBHelper", "addComment blocked: user is banned");
            return -1;
        }

        // 3) normalize content
        if (content == null) content = "";
        content = content.trim();
        if (content.isEmpty()) {
            // ไม่อนุญาตข้อความว่าง
            return -1;
        }
        // (ออปชัน) จำกัดความยาวกันหลุด schema/UX
        final int MAX_LEN = 4000;
        if (content.length() > MAX_LEN) {
            content = content.substring(0, MAX_LEN);
        }

        // 4) write
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", userEmail);
        cv.put("writing_id", writingId);

        if (episodeId == null) {
            cv.putNull("episode_id");        // ให้เป็น NULL ได้
        } else {
            cv.put("episode_id", episodeId);
        }

        // ❗️เลือก key ให้ตรง schema ตาราง comments ของคุณ:
        // - ถ้าคอลัมน์ชื่อ "content" ให้ใช้บรรทัดนี้:
        cv.put("content", content);
        // - ถ้าคอลัมน์ในตารางจริงชื่อ "text" ให้เปลี่ยนเป็นบรรทัดนี้แทน:
        // cv.put("text", content);

        // ใช้วินาทีเพื่อให้สอดคล้อง DEFAULT (strftime('%s','now'))
        long createdAtSec = System.currentTimeMillis() / 1000L;
        cv.put("created_at", createdAtSec);

        long rowId = db.insert(TABLE_COMMENTS, null, cv);
        if (rowId == -1) {
            Log.e("DBHelper", "addComment insert failed for writing_id=" + writingId);
        }
        return rowId;
    }



    public List<CommentRow> getCommentsByEpisodeWithStats(int writingId, int episodeId) {
        String my = getLoggedInUserEmail();
        if (my == null) my = "";

        // ถ้าคุณเก็บ avatar url ใน users.avatar_url ให้ select มาด้วย
        String sql =
                "SELECT c.id, c.writing_id, c.episode_id, c.user_email, c.text, c.created_at, " +
                        "       COALESCE(u.display_name, c.user_email) AS display_name, " +
                        "       u.avatar_url AS avatar_url, " +
                        "       CASE WHEN w.author_email = c.user_email THEN 1 ELSE 0 END AS is_writer, " +
                        "       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id=c.id) AS like_count, " +
                        "       (SELECT COUNT(*) FROM comment_likes cl2 WHERE cl2.comment_id=c.id AND cl2.user_email=?) AS liked_by_me " +
                        "FROM comments c " +
                        "LEFT JOIN users u ON u.email=c.user_email " +
                        "LEFT JOIN writings w ON w.id=c.writing_id " +
                        "WHERE c.writing_id=? AND c.episode_id=? " +
                        "ORDER BY c.created_at ASC";

        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ my, String.valueOf(writingId), String.valueOf(episodeId) });
        List<CommentRow> out = new ArrayList<>();
        while (c.moveToNext()) {
            CommentRow r = new CommentRow();
            r.id = c.getInt(0);
            r.writingId = c.getInt(1);
            r.episodeId = c.getInt(2);
            r.userEmail = c.getString(3);
            r.text = c.getString(4);
            r.createdAt = c.getLong(5);
            r.userDisplayName = c.getString(6);
            r.avatarUrl = c.getString(7);
            r.isWriter = c.getInt(8) == 1;
            r.likeCount = c.getInt(9);
            r.likedByMe = c.getInt(10) > 0;
            r.mine = my.equalsIgnoreCase(r.userEmail);
            out.add(r);
        }
        c.close();
        return out;
    }

    public List<CommentRow> getCommentsByEpisode(int episodeId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.id, c.writing_id, c.episode_id, c.user_email, c.content, c.created_at, " + // หรือ c.text
                        "COALESCE(u.display_name, c.user_email) AS display_name, " +
                        "u.avatar_url, " +
                        "0 AS is_writer " +
                        "FROM comments c LEFT JOIN users u ON u.email=c.user_email " +
                        "WHERE c.episode_id=? ORDER BY c.created_at ASC",
                new String[]{ String.valueOf(episodeId) }
        );
        List<CommentRow> out = new ArrayList<>();
        while (c.moveToNext()) {
            CommentRow r = new CommentRow();
            r.id = c.getInt(0);
            r.writingId = c.getInt(1);
            r.episodeId = c.isNull(2) ? null : c.getInt(2);
            r.userEmail = c.getString(3);
            r.content = c.getString(4); // หรือ r.text = ...
            r.createdAt = c.getLong(5);
            r.userDisplayName = c.getString(6);
            r.avatarUrl = c.getString(7);
            r.isWriter = c.getInt(8) == 1;
            out.add(r);
        }
        c.close();
        return out;
    }



    public boolean toggleCommentLike(int commentId, String userEmail, boolean like) {
        SQLiteDatabase db = getWritableDatabase();
        if (like) {
            ContentValues cv = new ContentValues();
            cv.put("comment_id", commentId);
            cv.put("user_email", userEmail);
            return db.insert("comment_likes", null, cv) != -1;
        } else {
            int n = db.delete("comment_likes", "comment_id=? AND user_email=?",
                    new String[]{ String.valueOf(commentId), userEmail });
            return n > 0;
        }
    }

    public int getCommentLikeCount(int commentId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM comment_likes WHERE comment_id=?",
                new String[]{ String.valueOf(commentId) });
        int k = 0;
        if (c.moveToFirst()) k = c.getInt(0);
        c.close();
        return k;
    }

    public boolean deleteCommentIfOwner(int commentId, String requesterEmail) {
        // ลบได้เฉพาะเจ้าของคอมเมนต์
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT user_email FROM comments WHERE id=?",
                new String[]{ String.valueOf(commentId) });
        String owner = null;
        if (c.moveToFirst()) owner = c.getString(0);
        c.close();
        if (owner != null && owner.equalsIgnoreCase(requesterEmail)) {
            getWritableDatabase().delete("comment_likes", "comment_id=?", new String[]{ String.valueOf(commentId) });
            return getWritableDatabase().delete("comments", "id=?", new String[]{ String.valueOf(commentId) }) > 0;
        }
        return false;
    }
    /** นับคอมเมนต์ของเรื่อง (ใช้โชว์ที่ Book Detail) */
    public int countCommentsForWriting(int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_COMMENTS + " WHERE writing_id=?",
                new String[]{ String.valueOf(writingId) })) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** ประวัติคอมเมนต์ของผู้ใช้ (ล่าสุดก่อน) */
    public List<CommentItem> getUserCommentHistory(String userEmail, int limit) {
        ArrayList<CommentItem> out = new ArrayList<>();
        if (userEmail == null || userEmail.trim().isEmpty()) return out;

        String sql =
                "SELECT c.id, c.writing_id, c.episode_id, c.user_email, c.content, c.created_at, " +
                        "       w.title AS w_title, " +
                        "       (SELECT e.title FROM " + TABLE_EPISODES + " e WHERE e.episode_id=c.episode_id) AS e_title " +
                        "FROM " + TABLE_COMMENTS + " c " +
                        "JOIN " + TABLE_WRITINGS + " w ON w.id=c.writing_id " +
                        "WHERE c.user_email=? " +
                        "ORDER BY c.created_at DESC " + (limit > 0 ? "LIMIT " + limit : "");

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ userEmail })) {
            while (c.moveToNext()) {
                CommentItem ci = new CommentItem();
                ci.id           = c.getInt(0);
                ci.writingId    = c.getInt(1);
                ci.episodeId    = c.isNull(2) ? null : c.getInt(2);
                ci.userEmail    = c.getString(3);
                ci.content      = c.getString(4);
                ci.createdAt    = c.getLong(5);
                ci.writingTitle = c.getString(6);
                ci.episodeTitle = c.getString(7);
                out.add(ci);
            }
        }
        return out;
    }



    public long reportComment(int commentId, @Nullable String reporterEmail, @Nullable String reason) {
        ContentValues cv = new ContentValues();
        cv.put("comment_id", commentId);
        cv.put("reporter_email", reporterEmail);
        cv.put("reason", reason);
        cv.put("created_at", System.currentTimeMillis());
        cv.put("status", "open");
        return getWritableDatabase().insert(TABLE_REPORTS, null, cv);
    }

    public boolean clearReport(int reportId) {
        ContentValues cv = new ContentValues();
        cv.put("status", "cleared");
        return getWritableDatabase().update(TABLE_REPORTS, cv, "id=?", new String[]{ String.valueOf(reportId) }) > 0;
    }

    /** สำหรับหน้า Admin: ดึงรายการรีพอร์ต + ข้อมูลประกอบ */
    public List<ReportItem> getReportedComments() {
        String sql =
                "SELECT r.id, r.comment_id, r.reporter_email, r.reason, r.created_at, r.status, " +
                        "c.content, c.user_email, w.title, " +
                        "(SELECT e.title FROM " + TABLE_EPISODES + " e WHERE e.episode_id=c.episode_id) AS ep_title " +
                        "FROM " + TABLE_REPORTS + " r " +
                        "JOIN " + TABLE_COMMENTS + " c ON c.id=r.comment_id " +
                        "JOIN " + TABLE_WRITINGS + " w ON w.id=c.writing_id " +
                        "WHERE r.status='open' " +
                        "ORDER BY r.created_at DESC";
        ArrayList<ReportItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                ReportItem r = new ReportItem();
                r.id = c.getInt(0);
                r.commentId = c.getInt(1);
                r.reporterEmail = c.isNull(2) ? null : c.getString(2);
                r.reason = c.isNull(3) ? null : c.getString(3);
                r.createdAt = c.getLong(4);
                r.status = c.getString(5);
                r.commentContent = c.getString(6);
                r.commentUserEmail = c.getString(7);
                r.writingTitle = c.getString(8);
                r.episodeTitle = c.getString(9);
                list.add(r);
            }
        }
        return list;
    }

    // ======================== FOLLOWS (เพิ่มแจ้งเตือนเมื่อมีคนกดติดตาม) ========================
    public boolean setFollow(String followerEmail, String authorEmail, boolean enable) {
        if (followerEmail == null || followerEmail.trim().isEmpty()) return false;
        if (authorEmail   == null || authorEmail.trim().isEmpty())   return false;

        SQLiteDatabase db = getWritableDatabase();
        if (enable) {
            ContentValues cv = new ContentValues();
            cv.put("follower_email", followerEmail.trim());
            cv.put("author_email",   authorEmail.trim());
            cv.put("created_at",     System.currentTimeMillis());
            long id = db.insertWithOnConflict(TABLE_FOLLOWS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

            // แจ้งผู้เขียนว่ามีคนติดตาม (ถ้า insert สำเร็จ)
            if (id != -1) {
                String followerName = getUsernameByEmail(followerEmail);
                enqueueNotification(
                        authorEmail,
                        "FOLLOW",
                        "You have a new follower",
                        (followerName != null ? followerName : followerEmail) + " started following you.",
                        "{\"followerEmail\":\"" + followerEmail + "\"}"
                );
            }
            return id != -1 || isFollowing(followerEmail, authorEmail);
        } else {
            return db.delete(TABLE_FOLLOWS, "follower_email=? AND author_email=?",
                    new String[]{followerEmail.trim(), authorEmail.trim()}) > 0;
        }
    }

    public List<String> getFollowersEmails(String authorEmail) {
        ArrayList<String> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT follower_email FROM " + TABLE_FOLLOWS + " WHERE author_email=? ORDER BY created_at DESC",
                new String[]{ authorEmail })) {
            while (c.moveToNext()) list.add(c.getString(0));
        }
        return list;
    }
    // ======================== NOTIFICATIONS ========================
    public long enqueueNotification(String toEmail, String type, @Nullable String title, @Nullable String message, @Nullable String payloadJson) {
        if (toEmail == null || toEmail.trim().isEmpty()) return -1;
        ContentValues cv = new ContentValues();
        cv.put("user_email", toEmail.trim());
        cv.put("type", type != null ? type : "SYSTEM");
        if (title != null)   cv.put("title", title);
        if (message != null) cv.put("message", message);
        if (payloadJson != null) cv.put("payload_json", payloadJson);
        cv.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE_NOTIFICATIONS, null, cv);
    }

    public List<Notification> getUnreadNotifications(String userEmail) {
        ArrayList<Notification> list = new ArrayList<>();
        if (userEmail == null || userEmail.trim().isEmpty()) return list;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, user_email, type, title, message, payload_json, created_at, read_at " +
                        "FROM " + TABLE_NOTIFICATIONS + " WHERE user_email=? AND read_at IS NULL " +
                        "ORDER BY created_at DESC",
                new String[]{ userEmail })) {
            while (c.moveToNext()) {
                Notification n = new Notification();
                n.id = c.getInt(0);
                n.userEmail = c.getString(1);
                n.type = c.getString(2);
                n.title = c.isNull(3) ? null : c.getString(3);
                n.message = c.isNull(4) ? null : c.getString(4);
                n.payloadJson = c.isNull(5) ? null : c.getString(5);
                n.createdAt = c.getLong(6);
                n.readAt = c.isNull(7) ? null : c.getLong(7);
                list.add(n);
            }
        }
        return list;
    }

    public boolean markNotificationRead(int notifId) {
        ContentValues cv = new ContentValues();
        cv.put("read_at", System.currentTimeMillis());
        return getWritableDatabase().update(TABLE_NOTIFICATIONS, cv, "id=?", new String[]{ String.valueOf(notifId) }) > 0;
    }

    // ======================== EPISODES ========================
    public boolean insertEpisode(int writingId, String title, String html, boolean isPrivate) {
        if (!writingExists(writingId)) return false;

        // ตรวจคนเขียนถูกแบน?
        String authorEmail = getAuthorEmailForWriting(writingId);
        if (authorEmail == null || !canUserWriteOrComment(authorEmail)) {
            Log.e("DBHelper", "insertEpisode blocked: author banned or unknown");
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("writing_id", writingId);
        cv.put("title", title);
        cv.put("content_html", html);
        cv.put("privacy_settings", isPrivate ? "private" : "public");

        int nextNo = getMaxEpisodeNoForWriting(writingId) + 1;
        cv.put("episode_no", nextNo);

        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("dd/MM/yy HH:mm:ss 'GMT+7'");
        f.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
        String nowText = f.format(new java.util.Date());
        cv.put("created_at_text", nowText);
        cv.put("updated_at_text", nowText);

        boolean ok;
        try { ok = db.insertOrThrow(TABLE_EPISODES, null, cv) != -1; }
        catch (Exception e) { Log.e("DBHelper", "insertEpisode failed", e); return false; }

        // แจ้งเตือนผู้ติดตาม (เฉพาะ public)
        if (ok && !isPrivate) {
            List<String> followers = getFollowersEmails(authorEmail);
            String wTitle = getWritingTitle(writingId);
            for (String follower : followers) {
                enqueueNotification(
                        follower,
                        "NEW_EPISODE",
                        "New episode: " + (wTitle != null ? wTitle : "New update"),
                        "There's a new episode from " + (getUsernameByEmail(authorEmail) != null ? getUsernameByEmail(authorEmail) : authorEmail),
                        "{\"writingId\":" + writingId + ",\"episodeNo\":" + nextNo + "}"
                );
            }
        }
        return ok;
    }
    public boolean updateEpisode(int episodeId, String title, String html, boolean isPrivate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content_html", html);
        cv.put("privacy_settings", isPrivate ? "private" : "public");

        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("dd/MM/yy HH:mm:ss 'GMT+7'");
        f.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
        cv.put("updated_at_text", f.format(new java.util.Date()));

        return db.update(TABLE_EPISODES, cv, "episode_id=?", new String[]{String.valueOf(episodeId)}) > 0;
    }

    public boolean deleteEpisode(int episodeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EPISODES, "episode_id=?", new String[]{String.valueOf(episodeId)}) > 0;
    }

    public Episode getEpisodeById(int episodeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Episode e = null;
        try (Cursor c = db.rawQuery(
                "SELECT episode_id, writing_id, title, content_html, privacy_settings, episode_no, created_at_text, updated_at_text " +
                        "FROM " + TABLE_EPISODES + " WHERE episode_id=? LIMIT 1",
                new String[]{String.valueOf(episodeId)})) {
            if (c.moveToFirst()) {
                e = new Episode();
                e.episodeId = c.getInt(0);
                e.writingId = c.getInt(1);
                e.title = c.getString(2);
                e.contentHtml = c.getString(3);
                e.isPrivate = "private".equalsIgnoreCase(c.getString(4));
                e.episodeNo = c.isNull(5) ? 0 : c.getInt(5);
                e.createdAt = 0;
                e.updatedAt = 0;
            }
        }
        return e;
    }

    public List<Episode> getEpisodesByWritingId(int writingId) {
        List<Episode> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT episode_id, writing_id, title, content_html, privacy_settings, episode_no, created_at_text, updated_at_text " +
                        "FROM " + TABLE_EPISODES + " WHERE writing_id=? ORDER BY episode_no ASC, episode_id ASC",
                new String[]{String.valueOf(writingId)})) {
            while (c.moveToNext()) {
                Episode e = new Episode();
                e.episodeId = c.getInt(0);
                e.writingId = c.getInt(1);
                e.title = c.getString(2);
                e.contentHtml = c.getString(3);
                e.isPrivate = "private".equalsIgnoreCase(c.getString(4));
                e.episodeNo = c.isNull(5) ? 0 : c.getInt(5);
                e.createdAt = 0;
                e.updatedAt = 0;
                list.add(e);
            }
        }
        return list;
    }

    public void backfillEpisodeNumbersIfNeeded() {
        SQLiteDatabase dbw = getWritableDatabase();
        try (Cursor cw = dbw.rawQuery("SELECT id FROM " + TABLE_WRITINGS, null)) {
            while (cw.moveToNext()) {
                int wid = cw.getInt(0);
                int no = getMaxEpisodeNoForWriting(wid) + 1;
                try (Cursor ce = dbw.rawQuery(
                        "SELECT episode_id FROM " + TABLE_EPISODES +
                                " WHERE writing_id=? AND (episode_no IS NULL OR episode_no=0) ORDER BY episode_id ASC",
                        new String[]{String.valueOf(wid)})) {
                    while (ce.moveToNext()) {
                        int eid = ce.getInt(0);
                        ContentValues cv = new ContentValues();
                        cv.put("episode_no", no++);
                        dbw.update(TABLE_EPISODES, cv, "episode_id=?", new String[]{String.valueOf(eid)});
                    }
                }
            }
        }
    }

    private int getMaxEpisodeNoForWriting(int writingId) {
        int max = 0;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT MAX(episode_no) FROM " + TABLE_EPISODES + " WHERE writing_id=?",
                new String[]{String.valueOf(writingId)})) {
            if (c.moveToFirst()) max = c.isNull(0) ? 0 : c.getInt(0);
        }
        return max;
    }

    private String safeGet(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        return idx >= 0 ? c.getString(idx) : null;
    }

    /** นับจำนวนตอนทั้งหมดของนิยาย */
    public int countEpisodesByWritingId(int writingId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_EPISODES + " WHERE writing_id=?",
                new String[]{ String.valueOf(writingId) })) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }


    private String getWritingTitle(int writingId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT title FROM " + TABLE_WRITINGS + " WHERE id=?",
                new String[]{ String.valueOf(writingId) })) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }


    // ======================== FEED ========================
    public static class EpisodeFeed {
        public int episodeId, writingId, episodeNo;
        public String episodeTitle, writingTitle;
    }

    public List<EpisodeFeed> getEpisodeFeed() {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT e.episode_id, e.writing_id, IFNULL(e.episode_no,0), e.title AS ep_title, w.title AS w_title " +
                        "FROM " + TABLE_EPISODES + " e " +
                        "JOIN " + TABLE_WRITINGS + " w ON e.writing_id = w.id " +
                        "WHERE e.privacy_settings = 'public' " +
                        "ORDER BY e.episode_id DESC";
        List<EpisodeFeed> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                EpisodeFeed f = new EpisodeFeed();
                f.episodeId = c.getInt(0);
                f.writingId = c.getInt(1);
                f.episodeNo = c.getInt(2);
                f.episodeTitle = c.getString(3);
                f.writingTitle = c.getString(4);
                list.add(f);
            }
        }
        return list;
    }

    // ======================== UTIL / SCHEMA ========================
    /** public no-arg wrapper */
    public void ensureEpisodesTable() { ensureEpisodesTable(getWritableDatabase()); }

    private void ensureEpisodesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EPISODES + " (" +
                "episode_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "writing_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "content_html TEXT NOT NULL," +
                "privacy_settings TEXT NOT NULL CHECK(privacy_settings IN ('public','private')) DEFAULT 'public'," +
                "episode_no INTEGER," +
                "created_at_text TEXT NOT NULL," +
                "updated_at_text TEXT NOT NULL," +
                "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_episodes_writing_id ON " + TABLE_EPISODES + "(writing_id)");
    }

    private void ensureBookmarksTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOKMARKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_email TEXT NOT NULL," +
                "writing_id INTEGER NOT NULL," +
                "created_at INTEGER NOT NULL," +
                "UNIQUE(user_email, writing_id) ON CONFLICT IGNORE," +
                "FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_bmk_writing ON " + TABLE_BOOKMARKS + "(writing_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_bmk_user ON " + TABLE_BOOKMARKS + "(user_email)");
    }

    private void ensureFollowsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FOLLOWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "follower_email TEXT NOT NULL," +
                "author_email   TEXT NOT NULL," +
                "created_at     INTEGER NOT NULL," +
                "UNIQUE(follower_email, author_email) ON CONFLICT IGNORE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_follows_follower ON " + TABLE_FOLLOWS + "(follower_email)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_follows_author   ON " + TABLE_FOLLOWS + "(author_email)");
    }

    private void ensureReportsTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_REPORTS + " (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  comment_id INTEGER," +                // คอมเมนต์ที่ถูกรายงาน (อาจเป็น null ถ้ารายงานทั้งเรื่อง)
                        "  writing_id INTEGER," +                // เรื่องที่เกี่ยวข้อง
                        "  reporter_email TEXT NOT NULL," +      // ใครกด report
                        "  reason TEXT," +                       // เหตุผลที่รายงาน
                        "  status TEXT NOT NULL DEFAULT 'OPEN'," + // OPEN / RESOLVED
                        "  created_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                        "  resolved_at INTEGER," +
                        "  moderator_note TEXT," +
                        "  FOREIGN KEY (comment_id) REFERENCES " + TABLE_COMMENTS + "(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (writing_id) REFERENCES " + TABLE_WRITINGS + "(id) ON DELETE CASCADE" +
                        ")"
        );
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reports_status ON " + TABLE_REPORTS + "(status)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reports_created ON " + TABLE_REPORTS + "(created_at DESC)");
    }


    private void safeAddColumn(SQLiteDatabase db, String table, String column, String typeAndDefault) {
        if (!hasColumn(db, table, column)) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + typeAndDefault);
        }
    }

    private void ensureColumnExists(SQLiteDatabase db, String table, String column, String typeAndDefault) {
        safeAddColumn(db, table, column, typeAndDefault);
    }

    private boolean hasColumn(SQLiteDatabase db, String table, String column) {
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                if (column.equalsIgnoreCase(name)) return true;
            }
        } finally { if (c != null) c.close(); }
        return false;
    }

    // นับจำนวนผู้ใช้ทั้งหมด (กันค่า null/email ว่าง)
    public int countAllUsers() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE IFNULL(email,'') <> ''", null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // นับจำนวนหนังสือ/งานเขียนทั้งหมด
    public int countAllWritings() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_WRITINGS, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public java.util.List<User> getAllUsers() {
        java.util.ArrayList<User> list = new java.util.ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, username, email, role FROM " + TABLE_USERS + " ORDER BY id DESC", null)) {
            while (c.moveToNext()) {
                Integer id   = c.getInt(0);
                String uname = c.isNull(1) ? null : c.getString(1);
                String email = c.isNull(2) ? null : c.getString(2);
                String role  = c.isNull(3) ? null : c.getString(3);
                String display = uname;
                boolean active = true;
                list.add(new User(id, null, uname, display, email, active));
            }
        }
        return list;
    }

    public @Nullable User getUserByIdInt(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, username, email, role FROM " + TABLE_USERS + " WHERE id=? LIMIT 1",
                new String[]{ String.valueOf(id) })) {
            if (c.moveToFirst()) {
                Integer uid   = c.getInt(0);
                String uname  = c.isNull(1) ? null : c.getString(1);
                String email  = c.isNull(2) ? null : c.getString(2);
                String role   = c.isNull(3) ? null : c.getString(3);
                String display = uname;
                boolean active = true;
                return new User(uid, null, uname, display, email, active);
            }
        }
        return null;
    }

    // ==== MODELS สำหรับหน้า User Details ====
    public static class LikeRecord {
        public int writingId;
        public String writingTitle;
        public long createdAt;
    }
    public static class CommentRecord {
        public int writingId;
        public @Nullable Integer episodeId;
        public String writingTitle;
        public @Nullable String episodeTitle;
        public String content;
        public long createdAt;
    }

    /** ประวัติกดใจของ user: ดึงชื่อเรื่อง + เวลากดใจ */
    public List<LikeRecord> getUserLikeHistory(String userEmail) {
        ArrayList<LikeRecord> out = new ArrayList<>();
        if (userEmail == null || userEmail.trim().isEmpty()) return out;
        String sql =
                "SELECT w.id, w.title, l.created_at " +
                        "FROM " + TABLE_LIKES_LOG + " l " +
                        "JOIN " + TABLE_WRITINGS + " w ON w.id = l.writing_id " +
                        "WHERE l.user_email=? " +
                        "ORDER BY l.created_at DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ userEmail })) {
            while (c.moveToNext()) {
                LikeRecord r = new LikeRecord();
                r.writingId   = c.getInt(0);
                r.writingTitle= c.getString(1);
                r.createdAt   = c.getLong(2);
                out.add(r);
            }
        }
        return out;
    }

    /** ประวัติคอมเมนต์ของ user: ดึงชื่อเรื่อง/ตอน + ข้อความ + เวลา */
    public List<CommentRecord> getUserCommentHistory(String userEmail) {
        ArrayList<CommentRecord> out = new ArrayList<>();
        if (userEmail == null || userEmail.trim().isEmpty()) return out;
        String sql =
                "SELECT c.writing_id, c.episode_id, w.title, " +
                        "       (SELECT e.title FROM " + TABLE_EPISODES + " e WHERE e.episode_id=c.episode_id) AS ep_title, " +
                        "       c.content, c.created_at " +
                        "FROM " + TABLE_COMMENTS + " c " +
                        "JOIN " + TABLE_WRITINGS + " w ON w.id=c.writing_id " +
                        "WHERE c.user_email=? " +
                        "ORDER BY c.created_at DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ userEmail })) {
            while (c.moveToNext()) {
                CommentRecord r = new CommentRecord();
                r.writingId     = c.getInt(0);
                r.episodeId     = c.isNull(1) ? null : c.getInt(1);
                r.writingTitle  = c.getString(2);
                r.episodeTitle  = c.getString(3);
                r.content       = c.getString(4);
                r.createdAt     = c.getLong(5);
                out.add(r);
            }
        }
        return out;
    }
    // ดึงรายชื่อ username ที่ไม่ว่าง (unique + เรียงชื่อ)
    public List<String> getAllUsernamesNonEmpty() {
        ArrayList<String> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT username FROM " + TABLE_USERS + " " +
                        "WHERE username IS NOT NULL AND TRIM(username) <> '' " +
                        "ORDER BY LOWER(username) ASC", null)) {
            while (c.moveToNext()) list.add(c.getString(0));
        }
        return list;
    }

    /** ดึงหมวดหมู่หนังสือ (category) ที่ไม่ว่าง/ไม่เป็น null แบบไม่ซ้ำ และเรียง A→Z */
    public java.util.List<String> getAllCategoriesNonEmpty() {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT TRIM(category) AS cat " +
                        "FROM " + TABLE_WRITINGS + " " +
                        "WHERE category IS NOT NULL AND TRIM(category) <> '' " +
                        "ORDER BY LOWER(cat) ASC", null)) {
            while (c.moveToNext()) {
                list.add(c.getString(0));
            }
        }
        return list;
    }
    public long insertBanner(String imagePath, @Nullable String title, @Nullable String deeplink, boolean active){
        ContentValues cv = new ContentValues();
        cv.put("image_path", imagePath);
        cv.put("title", title);
        cv.put("deeplink", deeplink);
        cv.put("is_active", active ? 1 : 0);
        return getWritableDatabase().insert(TABLE_BANNERS, null, cv);
    }

    public List<Banner> getActiveBanners(){
        ArrayList<Banner> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, image_path, title, deeplink, is_active FROM " + TABLE_BANNERS +
                        " WHERE is_active=1 ORDER BY id DESC", null)) {
            while (c.moveToNext()){
                Banner b = new Banner();
                b.id = c.getInt(0);
                b.imagePath = c.getString(1);
                b.title = c.isNull(2) ? null : c.getString(2);
                b.deeplink = c.isNull(3) ? null : c.getString(3);
                b.active = c.getInt(4) == 1;
                list.add(b);
            }
        }
        return list;
    }

    /** ดึงรายการรีพอร์ตสถานะ OPEN พร้อมข้อมูลคอมเมนต์/เจ้าของ/ชื่องานเขียน */
    public android.database.Cursor getOpenReportsCursor() {
        String sql =
                "SELECT r.id, r.reason, r.reporter_email, r.created_at, " +
                        "       c.content AS comment_text, " +
                        "       c.user_email AS comment_owner, " +
                        "       w.title AS writing_title " +
                        "FROM " + TABLE_REPORTS + " r " +
                        "LEFT JOIN " + TABLE_COMMENTS + " c ON c.id = r.comment_id " +
                        "LEFT JOIN " + TABLE_WRITINGS + " w ON w.id = r.writing_id " +
                        "WHERE r.status = 'OPEN' " +
                        "ORDER BY r.id DESC";
        return getReadableDatabase().rawQuery(sql, null);
    }

    /** ปิดเคสรีพอร์ต (เปลี่ยนสถานะเป็น RESOLVED + บันทึกเวลา/โน้ตผู้ดูแล) */
    public boolean resolveReport(int reportId, @Nullable String moderatorNote) {
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("status", "RESOLVED");
        cv.put("resolved_at", System.currentTimeMillis() / 1000L); // เก็บเป็นวินาที
        if (moderatorNote != null) cv.put("moderator_note", moderatorNote);
        return getWritableDatabase().update(TABLE_REPORTS, cv, "id=?",
                new String[]{ String.valueOf(reportId) }) > 0;
    }
}