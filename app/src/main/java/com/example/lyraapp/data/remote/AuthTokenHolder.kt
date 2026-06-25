package com.example.lyraapp.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenHolder @Inject constructor() {
    @Volatile
    var accessToken: String? = null
}
