package com.example.flowdesk_android.di

import com.example.flowdesk_android.core.network.AuthInterceptor
import com.example.flowdesk_android.core.network.TokenAuthenticator
import com.example.flowdesk_android.feature.auth.data.api.AuthRefreshApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient

/**
 * 공통 네트워크 모듈 — Retrofit/OkHttp 인스턴스만 제공
 * 각 feature별 API 인터페이스는 각 feature의 di/ 모듈에서 등록
 *
 * feature/auth/di/AuthModule.kt     → AuthApi
 * feature/user/di/UserModule.kt     → UserApi
 * feature/role/di/RoleModule.kt     → RoleApi
 * feature/super_admin/di/SuperModule.kt → SuperApi
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://flowdesk-admin-production.up.railway.app/"

    /**
     * Refresh 전용 OkHttpClient — Authenticator 미포함 (순환 방지)
     */
    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
     * Refresh 전용 Retrofit → AuthRefreshApi 생성에 사용
     */
    @Provides
    @Singleton
    fun provideAuthRefreshApi(@RefreshClient client: OkHttpClient): AuthRefreshApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthRefreshApi::class.java)
    }

    /**
     * 메인 OkHttpClient — AuthInterceptor(헤더 삽입) + TokenAuthenticator(401 자동 갱신)
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

