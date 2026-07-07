package com.gebbers.sonata.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)
