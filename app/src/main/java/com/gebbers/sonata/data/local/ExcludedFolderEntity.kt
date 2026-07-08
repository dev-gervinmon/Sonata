package com.gebbers.sonata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "excluded_folders")
data class ExcludedFolderEntity(
    @PrimaryKey val path: String
)
