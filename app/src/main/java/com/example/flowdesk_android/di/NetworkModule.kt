package com.example.flowdesk_android.di

import com.example.flowdesk_android.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

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

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = tokenManager.getToken()
                if (token != null) {
                    val request = original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                } else {
                    chain.proceed(original)
                }
            }
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
