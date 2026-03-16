package com.example.flowdesk_android.data.remote

import com.example.flowdesk_android.data.remote.dto.UsersResponse
import retrofit2.Response
import retrofit2.http.GET

import com.example.flowdesk_android.data.remote.dto.CreateUserRequest
import com.example.flowdesk_android.data.remote.dto.UserDto
import retrofit2.http.POST
import retrofit2.http.Body

interface UserApi {
    @GET("users")
    suspend fun getUsers(): Response<UsersResponse>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserDto>
}
