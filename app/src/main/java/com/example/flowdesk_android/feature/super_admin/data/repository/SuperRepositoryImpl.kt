package com.example.flowdesk_android.feature.super_admin.data.repository

import com.example.flowdesk_android.feature.super_admin.data.api.SuperApi
import com.example.flowdesk_android.feature.super_admin.domain.model.DashboardStats
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuperRepositoryImpl @Inject constructor(
    private val api: SuperApi
) : SuperRepository {

    override suspend fun getDashboard(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getDashboard()
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("응답 바디 없음")
            } else {
                throw Exception("대시보드 조회 실패 (${response.code()})")
            }
        }
    }
}
