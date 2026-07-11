package com.gebbers.sonata.data.remote

import com.google.gson.annotations.SerializedName

data class ITunesSearchResponse(
    @SerializedName("results") val results: List<ITunesResult>,
)

data class ITunesResult(
    @SerializedName("artworkUrl100") val artworkUrl100: String,
    @SerializedName("collectionName") val albumName: String,
    @SerializedName("artistName") val artistName: String
) {
    val artworkUrlHighRes: String
        get() = artworkUrl100.replace("100x100bb.jpg", "600x600bb.jpg")
}
