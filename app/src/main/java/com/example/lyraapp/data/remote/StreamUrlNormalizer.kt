package com.example.lyraapp.data.remote

import com.example.lyraapp.di.NetworkModule

object StreamUrlNormalizer {

    private val apiOrigin = NetworkModule.BASE_URL.trimEnd('/')

    fun normalize(url: String): String {
        if (url.startsWith("http://localhost", ignoreCase = true) ||
            url.startsWith("https://localhost", ignoreCase = true) ||
            url.contains("127.0.0.1")
        ) {
            return url
                .replace(Regex("https?://localhost:\\d+"), apiOrigin)
                .replace(Regex("https?://127\\.0\\.0\\.1:\\d+"), apiOrigin)
        }
        return url
    }
}
