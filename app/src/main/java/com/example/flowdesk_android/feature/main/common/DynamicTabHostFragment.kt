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
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardState
import com.example.flowdesk_android.feature.auth.presentation.dashboard.DashboardViewModel
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
    private var lastSelectedPosition: Int = -1

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            lastSelectedPosition = position
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_dynamic_tab_host, container, false)
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)

        viewPager.registerOnPageChangeCallback(pageChangeCallback)
        observeTabs()
        return view
    }

    private fun observeTabs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.dashboardState.collect { state ->
                    if (state is DashboardState.Success) {
                        val menuTree = state.data.menuTree ?: return@collect

                        val parentPageName = arguments?.getString("parent_page_name") ?: "user_management"

                        // arguments에서 initial_tab_index를 1회성으로 소비하고 제거하여 백스택 복귀 시 이전 탭 위치가 유지되도록 보정
                        val hasInitialIndexArg = arguments?.containsKey("initial_tab_index") == true
                        val initialTabIndex = if (hasInitialIndexArg) {
                            val idx = arguments?.getInt("initial_tab_index", 0) ?: 0
                            arguments?.remove("initial_tab_index")
                            idx
                        } else -1

                        val parentMenu = menuTree.find { it.pageName == parentPageName }
                        val childrenList: List<Menu>? = parentMenu?.children
                        val children = childrenList?.sortedBy { menu -> menu.order }
                        if (children.isNullOrEmpty()) return@collect

                        if (currentTabs != children || viewPager.adapter == null) {
                            currentTabs = children
                            pagerAdapter = DynamicTabPagerAdapter(this@DynamicTabHostFragment, currentTabs)
                            viewPager.adapter = pagerAdapter

                            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                                tab.text = currentTabs[position].displayName
                                    .replace(" 관리", "")
                                    .trim()
                            }.attach()

                            val targetIndex = if (initialTabIndex >= 0) initialTabIndex else if (lastSelectedPosition >= 0) lastSelectedPosition else 0
                            if (targetIndex in 0 until children.size) {
                                viewPager.setCurrentItem(targetIndex, false)
                            }
                        } else {
                            if (initialTabIndex >= 0 && initialTabIndex in 0 until currentTabs.size) {
                                viewPager.setCurrentItem(initialTabIndex, true)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        viewPager.adapter = null
    }
}
