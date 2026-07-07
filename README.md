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

### 📂 Library Management
- **MediaStore Scanner**: Automatically indexes all local music files.
- **Room Database**: Persistent storage for fast library access.
- **Instant Search**: Reactive search across titles, artists, and albums using Kotlin Flows.

### 🎛️ Audio Effects
- **5-Band Equalizer**: Direct hardware-level integration with band sliders.
- **Presets**: Support for system audio presets (Rock, Pop, Jazz, etc.).
- **Volume Normalization**: Built-in loudness enhancement to balance track levels.

### 🛠️ Advanced Tools
- **Sleep Timer**: Customizable countdown to automatically stop playback.
- **Playback Speed & Pitch**: Adjust playback speed (0.5x - 2.0x) and pitch independently for a customized listening experience.
- **Mini Player**: Persistent control bar for quick access while browsing the library.
- **Full Player Screen**: Immersive Material3 bottom sheet with rich controls.

## 🏗️ Tech Stack
- **UI**: Jetpack Compose (Material 3)
- **Engine**: Jetpack Media3 (ExoPlayer + Session)
- **Database**: Room Persistence Library
- **DI**: Hilt (Dagger)
- **Concurrency**: Kotlin Coroutines & Flow
- **Image Loading**: Coil 3
- **Build System**: Kotlin DSL (build.gradle.kts) + Version Catalog (libs.versions.toml)

## 📸 Screenshots
*(Coming soon as the UI evolves)*

## 🛣️ Roadmap
- [ ] Album Art extraction and caching.
- [ ] Folder-based browsing.
- [ ] Custom Playlist creation.
- [ ] Homescreen Widgets.
- [ ] Android Auto support.

---
*Developed as a high-performance local music solution for Android.*
