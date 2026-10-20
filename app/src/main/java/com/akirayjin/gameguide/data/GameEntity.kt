package com.akirayjin.gameguide.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val platform: String = "PS Vita",
    val coverUri: String? = null,
    val summary: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
