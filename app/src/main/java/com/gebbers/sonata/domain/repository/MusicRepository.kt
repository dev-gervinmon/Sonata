package com.gebbers.sonata.domain.repository

import com.gebbers.sonata.domain.model.Folder
import com.gebbers.sonata.domain.model.Playlist
import com.gebbers.sonata.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    fun getAllFolders(): Flow<List<Folder>>
    
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String)
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Long, mediaStoreId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, mediaStoreId: Long)

    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun refreshLibrary()
}
