package com.example.flowdesk_android.feature.system_management.di

import com.example.flowdesk_android.feature.system_management.data.repository.SystemManagementRepositoryImpl
import com.example.flowdesk_android.feature.system_management.data.api.SystemManagementApi
import com.example.flowdesk_android.feature.system_management.domain.repository.SystemManagementRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemManagementNetworkModule {

    @Provides
    @Singleton
    fun provideSystemManagementApi(retrofit: Retrofit): SystemManagementApi =
        retrofit.create(SystemManagementApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemManagementRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSystemManagementRepository(
        impl: SystemManagementRepositoryImpl
    ): SystemManagementRepository
}
