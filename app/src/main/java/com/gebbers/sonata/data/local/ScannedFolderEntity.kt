package com.gebbers.sonata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_folders")
data class ScannedFolderEntity(
    @PrimaryKey val path: String,
    val displayName: String
)
