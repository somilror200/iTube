# iTube — Android Video Playlist App

A native Android app built in **Java** that lets users create a local account, play YouTube videos from a URL or video ID, and maintain a personal on-device playlist.

This project started as an Android coursework app and has since been refactored into a cleaner portfolio project with stronger validation, safer local authentication, improved playlist behaviour and a Material 3 interface.

## Features

- Local account registration and sign-in
- Passwords stored as salted PBKDF2 hashes rather than plaintext
- Case-insensitive unique usernames
- YouTube URL parsing for standard, short, Shorts and embed links
- Full-screen in-app video playback using WebView + YouTube IFrame API
- Per-user SQLite playlists
- Duplicate playlist protection
- Tap a saved item to play it
- Long-press a saved item to remove it
- Persistent local user session
- Sign-out flow
- Material 3 light/dark theme support
- Input validation and user-friendly errors

## Tech Stack

- **Java**
- **Android SDK**
- **Material 3 / XML layouts**
- **SQLite / SQLiteOpenHelper**
- **PBKDF2 password hashing**
- **WebView + YouTube IFrame API**
- **Gradle Kotlin DSL**

## Project Structure

```text
app/src/main/java/com/example/itube/
├── MainActivity.java            # Sign-in and session creation
├── SignUpActivity.java          # Account registration and validation
├── MainPageActivity.java        # Video actions and navigation
├── UserPlayListActivity.java    # Saved videos, playback and deletion
├── VideoPlayerActivity.java     # URL parsing and embedded playback
└── DatabaseHelper.java          # SQLite users, playlists and auth storage
```

## Security Improvements

The original version stored passwords directly in SQLite. That has been removed.

The current implementation:

- creates a random salt for each password
- derives a password hash using PBKDF2
- compares hashes using `MessageDigest.isEqual`
- prevents the app database from being backed up
- upgrades the old v1 database by removing the insecure credential schema

This is still a local demonstration app rather than a production authentication service. A production app would normally use a backend identity provider or platform authentication rather than implementing user credentials entirely on-device.

## YouTube Playback

The app accepts:

- `youtube.com/watch?v=...`
- `youtu.be/...`
- `youtube.com/shorts/...`
- `youtube.com/embed/...`
- a raw 11-character YouTube video ID

The input is reduced to a validated video ID before being injected into the player HTML.

## Run Locally

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync complete.
4. Run on an Android emulator or device running Android 7.0 (API 24) or newer.
5. Create a local account, sign in and paste a YouTube link.

Internet access is required for video playback.

## Current Scope

The project intentionally keeps its data local to demonstrate native Android fundamentals including activities, SQLite persistence, input validation, session state and WebView integration.

Potential future improvements include Room, MVVM, RecyclerView playlist cards, video metadata/thumbnails and instrumentation tests.

## Author

**Somil Garak**  
Computer Science — Deakin University  
GitHub: [somilror200](https://github.com/somilror200)
