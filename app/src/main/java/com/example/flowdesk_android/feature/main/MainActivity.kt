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
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardState
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

        setupBottomNavigationListener()
        setupTopBarListeners()

        // 뒤로가기 더블 클릭 종료 처리
        var backPressedTime: Long = 0
        val onBackPressedCallback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 상세 화면 등 백스택이 존재할 경우 이전 화면으로 이동
                if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                    navController.popBackStack()
                } else {
                    // 최하단 탭 화면일 경우 2초 이내 더블 클릭 시 종료
                    if (System.currentTimeMillis() - backPressedTime < 2000) {
                        finish()
                    } else {
                        backPressedTime = System.currentTimeMillis()
                        android.widget.Toast.makeText(this@MainActivity, "한 번 더 누르면 앱이 종료됩니다.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 1. 전달받은 인증 및 권한 데이터가 있는지 확인
        val authMeInfoJson = intent.getStringExtra("EXTRA_AUTH_ME_INFO")
        if (!authMeInfoJson.isNullOrEmpty()) {
            try {
                val authMeInfo = com.google.gson.Gson().fromJson(authMeInfoJson, com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo::class.java)
                setupDynamicNavigation(authMeInfo)
            } catch (e: Exception) {
                observeViewModel()
            }
        } else {
            // 2. 전달된 데이터가 없으면 기존 방식(비동기 호출 관찰)으로 폴백
            observeViewModel()
        }
    }

    private fun setupDynamicNavigation(info: com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo) {
        val menuTree = info.menuTree ?: emptyList()
        if (menuTree.isNotEmpty()) {
            // 네비게이션 그래프 동적 설정
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_main)
            val firstMenu = menuTree.sortedBy { it.order }.firstOrNull()
            
            val startDestId = when (firstMenu?.pageName) {
                "super", "system_management" -> R.id.usersFragment
                "counsel_management" -> R.id.counselDashboardFragment
                else -> R.id.homeFragment
            }
            
            navGraph.setStartDestination(startDestId)
            navController.graph = navGraph
            
            // 하단 탭바 바인딩
            setupBottomNavigation(menuTree)
        } else {
            observeViewModel()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboardState.collect { state ->
                    when (state) {
                        is DashboardState.Success -> {
                            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_main)
                            val firstMenu = state.data.menuTree?.sortedBy { it.order }?.firstOrNull()
                            val startDestId = when (firstMenu?.pageName) {
                                "super", "system_management" -> R.id.usersFragment
                                "counsel_management" -> R.id.counselDashboardFragment
                                else -> R.id.homeFragment
                            }
                            navGraph.setStartDestination(startDestId)
                            navController.graph = navGraph
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
            val cleanDisplayName = if (menuDto.displayName.trim().endsWith("관리")) {
                val raw = menuDto.displayName.trim()
                raw.substring(0, raw.length - 2).trim()
            } else {
                menuDto.displayName
            }.replace("&", "·").trim()
            val menuItem = menu.add(0, index, menuDto.order, cleanDisplayName)
            
            val iconRes = when (menuDto.pageName) {
                "super" -> R.drawable.selector_nav_super_admin
                "user_management" -> R.drawable.selector_nav_users
                "system_management" -> R.drawable.selector_nav_system
                "content_management" -> R.drawable.selector_nav_content
                "counsel_management" -> R.drawable.selector_nav_counsel
                else -> R.drawable.ic_default_menu
            }
            menuItem.setIcon(iconRes)
        }

        // 복원된 navController의 백스택 parent_page_name이 있는지 확인
        val currentBackStackEntry = navController.currentBackStackEntry
        val restoredPageName = currentBackStackEntry?.arguments?.getString("parent_page_name")
        var restoredIndex = -1
        if (restoredPageName != null) {
            restoredIndex = currentMenuTree.indexOfFirst { it.pageName == restoredPageName }
        }

        if (restoredIndex != -1) {
            isFirstLoad = false
            // 선택 변경 리스너를 잠시 해제해 중복 전환 방지 후 탭 선택 상태만 강제 복원
            binding.bottomNavigation.setOnItemSelectedListener(null)
            binding.bottomNavigation.selectedItemId = restoredIndex
            setupBottomNavigationListener()
        } else if (isFirstLoad && currentMenuTree.isNotEmpty()) {
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
                destination.id == R.id.statusEditFragment ||
                destination.id == R.id.blockIpDetailFragment ||
                destination.id == R.id.blockPhoneDetailFragment ||
                destination.id == R.id.blockKeywordDetailFragment ||
                destination.id == R.id.managePermissionsFragment ||
                destination.id == R.id.inviteTeamFragment
            ) {
                 binding.bottomNavigation.visibility = android.view.View.GONE
                 binding.bottomNavigationDivider.visibility = android.view.View.GONE
                 binding.topBar.visibility = android.view.View.GONE
             } else {
                 binding.bottomNavigation.visibility = android.view.View.VISIBLE
                 binding.bottomNavigationDivider.visibility = android.view.View.VISIBLE
                 binding.topBar.visibility = android.view.View.VISIBLE
                
                when (destination.id) {
                    R.id.myPageFragment -> binding.tvTitle.text = "마이페이지"
                    R.id.homeFragment -> binding.tvTitle.text = "대시보드"
                    R.id.usersFragment -> {
                        val pageName = arguments?.getString("parent_page_name")
                        val rawTitle = currentMenuTree.find { it.pageName == pageName }?.displayName
                            ?: "사용자 · 권한"
                        val menuTitle = rawTitle.replace("&", "·")
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
