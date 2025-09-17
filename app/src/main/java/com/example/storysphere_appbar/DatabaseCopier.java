package com.example.storysphere_appbar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseCopier {
    private static final String TAG = "DatabaseCopier";

    /**
     * คัดลอกไฟล์ DB จาก assets ไปไว้ใน /data/data/<package>/databases/ ถ้ายังไม่มี
     * @param ctx          Context ของแอป (เช่น Activity นี้)
     * @param assetDbName  ชื่อไฟล์ DB ใน assets (ต้องตรงกับไฟล์ที่วางใน assets)
     */
    public static void copyIfNeeded(Context ctx, String assetDbName) {
        // ไฟล์ปลายทาง: /data/data/<package>/databases/<assetDbName>
        File outFile = ctx.getDatabasePath(assetDbName);
        if (outFile.exists()) return; // เคยคัดลอกแล้ว ข้ามได้เลย

        try {
            // ให้แน่ใจว่าโฟลเดอร์ /databases ถูกสร้าง
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            // คัดลอกจาก assets -> databases
            InputStream is = ctx.getAssets().open(assetDbName);
            OutputStream os = new FileOutputStream(outFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
            os.flush();
            os.close();
            is.close();

            // เปิด/ปิดหนึ่งครั้งให้ SQLite ลงทะเบียนไฟล์
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    outFile.getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READWRITE
            );
            db.close();

            Log.i(TAG, "Copied DB to: " + outFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Copy DB failed", e);
        }
    }
}
