package com.example.itube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 2;

    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password_hash";
    public static final String COLUMN_VIDEO_URL = "video_url";
    public static final String COLUMN_USER_ID = "user_id";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_PLAYLIST =
            "CREATE TABLE " + TABLE_PLAYLIST + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_VIDEO_URL + " TEXT NOT NULL, " +
                    "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_VIDEO_URL + "), " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PLAYLIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 stored plaintext passwords. For this local portfolio/demo app,
        // discard that insecure schema rather than carrying plaintext credentials forward.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long addUser(String name, String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.put(COLUMN_USERNAME, username.trim());
        values.put(COLUMN_PASSWORD, hashPassword(password));
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ? COLLATE NOCASE",
                new String[]{username.trim()},
                null,
                null,
                null,
                "1")) {
            return cursor.moveToFirst();
        }
    }

    public long addToPlaylist(long userId, String videoUrl) {
        if (userId < 0 || videoUrl == null || videoUrl.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_VIDEO_URL, videoUrl.trim());
        return db.insertWithOnConflict(TABLE_PLAYLIST, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean removeFromPlaylist(long userId, String videoUrl) {
        SQLiteDatabase db = getWritableDatabase();
        int deleted = db.delete(
                TABLE_PLAYLIST,
                COLUMN_USER_ID + " = ? AND " + COLUMN_VIDEO_URL + " = ?",
                new String[]{String.valueOf(userId), videoUrl});
        return deleted > 0;
    }

    public Cursor getPlaylist(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_PLAYLIST,
                new String[]{COLUMN_ID, COLUMN_VIDEO_URL},
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_ID + " DESC");
    }

    public long getUserId(String username) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ? COLLATE NOCASE",
                new String[]{username.trim()},
                null,
                null,
                null,
                "1")) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        }
        return -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_PASSWORD},
                COLUMN_USERNAME + " = ? COLLATE NOCASE",
                new String[]{username.trim()},
                null,
                null,
                null,
                "1")) {
            if (!cursor.moveToFirst()) {
                return false;
            }
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            return verifyPassword(password, storedHash);
        }
    }

    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt);
            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    private boolean verifyPassword(String password, String storedValue) {
        try {
            String[] parts = storedValue.split(":", 2);
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] actualHash = pbkdf2(password.toCharArray(), salt);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
