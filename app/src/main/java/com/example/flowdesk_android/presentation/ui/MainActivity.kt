package com.example.flowdesk_android.presentation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.MenuDto
import com.example.flowdesk_android.databinding.ActivityMainBinding
import com.example.flowdesk_android.presentation.viewmodel.DashboardState
import com.example.flowdesk_android.presentation.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Handle window insets for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraPadding = (16 * v.resources.displayMetrics.density).toInt()
            v.updatePadding(top = systemBars.top + extraPadding)
            insets
        }

        observeViewModel()
        setupBottomNavigationListener()
        setupTopBarListeners()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboardState.collect { state ->
                    when (state) {
                        is DashboardState.Success -> {
                            setupBottomNavigation(state.data.menuTree ?: emptyList())
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private var currentMenuTree: List<MenuDto> = emptyList()

    private fun setupBottomNavigation(menuTree: List<MenuDto>) {
        currentMenuTree = menuTree.sortedBy { it.order }
        val menu = binding.bottomNavigation.menu
        menu.clear()

        currentMenuTree.forEachIndexed { index, menuDto ->
            val menuItem = menu.add(0, index, menuDto.order, menuDto.displayName)
            
            val iconRes = when (menuDto.pageName) {
                "super", "system_management" -> com.example.flowdesk_android.R.drawable.ic_super_admin
                "roles", "content_management" -> com.example.flowdesk_android.R.drawable.ic_roles
                "users", "user_management" -> com.example.flowdesk_android.R.drawable.ic_users
                "permissions" -> com.example.flowdesk_android.R.drawable.ic_permissions
                else -> com.example.flowdesk_android.R.drawable.ic_default_menu
            }
            menuItem.setIcon(iconRes)
        }
    }

    private fun setupBottomNavigationListener() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedMenu = currentMenuTree.getOrNull(item.itemId)
            if (selectedMenu != null) {
                try {
                    val navOptions = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .setLaunchSingleTop(true)
                        .build()

                    when {
                        selectedMenu.pageName == "user_management" || selectedMenu.pageName == "users" || selectedMenu.pageName == "permissions" || selectedMenu.displayName.contains("사용자") || selectedMenu.displayName.contains("권한") -> {
                            if (navController.currentDestination?.id != R.id.usersFragment) {
                                navController.navigate(R.id.usersFragment, null, navOptions)
                            }
                            true
                        }
                        else -> {
                            android.widget.Toast.makeText(this, "${selectedMenu.displayName} 페이지는 준비중입니다.", android.widget.Toast.LENGTH_SHORT).show()
                            true
                        }
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "화면 이동 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                false
            }
        }
    }

    private fun setupTopBarListeners() {
        binding.ivPerson.setOnClickListener {
            try {
                navController.navigate(R.id.myPageFragment)
                binding.tvTitle.text = "마이페이지" 
            } catch (e: Exception) {
            }
        }
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.editProfileFragment || destination.id == R.id.changePasswordFragment) {
                binding.bottomNavigation.visibility = android.view.View.GONE
                binding.topBar.visibility = android.view.View.GONE
            } else {
                binding.bottomNavigation.visibility = android.view.View.VISIBLE
                binding.topBar.visibility = android.view.View.VISIBLE
                
                when (destination.id) {
                    R.id.myPageFragment -> binding.tvTitle.text = "마이페이지"
                    R.id.homeFragment -> binding.tvTitle.text = "대시보드"
                    R.id.usersFragment -> binding.tvTitle.text = "사용자 & 권한"
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
