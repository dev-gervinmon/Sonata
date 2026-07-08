package com.gebbers.sonata.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedFolderDao {
    @Query("SELECT * FROM scanned_folders")
    fun getAllScannedFolders(): Flow<List<ScannedFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedFolder(folder: ScannedFolderEntity)

    @Delete
    suspend fun deleteScannedFolder(folder: ScannedFolderEntity)
}
