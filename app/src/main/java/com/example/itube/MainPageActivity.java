package com.example.itube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    EditText editTextVideoUrl;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        dbHelper = new DatabaseHelper(this);

        editTextVideoUrl = findViewById(R.id.editTextVideoUrl);
        Button playButton = findViewById(R.id.buttonPlay);
        Button addToPlaylistButton = findViewById(R.id.buttonAddToPlaylist);
        Button myPlaylistButton = findViewById(R.id.buttonMyPlaylist);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoUrl = editTextVideoUrl.getText().toString();
                // Start VideoPlayerActivity with the entered video URL
                Intent intent = new Intent(MainPageActivity.this, VideoPlayerActivity.class);
                intent.putExtra("videoUrl", videoUrl);
                startActivity(intent);
            }
        });

        addToPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoUrl = editTextVideoUrl.getText().toString();
                long userId = getCurrentUserId(); // Get the current user's ID
                Log.i("MainPageActivity", "Retrieved user ID: " + userId);
                long result = dbHelper.addToPlaylist(userId, videoUrl);
                if (result != -1) {
                    Toast.makeText(MainPageActivity.this, "Video added to playlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainPageActivity.this, "Failed to add video to playlist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, MainActivity.PlaylistActivity.class);
                startActivity(intent);
            }
        });
    }

    private long getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.key_current_user_id), MODE_PRIVATE);
        long userId = sharedPreferences.getLong(getString(R.string.key_current_user_id), -1);
        return userId;
    }
}

