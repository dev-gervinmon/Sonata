package com.gebbers.sonata.domain.model

data class Song(
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val dataPath: String,
    val uri: String,
    val albumId: Long,
    val albumArtUri: String,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedAt: Long = 0,
    val lyrics: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null
)
