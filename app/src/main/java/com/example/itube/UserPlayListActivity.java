package com.example.itube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserPlayListActivity extends AppCompatActivity {

    private PlaylistAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView emptyState;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_playlist);

        dbHelper = DatabaseHelper.getInstance(this);
        userId = getCurrentUserId();
        if (userId < 0) {
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPlaylist);
        emptyState = findViewById(R.id.textEmptyPlaylist);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        adapter = new PlaylistAdapter(new PlaylistAdapter.Listener() {
            @Override
            public void onVideoClick(String videoUrl) {
                Intent intent = new Intent(UserPlayListActivity.this, VideoPlayerActivity.class);
                intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, videoUrl);
                startActivity(intent);
            }

            @Override
            public void onVideoLongClick(String videoUrl) {
                confirmRemoval(videoUrl);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadPlaylist();
        }
    }

    private void loadPlaylist() {
        AppExecutors.database().execute(() -> {
            List<String> items = dbHelper.getPlaylistItems(userId);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                adapter.submitItems(items);
                emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void confirmRemoval(String videoUrl) {
        new AlertDialog.Builder(this)
                .setTitle("Remove video?")
                .setMessage("This will remove the video from your saved playlist.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) ->
                        AppExecutors.database().execute(() -> {
                            boolean removed = dbHelper.removeFromPlaylist(userId, videoUrl);

                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) {
                                    return;
                                }

                                if (removed) {
                                    Toast.makeText(this, "Removed from playlist", Toast.LENGTH_SHORT).show();
                                    loadPlaylist();
                                }
                            });
                        }))
                .show();
    }

    private long getCurrentUserId() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.session_preferences), MODE_PRIVATE);
        return preferences.getLong(getString(R.string.key_current_user_id), -1);
    }
}
