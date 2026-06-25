package com.example.lyraapp.di

import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.RemoteAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(impl: RemoteAuthRepository): AuthRepository = impl
}
