package com.example.flowdesk_android.presentation.ui.users

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
import com.example.flowdesk_android.data.remote.dto.MenuDto
import com.example.flowdesk_android.presentation.viewmodel.DashboardState
import com.example.flowdesk_android.presentation.viewmodel.DashboardViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrganizationHostFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var pagerAdapter: OrganizationPagerAdapter? = null
    private var currentTabs: List<MenuDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organization_host, container, false)
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
                        
                        val parentMenu = menuTree.firstOrNull { parent ->
                            parent.children.any { child ->
                                child.pageName in listOf("users", "roles", "permissions")
                            }
                        }
                        
                        val children = parentMenu?.children?.sortedBy { it.order }
                        if (children.isNullOrEmpty()) return@collect

                        if (currentTabs != children) {
                            currentTabs = children
                        }

                        if (viewPager.adapter == null) {
                            pagerAdapter = OrganizationPagerAdapter(this@OrganizationHostFragment, currentTabs)
                            viewPager.adapter = pagerAdapter
                            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                                tab.text = currentTabs[position].displayName
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
