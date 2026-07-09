package com.gebbers.sonata.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.gebbers.sonata.data.local.*
import com.gebbers.sonata.data.mapper.toEntity
import com.gebbers.sonata.data.mapper.toSong
import com.gebbers.sonata.domain.model.Album
import com.gebbers.sonata.domain.model.Artist
import com.gebbers.sonata.domain.model.Folder
import com.gebbers.sonata.domain.model.Playlist
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val excludedFolderDao: ExcludedFolderDao,
    private val scannedFolderDao: ScannedFolderDao,
    private val musicScanner: MusicScanner
) : MusicRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.map { it.toSong() }
        }
    }

    override fun getSongsByFolder(folderPath: String): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.filter { 
                val file = File(it.dataPath)
                file.parent == folderPath
            }.map { it.toSong() }
        }
    }

    override fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.filter { it.artist == artistName }.map { it.toSong() }
        }
    }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.filter { it.albumId == albumId }
                .sortedWith(compareBy({ it.discNumber ?: 0 }, { it.trackNumber ?: 0 }))
                .map { it.toSong() }
        }
    }

    override fun getSongsByGenre(genreName: String): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.filter { it.genre == genreName }.map { it.toSong() }
        }
    }

    override fun getSongsByYear(year: Int): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.filter { it.year == year }.map { it.toSong() }
        }
    }

    override fun getAllFolders(): Flow<List<Folder>> {
        return songDao.getAllSongs().map { entities ->
            entities.groupBy { 
                val file = File(it.dataPath)
                file.parent ?: "Unknown"
            }.map { (path, songs) ->
                Folder(
                    name = path.substringAfterLast('/'),
                    path = path,
                    songCount = songs.size
                )
            }.sortedBy { it.name }
        }
    }

    override fun getAllArtists(): Flow<List<Artist>> {
        return songDao.getAllSongs().map { entities ->
            entities.groupBy { it.artist }.map { (name, songs) ->
                Artist(
                    name = name,
                    albumCount = songs.distinctBy { it.albumId }.size,
                    songCount = songs.size
                )
            }.sortedBy { it.name }
        }
    }

    override fun getAllAlbums(): Flow<List<Album>> {
        return songDao.getAllSongs().map { entities ->
            entities.groupBy { it.albumId }.map { (id, songs) ->
                val firstSong = songs.first()
                Album(
                    id = id,
                    name = firstSong.album,
                    artist = firstSong.artist,
                    albumArtUri = firstSong.toSong().albumArtUri,
                    songCount = songs.size
                )
            }.sortedBy { it.name }
        }
    }

    override fun getAllGenres(): Flow<List<String>> {
        return songDao.getAllSongs().map { entities ->
            entities.mapNotNull { it.genre }.distinct().sorted()
        }
    }

    override fun getAllYears(): Flow<List<Int>> {
        return songDao.getAllSongs().map { entities ->
            entities.mapNotNull { it.year }.distinct().sortedDescending()
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(id = entity.id, name = entity.name)
            }
        }
    }

    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId).map { entities ->
            entities.map { it.toSong() }
        }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { entities ->
            entities.map { it.toSong() }
        }
    }

    override fun getRecentlyAdded(): Flow<List<Song>> {
        return songDao.getRecentlyAdded().map { entities ->
            entities.map { it.toSong() }
        }
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return songDao.getRecentlyPlayed().map { entities ->
            entities.map { it.toSong() }
        }
    }

    override fun getMostPlayed(): Flow<List<Song>> {
        return songDao.getMostPlayed().map { entities ->
            entities.map { it.toSong() }
        }
    }

    override suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavoriteStatus(songId, isFavorite)
    }

    override suspend fun recordSongPlayback(songId: Long) {
        songDao.incrementPlayCount(songId, System.currentTimeMillis())
    }

    override suspend fun createPlaylist(name: String) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(PlaylistEntity(id = playlist.id, name = playlist.name))
    }

    override suspend fun addSongToPlaylist(playlistId: Long, mediaStoreId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, mediaStoreId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, mediaStoreId: Long) {
        playlistDao.removeSongFromPlaylist(PlaylistSongCrossRef(playlistId, mediaStoreId))
    }

    override suspend fun updateSongTags(
        songId: Long,
        newTitle: String,
        newArtist: String,
        newAlbum: String,
        newTrackNumber: Int?,
        newDiscNumber: Int?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA)
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            
            var extension = "mp3"
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val path = cursor.getString(0)
                    extension = path.substringAfterLast('.', "mp3")
                }
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, newTitle)
                put(MediaStore.Audio.Media.ARTIST, newArtist)
                put(MediaStore.Audio.Media.ALBUM, newAlbum)
                put(MediaStore.Audio.Media.TRACK, (newDiscNumber ?: 0) * 1000 + (newTrackNumber ?: 0))
                // Note: disc_number might not be available for update on all versions

                // Also rename the physical file on disk
                put(MediaStore.Audio.Media.DISPLAY_NAME, "$newArtist - $newTitle.$extension")
            }

            val updated = context.contentResolver.update(uri, contentValues, null, null)
            
            if (updated > 0) {
                refreshLibrary()
                return@withContext true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getLyrics(song: Song): String? = withContext(Dispatchers.IO) {
        if (song.lyrics != null) return@withContext song.lyrics
        
        val lyrics = musicScanner.getLyrics(song.dataPath)
        if (lyrics != null) {
            songDao.updateLyrics(song.mediaStoreId, lyrics)
        }
        lyrics
    }

    override fun getExcludedFolders(): Flow<List<String>> {
        return excludedFolderDao.getAllExcludedFolders().map { entities ->
            entities.map { it.path }
        }
    }

    override suspend fun excludeFolder(path: String) {
        excludedFolderDao.insertExcludedFolder(ExcludedFolderEntity(path))
        refreshLibrary()
    }

    override suspend fun includeFolder(path: String) {
        excludedFolderDao.deleteExcludedFolder(ExcludedFolderEntity(path))
        refreshLibrary()
    }

    override fun getScannedFolders(): Flow<List<ScannedFolderEntity>> {
        return scannedFolderDao.getAllScannedFolders()
    }

    override suspend fun addScannedFolder(path: String, name: String) {
        scannedFolderDao.insertScannedFolder(ScannedFolderEntity(path, name))
        refreshLibrary()
    }

    override suspend fun removeScannedFolder(path: String) {
        scannedFolderDao.deleteScannedFolder(ScannedFolderEntity(path, ""))
        refreshLibrary()
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            entities.map { it.toSong() }
        }
    }

    override suspend fun refreshLibrary() = withContext(Dispatchers.IO) {
        val excludedFolders = excludedFolderDao.getAllExcludedFolders().first().map { it.path }
        val scannedFolders = scannedFolderDao.getAllScannedFolders().first().map { it.path }
        val scannedSongs = musicScanner.scanInternalStorage(excludedFolders, scannedFolders)
        if (scannedSongs.isNotEmpty()) {
            songDao.insertSongs(scannedSongs.map { it.toEntity() })
            songDao.deleteRemovedSongs(scannedSongs.map { it.mediaStoreId })
        }
    }
}
