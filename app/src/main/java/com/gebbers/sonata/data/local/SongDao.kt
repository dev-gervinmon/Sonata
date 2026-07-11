package com.gebbers.sonata.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%' 
        OR genre LIKE '%' || :query || '%' 
        OR dataPath LIKE '%' || :query || '%' 
        OR lyrics LIKE '%' || :query || '%'
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE mediaStoreId = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE mediaStoreId = :id")
    suspend fun incrementPlayCount(id: Long, timestamp: Long)

    @Query("UPDATE songs SET lyrics = :lyrics WHERE mediaStoreId = :id")
    suspend fun updateLyrics(id: Long, lyrics: String?)

    @Query("UPDATE songs SET customAlbumArtUri = :artworkUri WHERE mediaStoreId = :id")
    suspend fun updateCustomArtwork(id: Long, artworkUri: String?)

    @Query("UPDATE songs SET genre = :genre WHERE mediaStoreId = :id")
    suspend fun updateGenre(id: Long, genre: String?)

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 50")
    fun getRecentlyAdded(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT 50")
    fun getRecentlyPlayed(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayed(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE mediaStoreId NOT IN (:ids)")
    suspend fun deleteRemovedSongs(ids: List<Long>)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()
}
