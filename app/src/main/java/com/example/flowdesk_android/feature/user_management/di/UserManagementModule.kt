package com.example.flowdesk_android.feature.user_management.di

import com.example.flowdesk_android.feature.user_management.data.api.UserApi
import com.example.flowdesk_android.feature.user_management.data.api.RoleApi
import com.example.flowdesk_android.feature.user_management.data.repository.UserRepositoryImpl
import com.example.flowdesk_android.feature.user_management.data.repository.RoleRepositoryImpl
import com.example.flowdesk_android.feature.user_management.domain.repository.UserRepository
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserManagementNetworkModule {

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideRoleApi(retrofit: Retrofit): RoleApi =
        retrofit.create(RoleApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserManagementRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindRoleRepository(impl: RoleRepositoryImpl): RoleRepository
}
