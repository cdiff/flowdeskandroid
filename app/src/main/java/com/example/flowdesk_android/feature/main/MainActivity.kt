package com.example.flowdesk_android.feature.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainNavigator {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()
    private var statusBarTopHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarTopHeight = systemBars.top
            val extraPadding = resources.getDimensionPixelSize(R.dimen.topbar_extra_padding_top)
            binding.topBar.updatePadding(top = systemBars.top + extraPadding)
            binding.navHostFragment.updatePadding(top = 0)
            insets
        }

        setupTopBarAndDrawerListeners()

        // 뒤로가기 버튼/제스처 처리 (드로어 열림 상태 우선 닫기)
        var backPressedTime: Long = 0
        val onBackPressedCallback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                    navController.popBackStack()
                } else {
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

        // MainViewModel 초기화 및 세션 수집 시작
        val authMeInfoJson = intent.getStringExtra("EXTRA_AUTH_ME_INFO")
        viewModel.init(authMeInfoJson)
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is MainUiState.Success) {
                        val menuTree = state.data.menuTree ?: emptyList()
                        viewModel.setMenuTree(menuTree)
                    }
                }
            }
        }
    }

    // ── MainNavigator 구현 ──────────────────────────────────────────────

    override fun navigateToTab(pageName: String, tabIndex: Int) {
        try {
            val bundle = Bundle().apply {
                putString("parent_page_name", pageName)
                putInt("initial_tab_index", tabIndex)
            }
            navController.navigate(R.id.usersFragment, bundle, buildNavOptions())
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "화면 이동 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun navigateToSubTab(pageName: String, subId: String, tabIndex: Int) {
        try {
            val bundle = Bundle().apply {
                putString("parent_page_name", pageName)
                putInt("initial_tab_index", tabIndex)
            }
            navController.navigate(R.id.usersFragment, bundle, buildNavOptions())
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "화면 이동 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildNavOptions() = androidx.navigation.NavOptions.Builder()
        .setPopUpTo(navController.graph.startDestinationId, false)
        .setLaunchSingleTop(true)
        .build()

    private fun setupTopBarAndDrawerListeners() {
        // 우측 햄버거 메뉴 버튼 (≡) 클릭 ➡️ 사이드 드로어 오픈
        binding.btnMenuHamburger.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // 우측 마이페이지 프로필 아이콘 클릭 ➡️ 마이페이지 이동
        binding.ivPerson.setOnClickListener {
            try {
                navController.navigate(R.id.myPageFragment)
            } catch (e: Exception) {
            }
        }

        // 좌측 뒤로가기 버튼(←) 클릭
        binding.btnNavBack.setOnClickListener {
            navController.popBackStack()
        }

        // 화면 이동 시 탑바 타이틀 및 버튼 상태 전환
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val subScreens = setOf(
                R.id.myPageFragment,
                R.id.editProfileFragment,
                R.id.changePasswordFragment,
                R.id.counselDetailFragment,
                R.id.userDetailFragment,
                R.id.roleDetailFragment,
                R.id.tenantDetailFragment,
                R.id.statusEditFragment,
                R.id.blockIpDetailFragment,
                R.id.blockPhoneDetailFragment,
                R.id.blockKeywordDetailFragment,
                R.id.managePermissionsFragment,
                R.id.inviteTeamFragment,
                R.id.invitePasswordFragment,
                R.id.inviteRoleFragment,
                R.id.websiteDetailFragment,
                R.id.websiteCreateFragment,
                R.id.boardTypeDetailFragment,
                R.id.boardPostDetailFragment,
                R.id.boardPostReadFragment
            )

            val isSubScreen = subScreens.contains(destination.id)

            if (destination.id == R.id.myPageFragment || destination.id == R.id.inviteTeamFragment) {
                // 마이페이지 & 팀원 초대: 자체 헤더 사용 ➡️ 메인 탑바 숨김
                binding.topBar.visibility = View.GONE
            } else if (isSubScreen) {
                // 서브 상세/수정/추가 화면: 메인 탑바 표출 (뒤로가기 ← 노출, 우측버튼 숨김)
                binding.topBar.visibility = View.VISIBLE
                binding.btnNavBack.visibility = View.VISIBLE
                binding.llRightActions.visibility = View.GONE
                binding.topBar.setBackgroundColor(getColor(R.color.white)) // 서브 화면: 흰색
                binding.tvTitle.textSize = 18f // 서브 화면: 18sp

                // 추가 모드(신규 등록) 화면은 타이틀 없음, 그 외 nav_graph label 자동 바인딩
                val titleText = when (destination.id) {
                    R.id.websiteCreateFragment -> "" // 웹사이트 추가: 타이틀 없음
                    R.id.statusEditFragment -> {
                        // tenantStatusId = -1L 이면 추가 모드 → 타이틀 없음
                        val id = arguments?.getLong("tenantStatusId", -1L) ?: -1L
                        if (id == -1L) "" else destination.label ?: ""
                    }
                    R.id.boardTypeDetailFragment -> {
                        // boardId = -1 이면 추가 모드 → 타이틀 없음
                        val id = arguments?.getLong("boardId", -1L) ?: -1L
                        if (id == -1L) "" else destination.label ?: ""
                    }
                    R.id.boardPostDetailFragment -> {
                        // postId = -1 이면 추가 모드 → 타이틀 없음
                        val id = arguments?.getLong("postId", -1L) ?: -1L
                        if (id == -1L) "" else destination.label ?: ""
                    }
                    else -> destination.label ?: ""
                }
                binding.tvTitle.text = titleText
            } else {
                // 메인 목록/탭 화면: 메인 탑바 표출 (뒤로가기 숨김, 우측버튼 노출)
                binding.topBar.visibility = View.VISIBLE
                binding.btnNavBack.visibility = View.GONE
                binding.llRightActions.visibility = View.VISIBLE
                binding.topBar.setBackgroundColor(getColor(R.color.bg_page)) // 메인 탭: 원래 색상
                binding.tvTitle.textSize = 20f // 메인 탭: 20sp

                when (destination.id) {
                    R.id.usersFragment -> {
                        val pageName = arguments?.getString("parent_page_name") ?: "user_management"
                        viewModel.updateSelectedPageName(pageName)
                        val rawTitle = viewModel.drawerState.value.menuTree.find { it.pageName == pageName }?.displayName
                            ?: "사용자 · 권한"
                        val menuTitle = rawTitle.replace("&", "·")
                        binding.tvTitle.text = menuTitle
                    }
                }
            }
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
