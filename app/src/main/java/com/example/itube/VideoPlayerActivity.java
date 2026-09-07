package com.example.itube;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "videoUrl";

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?(?:.*&)?v=|embed/|shorts/|v/))([A-Za-z0-9_-]{11})",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RAW_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        webView = findViewById(R.id.webView);
        configureWebView();

        String videoId = extractVideoId(getIntent().getStringExtra(EXTRA_VIDEO_URL));
        if (videoId == null) {
            Toast.makeText(this, "Invalid YouTube video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadVideo(videoId);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void loadVideo(String videoId) {
        String html = "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'>" +
                "<style>html,body,#player{margin:0;width:100%;height:100%;background:#000;overflow:hidden}</style>" +
                "</head><body>" +
                "<div id='player'></div>" +
                "<script src='https://www.youtube.com/iframe_api'></script>" +
                "<script>" +
                "var player;" +
                "function onYouTubeIframeAPIReady(){" +
                "player=new YT.Player('player',{" +
                "videoId:'" + videoId + "'," +
                "playerVars:{playsinline:1,rel:0}," +
                "events:{onReady:function(e){e.target.playVideo();}}" +
                "});}" +
                "</script></body></html>";

        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
    }

    public static String extractVideoId(String input) {
        if (input == null) {
            return null;
        }

        String value = input.trim();
        if (RAW_ID_PATTERN.matcher(value).matches()) {
            return value;
        }

        Matcher matcher = VIDEO_ID_PATTERN.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
