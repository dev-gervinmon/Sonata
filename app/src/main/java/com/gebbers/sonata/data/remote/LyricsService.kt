package com.gebbers.sonata.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface LyricsService {
    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String
    ): LyricsResponse
}
