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
    val albumArtUri: String
)
