package com.example.lyraapp.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.example.lyraapp.data.player.MediaPlayerRepository
import com.example.lyraapp.data.player.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // Ses odağını otomatik yönet
            )
            .setHandleAudioBecomingNoisy(true) // Kulaklık çekilince duraklat
            .build()
    }

    @Provides
    @Singleton
    fun providePlayerRepository(impl: MediaPlayerRepository): PlayerRepository = impl
}