package com.example.flowdesk_android.data.repository

import com.example.flowdesk_android.data.remote.SuperApi
import com.example.flowdesk_android.data.remote.dto.SuperDashboardResponse
import com.example.flowdesk_android.domain.repository.SuperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuperRepositoryImpl @Inject constructor(
    private val api: SuperApi
) : SuperRepository {

    override suspend fun getDashboard(): Result<SuperDashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboard()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("대시보드 조회 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
