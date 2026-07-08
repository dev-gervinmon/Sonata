package com.gebbers.sonata.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcludedFolderDao {
    @Query("SELECT * FROM excluded_folders")
    fun getAllExcludedFolders(): Flow<List<ExcludedFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcludedFolder(folder: ExcludedFolderEntity)

    @Delete
    suspend fun deleteExcludedFolder(folder: ExcludedFolderEntity)
}
