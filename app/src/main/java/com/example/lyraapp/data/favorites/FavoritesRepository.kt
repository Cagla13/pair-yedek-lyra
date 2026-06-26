package com.example.lyraapp.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.lyraapp.data.home.formatDurationMs
import com.example.lyraapp.ui.favorites.SongUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class StoredFavorite(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long = 0L,
)

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.favoritesDataStore
    private val json = Json { ignoreUnknownKeys = true }

    val favorites: Flow<List<StoredFavorite>> = dataStore.data.map { preferences ->
        decode(preferences[FAVORITES_JSON_KEY].orEmpty())
    }

    suspend fun getFavorites(): List<StoredFavorite> = favorites.first()

    suspend fun isFavorite(songId: String): Boolean =
        getFavorites().any { it.id == songId }

    suspend fun add(favorite: StoredFavorite) {
        val current = getFavorites()
        if (current.any { it.id == favorite.id }) return
        persist(current + favorite)
    }

    suspend fun remove(songId: String) {
        persist(getFavorites().filterNot { it.id == songId })
    }

    suspend fun toggle(favorite: StoredFavorite): Boolean {
        val current = getFavorites()
        return if (current.any { it.id == favorite.id }) {
            persist(current.filterNot { it.id == favorite.id })
            false
        } else {
            persist(current + favorite)
            true
        }
    }

    private suspend fun persist(items: List<StoredFavorite>) {
        dataStore.edit { preferences ->
            preferences[FAVORITES_JSON_KEY] = json.encodeToString(items)
        }
    }

    private fun decode(raw: String): List<StoredFavorite> {
        if (raw.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<StoredFavorite>>(raw) }
            .getOrDefault(emptyList())
    }

    companion object {
        private val FAVORITES_JSON_KEY = stringPreferencesKey("favorites_json")

        fun StoredFavorite.toUiModel(isPlaying: Boolean = false): SongUiModel = SongUiModel(
            id = id,
            title = title,
            artist = artist,
            duration = formatDurationMs(durationMs),
            isPlaying = isPlaying,
        )
    }
}
