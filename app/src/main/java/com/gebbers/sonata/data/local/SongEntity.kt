package com.gebbers.sonata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val dataPath: String,
    val uri: String,
    val albumId: Long,
    val dateAdded: Long,
    val isFavorite: Boolean = false
)
