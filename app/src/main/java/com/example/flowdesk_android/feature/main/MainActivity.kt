package com.example.flowdesk_android.feature.main

import android.content.res.ColorStateList
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
import com.example.flowdesk_android.databinding.ItemDrawerMenuBinding
import com.example.flowdesk_android.databinding.ItemDrawerSubMenuBinding
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.auth.domain.model.Menu
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
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is MainUiState.Success -> {
                                val authInfo = state.data
                                val menuTree = authInfo.menuTree ?: emptyList()

                                // 드로어 프로필 설정 및 ViewModel에 메뉴 트리 전달
                                setupDrawerHeader(authInfo)
                                viewModel.setMenuTree(menuTree)
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.drawerState.collect { drawerState ->
                        setupDrawerMenu(drawerState)
                    }
                }
            }
        }
    }

    private fun setupDrawerHeader(authInfo: AuthMeInfo) {
        val header = binding.layoutDrawerHeader
        header.tvDrawerUserName.text = authInfo.user.name.ifEmpty { "사용자" }
        header.tvDrawerUserEmail.text = authInfo.user.email

        header.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        header.btnDrawerMypage.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            try {
                navController.navigate(R.id.myPageFragment)
            } catch (e: Exception) {
            }
        }

        header.btnDrawerLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            finish()
        }
    }

    private fun setupDrawerMenu(drawerState: MainDrawerState) {
        val container = binding.llDrawerMenuList
        container.removeAllViews()

        val primaryBlue = getColor(R.color.drawer_item_selected)
        val defaultSlate = getColor(R.color.slate_600)
        val textDark = getColor(R.color.text_primary)

        drawerState.menuTree.forEach { menuDto ->
            val itemBinding = ItemDrawerMenuBinding.inflate(layoutInflater, container, false)

            val cleanDisplayName = if (menuDto.displayName.trim().endsWith("관리")) {
                val raw = menuDto.displayName.trim()
                raw.substring(0, raw.length - 2).trim()
            } else {
                menuDto.displayName
            }.replace("&", "·").trim()

            itemBinding.tvMenuName.text = cleanDisplayName

            val iconRes = when (menuDto.pageName) {
                "super" -> R.drawable.ic_super_admin
                "user_management" -> R.drawable.ic_users
                "system_management" -> R.drawable.ic_system
                "content_management" -> R.drawable.ic_tenant_banner
                "counsel_management" -> R.drawable.ic_counsel
                else -> R.drawable.ic_default_menu
            }
            itemBinding.ivMenuIcon.setImageResource(iconRes)

            val isSelected = (menuDto.pageName == drawerState.selectedPageName)
            itemBinding.llDrawerMenuItem.isSelected = isSelected

            // 아이콘 및 텍스트 선택 활성 색상 피드백
            if (isSelected) {
                itemBinding.ivMenuIcon.imageTintList = ColorStateList.valueOf(primaryBlue)
                itemBinding.tvMenuName.setTextColor(primaryBlue)
            } else {
                itemBinding.ivMenuIcon.imageTintList = ColorStateList.valueOf(defaultSlate)
                itemBinding.tvMenuName.setTextColor(textDark)
            }

            // 하위 메뉴(Children) 확인 및 아코디언 처리
            val subItems = getSubMenuItems(menuDto)
            val isExpanded = drawerState.expandedPageNames.contains(menuDto.pageName)

            if (subItems.isNotEmpty()) {
                itemBinding.ivChevron.visibility = View.VISIBLE
                itemBinding.ivChevron.rotation = if (isExpanded) 90f else 0f
            } else {
                itemBinding.ivChevron.visibility = View.GONE
            }

            itemBinding.llDrawerMenuItem.setOnClickListener {
                viewModel.selectMenu(menuDto.pageName, subItems.isNotEmpty())
                if (subItems.isEmpty()) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    navigateToTab(menuDto.pageName)
                }
            }

            // 우측 화살표 아이콘 클릭 동일 동작
            itemBinding.ivChevron.setOnClickListener {
                if (subItems.isNotEmpty()) {
                    viewModel.toggleExpand(menuDto.pageName)
                }
            }

            container.addView(itemBinding.root)

            // 아코디언이 펼쳐진 상태일 경우 하위 메뉴 목록 인플레이션
            if (isExpanded && subItems.isNotEmpty()) {
                subItems.forEach { subItem ->
                    val subBinding = ItemDrawerSubMenuBinding.inflate(layoutInflater, container, false)
                    subBinding.tvSubMenuName.text = subItem.displayName

                    val isSubSelected = (drawerState.selectedSubId == subItem.id)
                    subBinding.llDrawerSubItem.isSelected = isSubSelected

                    if (isSubSelected) {
                        subBinding.vBullet.backgroundTintList = ColorStateList.valueOf(primaryBlue)
                        subBinding.tvSubMenuName.setTextColor(primaryBlue)
                    } else {
                        subBinding.vBullet.backgroundTintList = ColorStateList.valueOf(getColor(R.color.slate_400))
                        subBinding.tvSubMenuName.setTextColor(getColor(R.color.slate_700))
                    }

                    subBinding.llDrawerSubItem.setOnClickListener {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        viewModel.selectSubMenu(menuDto.pageName, subItem.id)
                        navigateToSubTab(menuDto.pageName, subItem.id, subItem.tabIndex)
                    }

                    container.addView(subBinding.root)
                }
            }
        }
    }

    private data class SubMenuItem(val id: String, val displayName: String, val tabIndex: Int)

    private fun getSubMenuItems(parentMenu: Menu): List<SubMenuItem> {
        if (parentMenu.children.isNotEmpty()) {
            return parentMenu.children.mapIndexed { index, child ->
                SubMenuItem(child.pageName, child.displayName, index)
            }
        }
        return when (parentMenu.pageName) {
            "system_management" -> listOf(
                SubMenuItem("status", "테넌트 상태", 0),
                SubMenuItem("block", "차단 설정", 1),
                SubMenuItem("website", "웹사이트 관리", 2),
                SubMenuItem("board_type", "게시판 타입", 3)
            )
            "user_management", "super" -> listOf(
                SubMenuItem("users", "사용자 목록", 0),
                SubMenuItem("roles", "역할 목록", 1)
            )
            "content_management" -> listOf(
                SubMenuItem("board_post", "게시글 관리", 0)
            )
            else -> emptyList()
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
