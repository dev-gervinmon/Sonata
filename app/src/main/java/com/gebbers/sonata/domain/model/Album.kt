package com.gebbers.sonata.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: String,
    val songCount: Int
)
