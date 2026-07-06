package com.gebbers.sonata.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
abstract class SonataDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        const val DATABASE_NAME = "sonata_db"
    }
}
