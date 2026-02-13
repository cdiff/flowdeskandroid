package com.example.flowdesk_android.presentation.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.data.remote.dto.MenuDto
import com.example.flowdesk_android.presentation.viewmodel.DashboardState
import com.example.flowdesk_android.presentation.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        // Handle window insets for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraPadding = (16 * v.resources.displayMetrics.density).toInt() // 16dp extra
            v.updatePadding(top = systemBars.top + extraPadding) // Add status bar height + extra padding
            insets
        }

        observeViewModel()
        setupBottomNavigationListener()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboardState.collect { state ->
                    when (state) {
                        is DashboardState.Success -> {
                            updateUI(state)
                            setupBottomNavigation(state.data.menuTree)
                        }
                        is DashboardState.Error -> {
                            binding.tvGreeting.text = "Error: ${state.message}"
                        }
                        is DashboardState.Loading -> {
                            binding.tvGreeting.text = "Loading..."
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateUI(state: DashboardState.Success) {
        val user = state.data.user
        binding.tvGreeting.text = "Welcome, ${user.userName}\n(${user.userId})"
    }

    private fun setupBottomNavigation(menuTree: List<MenuDto>) {
        val menu = binding.bottomNavigation.menu
        menu.clear()

        menuTree.sortedBy { it.order }.forEachIndexed { index, menuDto ->
             // Using index as simple ID for menu item
            val menuItem = menu.add(0, index, menuDto.order, menuDto.displayName)
            
            val iconRes = when (menuDto.pageName) {
                "super" -> com.example.flowdesk_android.R.drawable.ic_super_admin
                "roles" -> com.example.flowdesk_android.R.drawable.ic_roles
                "users" -> com.example.flowdesk_android.R.drawable.ic_users
                "permissions" -> com.example.flowdesk_android.R.drawable.ic_permissions
                else -> com.example.flowdesk_android.R.drawable.ic_default_menu
            }
            menuItem.setIcon(iconRes)
        }
    }

    private fun setupBottomNavigationListener() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val index = item.itemId
            // TODO: Navigate to the correct fragment based on index or tag
            // Using placeholder logic since fragments for roles/users/permissions don't exist yet
            binding.tvGreeting.text = "Navigation unavailable for now\nSelected Item: ${item.title}"
            true
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
