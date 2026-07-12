package com.gebbers.sonata.di

import android.content.Context
import androidx.room.Room
import com.gebbers.sonata.data.local.ExcludedFolderDao
import com.gebbers.sonata.data.local.PlaylistDao
import com.gebbers.sonata.data.local.ScannedFolderDao
import com.gebbers.sonata.data.local.SongDao
import com.gebbers.sonata.data.local.SonataDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonataDatabase {
        return Room.databaseBuilder(
            context,
            SonataDatabase::class.java,
            SonataDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: SonataDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: SonataDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideExcludedFolderDao(database: SonataDatabase): ExcludedFolderDao {
        return database.excludedFolderDao()
    }

    @Provides
    @Singleton
    fun provideScannedFolderDao(database: SonataDatabase): ScannedFolderDao {
        return database.scannedFolderDao()
    }
}
