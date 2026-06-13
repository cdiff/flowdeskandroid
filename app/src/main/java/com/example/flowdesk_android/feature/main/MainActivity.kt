package com.example.flowdesk_android.feature.main

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
import com.example.flowdesk_android.feature.auth.domain.model.Menu
import com.example.flowdesk_android.databinding.ActivityMainBinding
import com.example.flowdesk_android.feature.mypage.presentation.main.MyPageUiState
import com.example.flowdesk_android.feature.mypage.presentation.main.MyPageViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardState
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: DashboardViewModel by viewModels()
    private var currentMenuTree: List<Menu> = emptyList()

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

    private var isFirstLoad = true

    private fun setupBottomNavigation(menuTree: List<Menu>) {
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

        if (isFirstLoad && currentMenuTree.isNotEmpty()) {
            isFirstLoad = false
            navigateToMenu(currentMenuTree.first())
        }
    }

    private fun setupBottomNavigationListener() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedMenu = currentMenuTree.getOrNull(item.itemId)
            if (selectedMenu != null) {
                navigateToMenu(selectedMenu)
                true
            } else {
                false
            }
        }
    }

    private fun navigateToMenu(selectedMenu: Menu) {
        try {
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, false)
                .setLaunchSingleTop(true)
                .build()

            val pageName = selectedMenu.pageName
            val bundle = Bundle().apply { putString("parent_page_name", pageName) }
            
            when {
                pageName == "user_management" || pageName == "super" || pageName == "system_management" || pageName == "content_management" || pageName == "counsel_management" -> {
                    navController.navigate(R.id.usersFragment, bundle, navOptions)
                }
                pageName == "users" || pageName == "permissions" || selectedMenu.displayName.contains("사용자") || selectedMenu.displayName.contains("권한") -> {
                    val userBundle = Bundle().apply { putString("parent_page_name", "user_management") }
                    navController.navigate(R.id.usersFragment, userBundle, navOptions)
                }
                else -> {
                    android.widget.Toast.makeText(this, "${selectedMenu.displayName} 페이지는 준비중입니다.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "화면 이동 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
        
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            if (destination.id == R.id.editProfileFragment ||
                destination.id == R.id.changePasswordFragment ||
                destination.id == R.id.counselDetailFragment ||
                destination.id == R.id.userDetailFragment ||
                destination.id == R.id.roleDetailFragment ||
                destination.id == R.id.tenantDetailFragment ||
                destination.id == R.id.managePermissionsFragment
            ) {
                binding.bottomNavigation.visibility = android.view.View.GONE
                binding.topBar.visibility = android.view.View.GONE
            } else {
                binding.bottomNavigation.visibility = android.view.View.VISIBLE
                binding.topBar.visibility = android.view.View.VISIBLE
                
                when (destination.id) {
                    R.id.myPageFragment -> binding.tvTitle.text = "마이페이지"
                    R.id.homeFragment -> binding.tvTitle.text = "대시보드"
                    R.id.usersFragment -> {
                        val pageName = arguments?.getString("parent_page_name")
                        val menuTitle = currentMenuTree.find { it.pageName == pageName }?.displayName
                            ?: "사용자 & 권한"
                        binding.tvTitle.text = menuTitle
                    }
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
