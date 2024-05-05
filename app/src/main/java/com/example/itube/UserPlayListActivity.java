package com.example.itube;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserPlayListActivity extends AppCompatActivity {

    private ListView listViewPlaylist;
    private ArrayList<String> playlistItems;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_playlist);

        dbHelper = new DatabaseHelper(this);

        listViewPlaylist = findViewById(R.id.listViewPlaylist);
        playlistItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlistItems);
        listViewPlaylist.setAdapter(adapter);

        // Retrieve the current user's playlist from the database
        long userId = getCurrentUserId();
        Cursor cursor = dbHelper.getPlaylist(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Add each video URL to the playlistItems ArrayList
                String videoUrl = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_URL));
                playlistItems.add(videoUrl);
            } while (cursor.moveToNext());

            // Update the ListView with the playlist items
            adapter.notifyDataSetChanged();
        } else {
            // No items found in the playlist
            Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show();
        }
    }
    private long getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.key_current_user_id), MODE_PRIVATE);
        long userId = sharedPreferences.getLong(getString(R.string.key_current_user_id), -1);
        return userId;
    }
}
