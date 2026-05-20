package com.example.flowdesk_android.feature.role.di

import com.example.flowdesk_android.feature.role.data.api.RoleApi
import com.example.flowdesk_android.feature.role.data.repository.RoleRepositoryImpl
import com.example.flowdesk_android.feature.role.domain.repository.RoleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoleNetworkModule {

    @Provides
    @Singleton
    fun provideRoleApi(retrofit: Retrofit): RoleApi =
        retrofit.create(RoleApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RoleRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRoleRepository(impl: RoleRepositoryImpl): RoleRepository
}
