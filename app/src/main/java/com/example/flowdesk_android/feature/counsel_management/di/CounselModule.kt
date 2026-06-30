package com.example.flowdesk_android.feature.counsel_management.di

import com.example.flowdesk_android.feature.counsel_management.data.api.CounselApi
import com.example.flowdesk_android.feature.counsel_management.data.repository.CounselRepositoryImpl
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CounselNetworkModule {

    @Provides
    @Singleton
    fun provideCounselApi(retrofit: Retrofit): CounselApi =
        retrofit.create(CounselApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CounselRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCounselRepository(impl: CounselRepositoryImpl): CounselRepository
}
