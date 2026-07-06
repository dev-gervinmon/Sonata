package com.gebbers.sonata.data.mapper

import com.gebbers.sonata.data.local.SongEntity
import com.gebbers.sonata.domain.model.Song

fun SongEntity.toSong(): Song {
    return Song(
        mediaStoreId = mediaStoreId,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        dataPath = dataPath,
        uri = uri,
        albumId = albumId
    )
}

fun Song.toEntity(dateAdded: Long = System.currentTimeMillis()): SongEntity {
    return SongEntity(
        mediaStoreId = mediaStoreId,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        dataPath = dataPath,
        uri = uri,
        albumId = albumId,
        dateAdded = dateAdded
    )
}
