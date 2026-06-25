package com.example.lyraapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistRequest(
    val name: String,
    val description: String
)