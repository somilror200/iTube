package com.example.itube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.VideoViewHolder> {

    public interface Listener {
        void onVideoClick(String videoUrl);
        void onVideoLongClick(String videoUrl);
    }

    private static final ExecutorService IMAGE_EXECUTOR = Executors.newFixedThreadPool(3);
    private static final LruCache<String, Bitmap> IMAGE_CACHE = new LruCache<>(24);

    private final List<String> items = new ArrayList<>();
    private final Listener listener;

    public PlaylistAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitItems(List<String> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String videoUrl = items.get(position);
        String videoId = VideoPlayerActivity.extractVideoId(videoUrl);

        holder.videoId.setText(videoId == null ? videoUrl : videoId);
        holder.thumbnail.setImageDrawable(null);
        holder.thumbnail.setTag(videoUrl);
        loadThumbnail(holder.thumbnail, videoUrl, videoId);

        holder.itemView.setOnClickListener(v -> listener.onVideoClick(videoUrl));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onVideoLongClick(videoUrl);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void loadThumbnail(ImageView imageView, String videoUrl, String videoId) {
        if (videoId == null) {
            return;
        }

        Bitmap cached = IMAGE_CACHE.get(videoId);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        IMAGE_EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5_000);
                connection.setReadTimeout(5_000);
                connection.setUseCaches(true);

                try (InputStream inputStream = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        IMAGE_CACHE.put(videoId, bitmap);
                        imageView.post(() -> {
                            Object tag = imageView.getTag();
                            if (videoUrl.equals(tag)) {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            } catch (Exception ignored) {
                // Thumbnail failure should never prevent playlist use.
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView videoId;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.imageThumbnail);
            videoId = itemView.findViewById(R.id.textVideoId);
        }
    }
}
