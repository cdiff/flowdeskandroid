package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RolesViewModelTest {

    private val roleRepository: RoleRepository = mockk(relaxed = true)
    private lateinit var viewModel: RolesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RolesViewModel(roleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchRoles success updates uiState and filteredRoles`() = runTest {
        // Given
        val mockRoles = listOf(
            Role(roleId = 1, roleName = "ADMIN", displayName = "관리자", description = "관리자 역할", isActive = true, userCount = 3, permissionCount = 10, createdAt = "2026-06-22T08:00:00.000Z"),
            Role(roleId = 2, roleName = "USER", displayName = "일반사용자", description = "일반 사용자 역할", isActive = true, userCount = 5, permissionCount = 5, createdAt = "2026-06-22T08:00:00.000Z")
        )
        coEvery { roleRepository.getRoles() } returns Result.success(mockRoles)

        // When
        viewModel.fetchRoles()
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is RoleListUiState.Success)
        val successState = viewModel.uiState.value as RoleListUiState.Success
        assertEquals(2, successState.roles.size)
        assertEquals("ADMIN", successState.roles[0].roleName)
        assertEquals(2, viewModel.filteredRoles.value.size)
    }

    @Test
    fun `fetchRoles failure updates uiState to Error`() = runTest {
        // Given
        val errorMessage = "네트워크 오류"
        coEvery { roleRepository.getRoles() } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.fetchRoles()
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is RoleListUiState.Error)
        val errorState = viewModel.uiState.value as RoleListUiState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `search filters roles correctly`() = runTest {
        // Given
        val mockRoles = listOf(
            Role(roleId = 1, roleName = "ADMIN", displayName = "관리자", description = "관리자 역할", isActive = true, userCount = 3, permissionCount = 10, createdAt = "2026-06-22T08:00:00.000Z"),
            Role(roleId = 2, roleName = "USER", displayName = "일반사용자", description = "일반 사용자 역할", isActive = true, userCount = 5, permissionCount = 5, createdAt = "2026-06-22T08:00:00.000Z")
        )
        coEvery { roleRepository.getRoles() } returns Result.success(mockRoles)

        viewModel.fetchRoles()
        testScheduler.advanceUntilIdle()

        // When
        viewModel.search("관리자")

        // Then
        assertEquals(1, viewModel.filteredRoles.value.size)
        assertEquals("ADMIN", viewModel.filteredRoles.value[0].roleName)
    }
}
