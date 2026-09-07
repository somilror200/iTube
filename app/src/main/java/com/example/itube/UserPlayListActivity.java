package com.example.itube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserPlayListActivity extends AppCompatActivity {

    private final ArrayList<String> playlistItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_playlist);

        dbHelper = new DatabaseHelper(this);
        userId = getCurrentUserId();
        if (userId < 0) {
            finish();
            return;
        }

        ListView listViewPlaylist = findViewById(R.id.listViewPlaylist);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistItems);
        listViewPlaylist.setAdapter(adapter);

        listViewPlaylist.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, playlistItems.get(position));
            startActivity(intent);
        });

        listViewPlaylist.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmRemoval(playlistItems.get(position));
            return true;
        });

        loadPlaylist();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadPlaylist();
        }
    }

    private void loadPlaylist() {
        playlistItems.clear();

        try (Cursor cursor = dbHelper.getPlaylist(userId)) {
            while (cursor.moveToNext()) {
                playlistItems.add(cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VIDEO_URL)));
            }
        }

        adapter.notifyDataSetChanged();
        if (playlistItems.isEmpty()) {
            Toast.makeText(this, "Your playlist is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmRemoval(String videoUrl) {
        new AlertDialog.Builder(this)
                .setTitle("Remove video?")
                .setMessage("This will remove the video from your saved playlist.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (dbHelper.removeFromPlaylist(userId, videoUrl)) {
                        loadPlaylist();
                        Toast.makeText(this, "Removed from playlist", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private long getCurrentUserId() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.session_preferences), MODE_PRIVATE);
        return preferences.getLong(getString(R.string.key_current_user_id), -1);
    }
}
