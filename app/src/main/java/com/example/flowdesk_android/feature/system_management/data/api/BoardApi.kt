package com.example.flowdesk_android.feature.system_management.data.api

import com.example.flowdesk_android.feature.system_management.data.dto.BoardTypeDto
import com.example.flowdesk_android.feature.system_management.data.dto.BoardTypeListResponseDto
import com.example.flowdesk_android.feature.system_management.data.dto.CreateBoardTypeRequestDto
import com.example.flowdesk_android.feature.system_management.data.dto.UpdateBoardTypeRequestDto
import com.example.flowdesk_android.feature.system_management.data.dto.BoardPostDto
import com.example.flowdesk_android.feature.system_management.data.dto.BoardPostListResponseDto
import com.example.flowdesk_android.feature.system_management.data.dto.CreateBoardPostRequestDto
import com.example.flowdesk_android.feature.system_management.data.dto.UpdateBoardPostRequestDto
import retrofit2.Response
import retrofit2.http.*

interface BoardApi {

    // ─── 게시판 타입 관련 APIs ───

    @GET("boards")
    suspend fun getBoardTypes(): Response<BoardTypeListResponseDto>

    @POST("boards")
    suspend fun createBoardType(
        @Body request: CreateBoardTypeRequestDto
    ): Response<BoardTypeDto>

    @GET("boards/{boardId}")
    suspend fun getBoardTypeDetail(
        @Path("boardId") boardId: Long
    ): Response<BoardTypeDto>

    @PATCH("boards/{boardId}")
    suspend fun updateBoardType(
        @Path("boardId") boardId: Long,
        @Body request: UpdateBoardTypeRequestDto
    ): Response<BoardTypeDto>

    @DELETE("boards/{boardId}")
    suspend fun deactivateBoardType(
        @Path("boardId") boardId: Long
    ): Response<Unit>


    // ─── 게시글 관련 APIs ───

    @GET("boards/{boardId}/posts")
    suspend fun getBoardPosts(
        @Path("boardId") boardId: Long,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String? = null
    ): Response<BoardPostListResponseDto>

    @POST("boards/{boardId}/posts")
    suspend fun createBoardPost(
        @Path("boardId") boardId: Long,
        @Body request: CreateBoardPostRequestDto
    ): Response<BoardPostDto>

    @GET("boards/{boardId}/posts/{postId}")
    suspend fun getBoardPostDetail(
        @Path("boardId") boardId: Long,
        @Path("postId") postId: Long
    ): Response<BoardPostDto>

    @PATCH("boards/{boardId}/posts/{postId}")
    suspend fun updateBoardPost(
        @Path("boardId") boardId: Long,
        @Path("postId") postId: Long,
        @Body request: UpdateBoardPostRequestDto
    ): Response<BoardPostDto>

    @DELETE("boards/{boardId}/posts/{postId}")
    suspend fun deleteBoardPost(
        @Path("boardId") boardId: Long,
        @Path("postId") postId: Long
    ): Response<Unit>
}
