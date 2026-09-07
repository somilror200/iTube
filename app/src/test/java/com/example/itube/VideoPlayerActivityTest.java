package com.example.itube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class VideoPlayerActivityTest {

    private static final String VIDEO_ID = "dQw4w9WgXcQ";

    @Test
    public void extractsStandardWatchUrl() {
        assertEquals(VIDEO_ID,
                VideoPlayerActivity.extractVideoId("https://www.youtube.com/watch?v=" + VIDEO_ID));
    }

    @Test
    public void extractsShortUrl() {
        assertEquals(VIDEO_ID,
                VideoPlayerActivity.extractVideoId("https://youtu.be/" + VIDEO_ID));
    }

    @Test
    public void extractsShortsUrl() {
        assertEquals(VIDEO_ID,
                VideoPlayerActivity.extractVideoId("https://www.youtube.com/shorts/" + VIDEO_ID));
    }

    @Test
    public void acceptsRawVideoId() {
        assertEquals(VIDEO_ID, VideoPlayerActivity.extractVideoId(VIDEO_ID));
    }

    @Test
    public void rejectsInvalidInput() {
        assertNull(VideoPlayerActivity.extractVideoId("not-a-youtube-link"));
    }
}
