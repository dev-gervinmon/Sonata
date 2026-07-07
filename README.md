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
- **Smooth Transitions**: Automated fade-in/fade-out between tracks.

### 📂 Library Management
- **MediaStore Scanner**: Automatically indexes all local music files.
- **Room Database**: Persistent storage for fast library access.
- **Instant Search**: Reactive search across titles, artists, and albums.
- **Artists & Albums Views**: Dedicated groupings for better structured library browsing.
- **Folder-based Browsing**: Navigate your music by its actual directory structure.
- **Custom Playlists**: Create and manage your own song collections.
- **Smart Playlists**: Automatically generated lists for "Recently Added," "Most Played," and "Recently Played."
- **Favorites System**: Quick "Heart" toggle to save and access your top tracks instantly.
- **Tag Editor**: Edit song metadata (Title, Artist, Album) directly within the app.

### 🎛️ Audio Effects
- **5-Band Equalizer**: Direct hardware-level integration with band sliders.
- **Presets**: Support for system audio presets (Rock, Pop, Jazz, etc.).
- **Volume Normalization**: Built-in loudness enhancement to balance track levels.

### 🛠️ Advanced Tools
- **Sleep Timer**: Customizable countdown to automatically stop playback.
- **Playback Speed & Pitch**: Adjust playback speed (0.5x - 2.0x) and pitch independently.
- **Mini Player**: Persistent control bar for quick access while browsing the library.
- **Full Player Screen**: Immersive Material3 bottom sheet with rich controls.
- **Dynamic Theming**: Automatically updates the app's accent colors based on the currently playing album art.
- **Settings Page**: Customizable experience including theme toggles and library management.
- **Homescreen Widget**: Control playback and see current song info directly from your homescreen.

## 🏗️ Tech Stack
- **UI**: Jetpack Compose (Material 3)
- **Engine**: Jetpack Media3 (ExoPlayer + Session)
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
- [x] Homescreen Widgets.
- [x] Tag Editor.
- [x] Dynamic UI Polish.
- [x] Settings Page.
- [ ] Android Auto support.
- [ ] Lyrics extraction and display.

---
*Developed as a high-performance local music solution for Android.*
