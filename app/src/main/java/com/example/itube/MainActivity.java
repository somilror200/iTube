package com.example.itube;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, signupButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this); // Initialize database helper

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Check if username and password are not empty
                if (!username.isEmpty() && !password.isEmpty()) {
                    // Check if user exists in the database
                    if (dbHelper.checkUser(username, password)) {
                        // Successful login, retrieve user ID and save it in shared preferences
                        long userId = dbHelper.getUserId(username);
                        if (userId != -1) {
                            saveUserId(userId);
                            // Navigate to main functionality or another activity
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Invalid username or password
                        Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Username or password is empty
                    Toast.makeText(MainActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveUserId(long userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.key_current_user_id), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(getString(R.string.key_current_user_id), userId);
        editor.apply();
        Log.i("MainActivity", "Saved user ID: " + userId);
    }

    public static class PlaylistActivity extends AppCompatActivity {

        DatabaseHelper dbHelper;
        ListView listViewPlaylist;
        ArrayList<String> playlistItems;
        ArrayAdapter<String> playlistAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_playlist);

            dbHelper = new DatabaseHelper(this);
            listViewPlaylist = findViewById(R.id.listViewPlaylist);

            // Initialize ArrayList to store playlist items
            playlistItems = new ArrayList<>();

            // Retrieve playlist data for the current user
            long userId = getCurrentUserId();
            if (userId != -1) {
                Cursor cursor = dbHelper.getPlaylist(userId);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // Retrieve video URL from the cursor and add it to the playlistItems list
                        String videoUrl = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_URL));
                        playlistItems.add(videoUrl);
                    } while (cursor.moveToNext());
                    cursor.close();

                    // Display playlistItems in the ListView using ArrayAdapter
                    playlistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistItems);
                    listViewPlaylist.setAdapter(playlistAdapter);
                } else {
                    Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show();
            }
        }

        private long getCurrentUserId() {
            SharedPreferences sharedPreferences = getSharedPreferences(
                    getString(R.string.key_current_user_id), MODE_PRIVATE);
            long userId = sharedPreferences.getLong(getString(R.string.key_current_user_id), -1);
            return userId;
        }
    }
}

