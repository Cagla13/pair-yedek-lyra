package com.example.lyraapp.di

import com.example.lyraapp.data.membership.MembershipRepository
import com.example.lyraapp.data.membership.RemoteMembershipRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MembershipModule {

    @Provides
    @Singleton
    fun provideMembershipRepository(impl: RemoteMembershipRepository): MembershipRepository = impl
}
