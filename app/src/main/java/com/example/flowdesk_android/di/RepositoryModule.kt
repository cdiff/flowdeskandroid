package com.example.flowdesk_android.di

import com.example.flowdesk_android.data.repository.AuthRepositoryImpl
import com.example.flowdesk_android.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: com.example.flowdesk_android.data.repository.UserRepositoryImpl
    ): com.example.flowdesk_android.domain.repository.UserRepository

    @Binds
    @Singleton
    abstract fun bindRoleRepository(
        roleRepositoryImpl: com.example.flowdesk_android.data.repository.RoleRepositoryImpl
    ): com.example.flowdesk_android.domain.repository.RoleRepository
}
