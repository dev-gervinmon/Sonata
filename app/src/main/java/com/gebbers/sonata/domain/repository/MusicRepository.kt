package com.gebbers.sonata.domain.repository

import com.gebbers.sonata.domain.model.Album
import com.gebbers.sonata.domain.model.Artist
import com.gebbers.sonata.domain.model.Folder
import com.gebbers.sonata.domain.model.Playlist
import com.gebbers.sonata.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByGenre(genreName: String): Flow<List<Song>>
    fun getSongsByYear(year: Int): Flow<List<Song>>
    
    fun getAllFolders(): Flow<List<Folder>>
    fun getAllArtists(): Flow<List<Artist>>
    fun getAllAlbums(): Flow<List<Album>>
    fun getAllGenres(): Flow<List<String>>
    fun getAllYears(): Flow<List<Int>>
    
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getRecentlyAdded(): Flow<List<Song>>
    fun getRecentlyPlayed(): Flow<List<Song>>
    fun getMostPlayed(): Flow<List<Song>>

    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean)
    suspend fun recordSongPlayback(songId: Long)
    suspend fun createPlaylist(name: String)
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Long, mediaStoreId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, mediaStoreId: Long)

    suspend fun updateSongTags(songId: Long, newTitle: String, newArtist: String, newAlbum: String): Boolean

    fun getExcludedFolders(): Flow<List<String>>
    suspend fun excludeFolder(path: String)
    suspend fun includeFolder(path: String)

    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun refreshLibrary()
}
