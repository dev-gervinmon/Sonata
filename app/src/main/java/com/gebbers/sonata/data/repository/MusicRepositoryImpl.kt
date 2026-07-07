package com.gebbers.sonata.data.repository

import com.gebbers.sonata.data.local.SongDao
import com.gebbers.sonata.data.mapper.toEntity
import com.gebbers.sonata.data.mapper.toSong
import com.gebbers.sonata.domain.model.Folder
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
