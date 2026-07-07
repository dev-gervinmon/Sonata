package com.gebbers.sonata.data.mapper

import android.content.ContentUris
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.gebbers.sonata.data.local.SongEntity
import com.gebbers.sonata.domain.model.Song

fun SongEntity.toSong(): Song {
    val albumArtUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId
    ).toString()

    return Song(
        mediaStoreId = mediaStoreId,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        dataPath = dataPath,
        uri = uri,
        albumId = albumId,
        albumArtUri = albumArtUri,
        isFavorite = isFavorite
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
        dateAdded = dateAdded,
        isFavorite = isFavorite
    )
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(mediaStoreId.toString())
        .setUri(Uri.parse(uri))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(Uri.parse(albumArtUri))
                .build()
        )
        .build()
}
