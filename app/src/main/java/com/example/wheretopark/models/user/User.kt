package com.example.wheretopark.models.user

import com.example.wheretopark.data.local.entity.Favorite

data class User(
    val username: String,
    val email: String,
    val password: String,
    val imgPath: String = "",
    val coins: Int = 0,
    val favorites: List<Favorite> = emptyList()
)
