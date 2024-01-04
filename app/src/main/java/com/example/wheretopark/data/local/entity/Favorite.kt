package com.example.wheretopark.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    val name: String,
    val latitude: String,
    val longitude: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)