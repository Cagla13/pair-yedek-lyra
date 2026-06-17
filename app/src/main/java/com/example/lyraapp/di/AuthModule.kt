package com.example.lyraapp.di

import com.example.lyraapp.data.AuthRepository
import com.example.lyraapp.data.FakeAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt için bağımlılık modülü.
 * @Provides kullanarak bağımlılıkları açıkça tanımlıyoruz;
 * bu yöntem KSP'nin tip tahminleme hatalarını (STAR null) kesin olarak engeller.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(impl: FakeAuthRepository): AuthRepository {
        return impl
    }
}