package com.gebbers.sonata.data.local

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = SongEntity::class)
@Entity(tableName = "songs_fts")
data class SongFtsEntity(
    val title: String,
    val artist: String,
    val album: String,
    val lyrics: String?,
    val genre: String?,
    val dataPath: String
)
