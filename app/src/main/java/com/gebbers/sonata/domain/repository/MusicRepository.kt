package com.gebbers.sonata.domain.repository

import com.gebbers.sonata.domain.model.Folder
import com.gebbers.sonata.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    fun getAllFolders(): Flow<List<Folder>>
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun refreshLibrary()
}
