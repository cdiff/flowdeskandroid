package com.example.flowdesk_android.feature.super_admin.di

import com.example.flowdesk_android.feature.super_admin.data.api.SuperApi
import com.example.flowdesk_android.feature.super_admin.data.repository.SuperRepositoryImpl
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SuperNetworkModule {

    @Provides
    @Singleton
    fun provideSuperApi(retrofit: Retrofit): SuperApi =
        retrofit.create(SuperApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SuperRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSuperRepository(impl: SuperRepositoryImpl): SuperRepository
}
