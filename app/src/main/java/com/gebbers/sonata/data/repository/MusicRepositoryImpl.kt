package com.gebbers.sonata.data.repository

import com.gebbers.sonata.data.local.PlaylistDao
import com.gebbers.sonata.data.local.PlaylistEntity
import com.gebbers.sonata.data.local.PlaylistSongCrossRef
import com.gebbers.sonata.data.local.SongDao
import com.gebbers.sonata.data.mapper.toEntity
import com.gebbers.sonata.data.mapper.toSong
import com.gebbers.sonata.domain.model.Album
import com.gebbers.sonata.domain.model.Artist
import com.gebbers.sonata.domain.model.Folder
import com.gebbers.sonata.domain.model.Playlist
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
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
            entities.filter { it.albumId == albumId }.map { it.toSong() }
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

    override suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavoriteStatus(songId, isFavorite)
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

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            entities.map { it.toSong() }
        }
    }

    override suspend fun refreshLibrary() = withContext(Dispatchers.IO) {
        val scannnedSongs = musicScanner.scanInternalStorage()
        if (scannnedSongs.isNotEmpty()) {
            songDao.insertSongs(scannnedSongs.map { it.toEntity() })
            songDao.deleteRemovedSongs(scannnedSongs.map { it.mediaStoreId })
        }
    }
}
