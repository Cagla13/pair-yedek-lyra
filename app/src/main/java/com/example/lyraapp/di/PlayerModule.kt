package com.example.lyraapp.di

import com.example.lyraapp.data.player.FakePlayerRepository
import com.example.lyraapp.data.player.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providePlayerRepository(impl: FakePlayerRepository): PlayerRepository = impl
}
