package com.example.wheretopark.models.user

data class User(
    val username: String,
    val email: String,
    val password: String,
    val imgPath: String = ""
)
