package com.example.lyraapp.di

import com.example.lyraapp.data.home.HomeRepository
import com.example.lyraapp.data.home.RemoteHomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideHomeRepository(impl: RemoteHomeRepository): HomeRepository = impl
}
