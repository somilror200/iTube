# iTube — Android Video Playlist App

[![Android CI](https://github.com/somilror200/iTube/actions/workflows/android-ci.yml/badge.svg)](https://github.com/somilror200/iTube/actions/workflows/android-ci.yml)

A native Android app built in **Java** that lets users create a local account, play YouTube videos from a URL or video ID, and maintain a personal on-device playlist.

This project started as an Android coursework app and has since been refactored into a cleaner portfolio project with stronger validation, safer local authentication, background database work, improved playlist behaviour and a Material 3 interface.

## Features

- Local account registration and sign-in
- Passwords stored as salted PBKDF2 hashes rather than plaintext
- Case-insensitive unique usernames
- YouTube URL parsing for standard, short, Shorts and embed links
- Full-screen in-app video playback using WebView + YouTube IFrame API
- Per-user SQLite playlists
- RecyclerView playlist cards with YouTube thumbnails
- Duplicate playlist protection
- Tap a saved item to play it
- Long-press a saved item to remove it
- Persistent local user session
- Sign-out flow
- Material 3 light/dark theme support
- Input validation and user-friendly errors
- Database and PBKDF2 work moved off the Android main thread
- Automated unit tests, lint and APK build through GitHub Actions

## Tech Stack

- **Java**
- **Android SDK**
- **Material 3 / XML layouts**
- **RecyclerView**
- **SQLite / SQLiteOpenHelper**
- **PBKDF2 password hashing**
- **WebView + YouTube IFrame API**
- **Gradle Kotlin DSL**
- **GitHub Actions**

## Project Structure

```text
app/src/main/java/com/example/itube/
├── MainActivity.java            # Sign-in and session creation
├── SignUpActivity.java          # Account registration and validation
├── MainPageActivity.java        # Video actions and navigation
├── UserPlayListActivity.java    # Saved-video screen and DB coordination
├── PlaylistAdapter.java         # RecyclerView cards and thumbnail loading
├── VideoPlayerActivity.java     # URL parsing and embedded playback
├── DatabaseHelper.java          # SQLite users, playlists and auth storage
├── PasswordHasher.java          # PBKDF2 derivation and verification
└── AppExecutors.java            # Serialized background DB executor
```

## Security Improvements

The original version stored passwords directly in SQLite. That has been removed.

The current implementation:

- creates a random salt for each password
- derives a password hash using PBKDF2 with 120,000 iterations
- compares derived hashes using constant-time byte comparison
- keeps the database behind a single shared `SQLiteOpenHelper` instance
- catches duplicate-username constraint violations so signup cannot crash on a race
- prevents the app database from being backed up
- performs the deliberately expensive password derivation away from the UI thread
- upgrades the old v1 database by removing the insecure credential schema

The v1-to-v2 migration intentionally discards the old plaintext-credential database. Future schema versions are required to add explicit data-preserving migrations instead of using a generic drop-and-recreate upgrade.

This is still a local demonstration app rather than a production authentication service. A production app would normally use a backend identity provider or platform authentication rather than implementing user credentials entirely on-device.

## YouTube Playback

The app accepts:

- `youtube.com/watch?v=...`
- `youtu.be/...`
- `youtube.com/shorts/...`
- `youtube.com/embed/...`
- a raw 11-character YouTube video ID

The input is reduced to a validated video ID before being injected into the player HTML. Playlist thumbnails are loaded from YouTube's public thumbnail endpoint on background worker threads and cached in memory.

## Testing and CI

Unit tests currently cover the two logic-heavy areas of the app:

- YouTube URL/video-ID parsing
- PBKDF2 salt generation, deterministic derivation, mismatch behaviour and password verification

GitHub Actions runs on pushes and pull requests to `main` and performs:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

This means a change has to pass unit tests and Android lint and produce a debug APK before the CI workflow is green.

## Run Locally

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync complete.
4. Run on an Android emulator or device running Android 7.0 (API 24) or newer.
5. Create a local account, sign in and paste a YouTube link.

Internet access is required for video playback and playlist thumbnails.

## Current Scope

The project intentionally keeps its user and playlist data local to demonstrate native Android fundamentals including activities, SQLite persistence, input validation, background execution, session state, RecyclerView and WebView integration.

Potential future improvements include Room + MVVM, richer video metadata, instrumentation/UI tests and a backend account service.

## Author

**Somil Garak**  
Computer Science — Deakin University  
GitHub: [somilror200](https://github.com/somilror200)
