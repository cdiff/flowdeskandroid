package com.example.flowdesk_android.feature.main.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.auth.domain.model.Menu
import com.example.flowdesk_android.feature.mypage.presentation.main.MyPageUiState
import com.example.flowdesk_android.feature.mypage.presentation.main.MyPageViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardViewModel
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardState
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DynamicTabHostFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var pagerAdapter: DynamicTabPagerAdapter? = null
    private var currentTabs: List<Menu> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_dynamic_tab_host, container, false)
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)
        
        observeTabs()
        return view
    }

    private fun observeTabs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.dashboardState.collect { state ->
                    if (state is DashboardState.Success) {
                        val menuTree = state.data.menuTree ?: return@collect
                        
                        // Arguments에서 parentPageName을 가져옴 (기본값: user_management)
                        val parentPageName = arguments?.getString("parent_page_name") ?: "user_management"
                        
                        // 매개변수로 받은 parentPageName에 맞는 대분류를 찾음
                        val parentMenu = menuTree.find { it.pageName == parentPageName }
                        
                        val childrenList: List<Menu>? = parentMenu?.children
                        val children = childrenList?.sortedBy { menu -> menu.order }
                        if (children == null || children.isEmpty()) return@collect

                            if (currentTabs != children || viewPager.adapter == null) {
                                currentTabs = children
                                pagerAdapter = DynamicTabPagerAdapter(this@DynamicTabHostFragment, currentTabs)
                                viewPager.adapter = pagerAdapter
                                
                                // TabLayoutMediator를 다시 연결하기 전에 이전 것 해제 (필요시)
                                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                                    tab.text = currentTabs[position].displayName
                                        .replace(" 관리", "")
                                        .trim()
                                }.attach()
                            }
                        }
                    }
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.adapter = null
    }
}
