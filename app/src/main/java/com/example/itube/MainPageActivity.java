package com.example.itube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    private EditText videoUrlEditText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        dbHelper = new DatabaseHelper(this);
        videoUrlEditText = findViewById(R.id.editTextVideoUrl);
        Button playButton = findViewById(R.id.buttonPlay);
        Button addToPlaylistButton = findViewById(R.id.buttonAddToPlaylist);
        Button myPlaylistButton = findViewById(R.id.buttonMyPlaylist);
        Button logoutButton = findViewById(R.id.buttonLogout);

        playButton.setOnClickListener(v -> playEnteredVideo());
        addToPlaylistButton.setOnClickListener(v -> addEnteredVideoToPlaylist());
        myPlaylistButton.setOnClickListener(v ->
                startActivity(new Intent(this, UserPlayListActivity.class)));
        logoutButton.setOnClickListener(v -> logout());
    }

    private void playEnteredVideo() {
        String normalizedUrl = getValidatedVideoUrl();
        if (normalizedUrl == null) {
            return;
        }

        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, normalizedUrl);
        startActivity(intent);
    }

    private void addEnteredVideoToPlaylist() {
        String normalizedUrl = getValidatedVideoUrl();
        if (normalizedUrl == null) {
            return;
        }

        long userId = getCurrentUserId();
        if (userId < 0) {
            logout();
            return;
        }

        long result = dbHelper.addToPlaylist(userId, normalizedUrl);
        if (result < 0) {
            Toast.makeText(this, "This video is already in your playlist", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show();
            videoUrlEditText.setText("");
        }
    }

    private String getValidatedVideoUrl() {
        videoUrlEditText.setError(null);
        String input = videoUrlEditText.getText().toString().trim();
        String videoId = VideoPlayerActivity.extractVideoId(input);

        if (videoId == null) {
            videoUrlEditText.setError("Enter a valid YouTube URL or video ID");
            videoUrlEditText.requestFocus();
            return null;
        }

        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private long getCurrentUserId() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.session_preferences), MODE_PRIVATE);
        return preferences.getLong(getString(R.string.key_current_user_id), -1);
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.session_preferences), MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
