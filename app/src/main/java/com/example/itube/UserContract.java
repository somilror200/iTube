package com.example.itube;

public final class UserContract {

    private UserContract() {} // Prevent instantiation

    public static class UserEntry {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
    }
}
