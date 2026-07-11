# Sonata 🎵

Sonata is a full-featured, modern Android music player built with **Jetpack Compose** and **Media3**. It is designed for playing local audio files with a focus on performance, clean architecture, and a premium user experience.

## 🚀 Current Progress: SDLC Phase - Initial Development
The core playback engine and data infrastructure are complete. The app is currently in the feature-expansion phase.

## ✨ Features

### 🎧 Core Playback
- **Media3 ExoPlayer Integration**: High-performance audio engine.
- **Background Playback**: Full support for background service with system notification controls.
- **Fast Forward & Rewind**: Dedicated 10s skip buttons in the player.
- **Queue Management**: Dynamic playlist handling with Gapless playback.
- **Seek & Progress**: Real-time seek bar with millisecond-perfect timestamps.
- **Shuffle & Repeat**: Full support for Shuffle mode and Repeat (Off, One, All).
- **Gapless Playback**: Professional, configurable crossfade transitions for an uninterrupted listening experience.
- **Smooth Transitions**: Automated, user-definable fade-in/fade-out between tracks.

### 📂 Library Management
- **MediaStore Scanner**: Automatically indexes all local music files.
- **Room Database**: Persistent storage for fast library access.
- **Instant Search**: Reactive search across titles, artists, and albums.
- **Advanced Search**: Global search support for Genres, Folders, and even Lyrics content.
- **Fuzzy Search**: Powered by SQLite FTS4 for fast, typo-tolerant discovery.
- **Artists & Albums Views**: Dedicated groupings for better structured library browsing.
- **Genre & Year Views**: Explore your collection by musical style or release era.
- **Folder-based Browsing**: Navigate your music by its actual directory structure.
- **Custom Library Locations**: Choose specific folders to scan and organize your music exactly where you want it.
- **Folder Exclusion**: Long-press any folder to hide it from your library (e.g., ringtones, voice notes).
- **Custom Playlists**: Create and manage your own song collections.
- **Smart Playlists**: Automatically generated lists for "Recently Added," "Most Played," and "Recently Played."
- **Favorites System**: Quick "Heart" toggle to save and access your top tracks instantly.
- **Tag Editor**: Edit song metadata (Title, Artist, Album, Track, Disc) and automatically rename files on disk to match.
- **Custom Album Art**: Choose any image from your gallery or search the internet (via iTunes API) to replace missing or low-quality album covers.

### 🎛️ Audio Effects
- **5-Band Equalizer**: Direct hardware-level integration with band sliders and presets.
- **Bass Boost & Virtualizer**: Dedicated controls to enhance low-end and spatial depth.
- **Volume Normalization**: Built-in loudness enhancement to balance track levels.
- **Audio Visualizer**: Real-time FFT spectrum analysis integrated into the audio effects screen.

### 🛠️ Advanced Tools
- **Sleep Timer**: Customizable countdown to automatically stop playback.
- **Playback Speed & Pitch**: Adjust playback speed (0.5x - 2.0x) and pitch independently.
- **Mini Player**: Persistent control bar for quick access while browsing the library.
- **Full Player Screen**: Immersive Material3 bottom sheet with rich controls.
- **Lyrics Support**: Integrated display for both embedded and external synced (.lrc) lyrics.
- **Online Lyrics**: Automatically fetches missing lyrics from the internet if not found in your local files.
- **Dynamic Theming**: Automatically updates the app's accent colors based on the currently playing album art.
- **Android Auto Support**: Full integration with the Android Auto media ecosystem for a safe and professional driving experience.
- **Settings Page**: Customizable experience including theme toggles and library management.
- **Homescreen Widget**: Control playback and see current song info directly from your homescreen.

## 🏗️ Tech Stack
- **UI**: Jetpack Compose (Material 3)
- **Engine**: Jetpack Media3 (ExoPlayer + Session + Library)
- **Database**: Room Persistence Library
- **DI**: Hilt (Dagger)
- **Concurrency**: Kotlin Coroutines & Flow
- **Image Loading**: Coil 3
- **Color Extraction**: Android Palette API
- **Build System**: Kotlin DSL (build.gradle.kts) + Version Catalog (libs.versions.toml)

## 📸 Screenshots
*(Coming soon as the UI evolves)*

## 🛣️ Roadmap
- [x] Album Art extraction and caching.
- [x] Folder-based browsing.
- [x] Custom Playlist creation.
- [x] Smart Playlists.
- [x] Favorites System.
- [x] Artist & Album Views.
- [x] Genre & Year Views.
- [x] Folder Exclusion logic.
- [x] Homescreen Widgets.
- [x] Tag Editor.
- [x] Custom Library Locations.
- [x] Dynamic UI Polish.
- [x] Android Auto support.
- [x] Gapless Playback fine-tuning.
- [x] Lyrics extraction and display.

---
*Developed as a high-performance local music solution for Android.*
