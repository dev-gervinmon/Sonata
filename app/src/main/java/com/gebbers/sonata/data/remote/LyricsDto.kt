package com.gebbers.sonata.data.remote

import com.google.gson.annotations.SerializedName

data class LyricsResponse(
    @SerializedName("lyrics") val lyrics: String?
)
