package com.example.flowdesk_android.feature.system_management.di

import com.example.flowdesk_android.feature.system_management.data.api.SecurityBlockApi
import com.example.flowdesk_android.feature.system_management.data.api.SystemManagementApi
import com.example.flowdesk_android.feature.system_management.data.api.WebsiteApi
import com.example.flowdesk_android.feature.system_management.data.api.BoardApi
import com.example.flowdesk_android.feature.system_management.data.repository.SecurityBlockRepositoryImpl
import com.example.flowdesk_android.feature.system_management.data.repository.SystemManagementRepositoryImpl
import com.example.flowdesk_android.feature.system_management.data.repository.WebsiteRepositoryImpl
import com.example.flowdesk_android.feature.system_management.data.repository.BoardRepositoryImpl
import com.example.flowdesk_android.feature.system_management.domain.repository.SecurityBlockRepository
import com.example.flowdesk_android.feature.system_management.domain.repository.SystemManagementRepository
import com.example.flowdesk_android.feature.system_management.domain.repository.WebsiteRepository
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
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

    @Provides
    @Singleton
    fun provideSecurityBlockApi(retrofit: Retrofit): SecurityBlockApi =
        retrofit.create(SecurityBlockApi::class.java)

    @Provides
    @Singleton
    fun provideWebsiteApi(retrofit: Retrofit): WebsiteApi =
        retrofit.create(WebsiteApi::class.java)

    @Provides
    @Singleton
    fun provideBoardApi(retrofit: Retrofit): BoardApi =
        retrofit.create(BoardApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemManagementRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSystemManagementRepository(
        impl: SystemManagementRepositoryImpl
    ): SystemManagementRepository

    @Binds
    @Singleton
    abstract fun bindSecurityBlockRepository(
        impl: SecurityBlockRepositoryImpl
    ): SecurityBlockRepository

    @Binds
    @Singleton
    abstract fun bindWebsiteRepository(
        impl: WebsiteRepositoryImpl
    ): WebsiteRepository

    @Binds
    @Singleton
    abstract fun bindBoardRepository(
        impl: BoardRepositoryImpl
    ): BoardRepository
}
