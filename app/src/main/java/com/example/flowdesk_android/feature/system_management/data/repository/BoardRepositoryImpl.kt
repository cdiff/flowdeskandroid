package com.example.flowdesk_android.feature.system_management.data.repository

import com.example.flowdesk_android.core.network.parseErrorMessage
import com.example.flowdesk_android.feature.system_management.data.api.BoardApi
import com.example.flowdesk_android.feature.system_management.data.dto.CreateBoardTypeRequestDto
import com.example.flowdesk_android.feature.system_management.data.dto.UpdateBoardTypeRequestDto
import com.example.flowdesk_android.feature.system_management.data.dto.CreateBoardPostRequestDto
import com.example.flowdesk_android.feature.system_management.data.dto.UpdateBoardPostRequestDto
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import com.example.flowdesk_android.feature.system_management.domain.model.PageInfo
import com.example.flowdesk_android.feature.system_management.domain.repository.BoardRepository
import javax.inject.Inject

class BoardRepositoryImpl @Inject constructor(
    private val boardApi: BoardApi
) : BoardRepository {

    private inline fun <T, R> handleApiResponse(
        call: () -> retrofit2.Response<T>,
        transform: (T) -> R
    ): Result<R> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(transform(body))
                } else {
                    Result.failure(Exception("응답 바디가 비어있습니다."))
                }
            } else {
                Result.failure(Exception(response.parseErrorMessage("알 수 없는 API 에러")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBoardTypes(): Result<List<BoardType>> {
        return handleApiResponse(
            call = { boardApi.getBoardTypes() },
            transform = { dto -> dto.items.map { it.toDomain() } }
        )
    }

    override suspend fun createBoardType(
        boardKey: String,
        name: String,
        description: String?,
        sortOrder: Int
    ): Result<BoardType> {
        val request = CreateBoardTypeRequestDto(boardKey, name, description, sortOrder)
        return handleApiResponse(
            call = { boardApi.createBoardType(request) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun getBoardTypeDetail(boardId: Long): Result<BoardType> {
        return handleApiResponse(
            call = { boardApi.getBoardTypeDetail(boardId) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun updateBoardType(
        boardId: Long,
        name: String,
        description: String?,
        sortOrder: Int,
        isActive: Boolean
    ): Result<BoardType> {
        val request = UpdateBoardTypeRequestDto(name, description, sortOrder, if (isActive) 1 else 0)
        return handleApiResponse(
            call = { boardApi.updateBoardType(boardId, request) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun deactivateBoardType(boardId: Long): Result<Unit> {
        return try {
            val response = boardApi.deactivateBoardType(boardId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.parseErrorMessage("비활성화 에러")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBoardPosts(
        boardId: Long,
        page: Int,
        limit: Int,
        query: String?
    ): Result<Pair<List<BoardPost>, PageInfo>> {
        return handleApiResponse(
            call = { boardApi.getBoardPosts(boardId, page, limit, query) },
            transform = { dto ->
                val domainItems = dto.items.map { it.toDomain() }
                val domainPageInfo = dto.pageInfo.toDomain()
                Pair(domainItems, domainPageInfo)
            }
        )
    }

    override suspend fun createBoardPost(
        boardId: Long,
        title: String,
        content: String,
        isNotice: Boolean,
        startDtm: String?,
        endDtm: String?
    ): Result<BoardPost> {
        val request = CreateBoardPostRequestDto(
            title = title,
            content = content,
            isNotice = if (isNotice) 1 else 0,
            startDtm = startDtm,
            endDtm = endDtm
        )
        return handleApiResponse(
            call = { boardApi.createBoardPost(boardId, request) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun getBoardPostDetail(boardId: Long, postId: Long): Result<BoardPost> {
        return handleApiResponse(
            call = { boardApi.getBoardPostDetail(boardId, postId) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun updateBoardPost(
        boardId: Long,
        postId: Long,
        title: String,
        content: String,
        isNotice: Boolean,
        isActive: Boolean,
        startDtm: String?,
        endDtm: String?
    ): Result<BoardPost> {
        val request = UpdateBoardPostRequestDto(
            title = title,
            content = content,
            isNotice = if (isNotice) 1 else 0,
            isActive = if (isActive) 1 else 0,
            startDtm = startDtm,
            endDtm = endDtm
        )
        return handleApiResponse(
            call = { boardApi.updateBoardPost(boardId, postId, request) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun deleteBoardPost(boardId: Long, postId: Long): Result<Unit> {
        return try {
            val response = boardApi.deleteBoardPost(boardId, postId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.parseErrorMessage("삭제 에러")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
