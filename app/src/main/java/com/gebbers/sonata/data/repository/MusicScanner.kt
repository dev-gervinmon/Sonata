package com.gebbers.sonata.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import com.gebbers.sonata.domain.model.Song
import java.io.File
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicScanner @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun scanInternalStorage(
        excludedFolders: List<String> = emptyList(),
        scannedFolders: List<String> = emptyList()
    ): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.DISC_NUMBER)
        }

        val projection = projectionList.toTypedArray()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val discCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
                } else -1

                while (cursor.moveToNext()) {
                    val data = if (dataCol != -1) cursor.getString(dataCol) else null
                    if (data == null) continue
                    
                    // Folder Exclusion Check
                    if (excludedFolders.any { data.startsWith(it) }) continue

                    // Scanned Folders (Whitelist) Check
                    if (scannedFolders.isNotEmpty() && !scannedFolders.any { data.startsWith(it) }) continue

                    val id = if (idCol != -1) cursor.getLong(idCol) else 0L
                    val title = if (titleCol != -1) cursor.getString(titleCol) ?: "Unknown" else "Unknown"
                    val artist = if (artistCol != -1) cursor.getString(artistCol) ?: "Unknown" else "Unknown"
                    val album = if (albumCol != -1) cursor.getString(albumCol) ?: "Unknown" else "Unknown"
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val albumId = if (albumIdCol != -1) cursor.getLong(albumIdCol) else 0L
                    val year = if (yearCol != -1) cursor.getInt(yearCol) else 0
                    val trackRaw = if (trackCol != -1) cursor.getInt(trackCol) else 0
                    val discRaw = if (discCol != -1) cursor.getInt(discCol) else 0

                    val trackNumber = if (trackRaw >= 1000) trackRaw % 1000 else trackRaw
                    val discNumber = if (discRaw > 0) discRaw else if (trackRaw >= 1000) trackRaw / 1000 else 0
                    
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    songs.add(
                        Song(
                            mediaStoreId = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            dataPath = data,
                            uri = contentUri,
                            albumId = albumId,
                            albumArtUri = "", // Filled by mapper
                            lyrics = null,
                            genre = null,
                            year = if (year > 0) year else null,
                            trackNumber = if (trackNumber > 0) trackNumber else null,
                            discNumber = if (discNumber > 0) discNumber else null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs
    }

    fun getLyrics(path: String): String? {
        return extractLyrics(path)
    }

    private fun extractLyrics(path: String): String? {
        val synced = findSyncedLyrics(path)
        if (synced != null) return synced
        
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.extractMetadata(36)
        } catch (_: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    private fun findSyncedLyrics(songPath: String): String? {
        val lrcPath = songPath.substringBeforeLast('.') + ".lrc"
        val lrcFile = File(lrcPath)
        return if (lrcFile.exists()) {
            try {
                lrcFile.readText()
            } catch (_: Exception) {
                null
            }
        } else null
    }
}
