package com.gebbers.sonata.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesSearchService {
    @GET("search")
    suspend fun searchAlbum(
        @Query("term") term: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 10
    ): ITunesSearchResponse
}
