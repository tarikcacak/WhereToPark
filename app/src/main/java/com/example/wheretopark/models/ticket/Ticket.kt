package com.example.wheretopark.models.ticket

data class Ticket(
    val location: String,
    val expiring: String,
    val user: String
)