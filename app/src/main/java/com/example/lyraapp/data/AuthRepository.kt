package com.example.lyraapp.data

import com.example.lyraapp.data.auth.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<UserProfile?>

    suspend fun login(phoneNumber: String, password: String): Result<Unit>

    suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit>
}
