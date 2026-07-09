package com.gebbers.sonata.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.gebbers.sonata.domain.model.Song
import java.io.File
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanInternalStorage(
        excludedFolders: List<String> = emptyList(),
        scannedFolders: List<String> = emptyList()
    ): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            "disc_number" // MediaStore.Audio.Media.DISC_NUMBER
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
            val discColumn = cursor.getColumnIndex("disc_number")

            while (cursor.moveToNext()) {
                val data = if (dataColumn != -1) cursor.getString(dataColumn) else null
                if (data == null) continue
                
                // Folder Exclusion Check
                if (excludedFolders.any { data.startsWith(it) }) continue

                // Scanned Folders (Whitelist) Check
                if (scannedFolders.isNotEmpty() && !scannedFolders.any { data.startsWith(it) }) continue

                val id = if (idColumn != -1) cursor.getLong(idColumn) else 0L
                val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "Unknown" else "Unknown"
                val artist = if (artistColumn != -1) cursor.getString(artistColumn) ?: "Unknown" else "Unknown"
                val album = if (albumColumn != -1) cursor.getString(albumColumn) ?: "Unknown" else "Unknown"
                val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                val albumId = if (albumIdColumn != -1) cursor.getLong(albumIdColumn) else 0L
                val year = if (yearColumn != -1) cursor.getInt(yearColumn) else 0
                val trackRaw = if (trackColumn != -1) cursor.getInt(trackColumn) else 0
                val discRaw = if (discColumn != -1) cursor.getInt(discColumn) else 0

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
                        lyrics = null, // Extracted on demand
                        genre = null, // Extracted on demand
                        year = if (year > 0) year else null,
                        trackNumber = if (trackNumber > 0) trackNumber else null,
                        discNumber = if (discNumber > 0) discNumber else null
                    )
                )
            }
        }
        return songs
    }

    fun getLyrics(path: String): String? {
        return extractLyrics(path)
    }

    fun getGenre(path: String): String? {
        return extractGenre(path)
    }

    private fun extractGenre(path: String): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    private fun extractLyrics(path: String): String? {
        val synced = findSyncedLyrics(path)
        if (synced != null) return synced
        
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.extractMetadata(36)
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    private fun findSyncedLyrics(songPath: String): String? {
        val lrcPath = songPath.substringBeforeLast('.') + ".lrc"
        val lrcFile = File(lrcPath)
        return if (lrcFile.exists()) {
            try {
                lrcFile.readText()
            } catch (e: Exception) {
                null
            }
        } else null
    }
}
