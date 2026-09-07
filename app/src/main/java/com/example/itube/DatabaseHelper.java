package com.example.itube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password_hash";
    public static final String COLUMN_VIDEO_URL = "video_url";
    public static final String COLUMN_USER_ID = "user_id";

    private static volatile DatabaseHelper instance;

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

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context);
                }
            }
        }
        return instance;
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
        int migratedVersion = oldVersion;

        if (migratedVersion < 2) {
            // Version 1 stored plaintext passwords. This one-time migration intentionally
            // removes the insecure credential schema instead of preserving plaintext secrets.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
            migratedVersion = 2;
        }

        // Future schema versions must add explicit, data-preserving migrations here.
        // Failing loudly is safer than silently deleting user data.
        if (migratedVersion != newVersion) {
            throw new IllegalStateException(
                    "Missing database migration from " + migratedVersion + " to " + newVersion);
        }
    }

    public long addUser(String name, String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.put(COLUMN_USERNAME, username.trim());
        values.put(COLUMN_PASSWORD, hashPassword(password));

        try {
            return db.insertOrThrow(TABLE_USERS, null, values);
        } catch (SQLiteConstraintException e) {
            // The UNIQUE username constraint remains the final protection against
            // two near-simultaneous signup attempts using the same username.
            return -1;
        }
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

    public long authenticateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_PASSWORD},
                COLUMN_USERNAME + " = ? COLLATE NOCASE",
                new String[]{username.trim()},
                null,
                null,
                null,
                "1")) {
            if (!cursor.moveToFirst()) {
                return -1;
            }

            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            if (!verifyPassword(password, storedHash)) {
                return -1;
            }

            return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
    }

    public boolean checkUser(String username, String password) {
        return authenticateUser(username, password) >= 0;
    }

    public long addToPlaylist(long userId, String videoUrl) {
        if (userId < 0 || videoUrl == null || videoUrl.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_VIDEO_URL, videoUrl.trim());
        return db.insertWithOnConflict(
                TABLE_PLAYLIST,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean removeFromPlaylist(long userId, String videoUrl) {
        SQLiteDatabase db = getWritableDatabase();
        int deleted = db.delete(
                TABLE_PLAYLIST,
                COLUMN_USER_ID + " = ? AND " + COLUMN_VIDEO_URL + " = ?",
                new String[]{String.valueOf(userId), videoUrl});
        return deleted > 0;
    }

    public List<String> getPlaylistItems(long userId) {
        ArrayList<String> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_PLAYLIST,
                new String[]{COLUMN_VIDEO_URL},
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_ID + " DESC")) {
            while (cursor.moveToNext()) {
                items.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_URL)));
            }
        }

        return items;
    }

    private String hashPassword(String password) {
        byte[] salt = PasswordHasher.generateSalt();
        byte[] hash = PasswordHasher.derive(password.toCharArray(), salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    private boolean verifyPassword(String password, String storedValue) {
        try {
            String[] parts = storedValue.split(":", 2);
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);
            return PasswordHasher.matches(password.toCharArray(), salt, expectedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
