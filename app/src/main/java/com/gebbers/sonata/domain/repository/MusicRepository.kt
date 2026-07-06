package com.gebbers.sonata.domain.repository

import com.gebbers.sonata.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun refreshLibrary()
}
