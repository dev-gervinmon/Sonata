package com.gebbers.sonata.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        SongFtsEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        ExcludedFolderEntity::class,
        ScannedFolderEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class SonataDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun excludedFolderDao(): ExcludedFolderDao
    abstract fun scannedFolderDao(): ScannedFolderDao

    companion object {
        const val DATABASE_NAME = "sonata_db"
    }
}
