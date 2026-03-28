package com.example.flowdesk_android.presentation.ui.common

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.flowdesk_android.data.remote.dto.MenuDto
import com.example.flowdesk_android.presentation.ui.roles.RolesFragment
import com.example.flowdesk_android.presentation.ui.roles.PermissionCatalogFragment
import com.example.flowdesk_android.presentation.ui.users.UserListFragment
import com.example.flowdesk_android.presentation.ui.super_admin.SuperDashboardFragment

class DynamicTabPagerAdapter(
    fragment: Fragment,
    private val tabs: List<MenuDto>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        val pageName = tabs[position].pageName
        return when (pageName) {
            "users" -> UserListFragment()
            "roles" -> RolesFragment()
            "permissions" -> PermissionCatalogFragment.newInstance()
            "super.dashboard" -> SuperDashboardFragment()
            
            // 추후 슈퍼 관리자나 시스템 관리 자식 페이지들도 여기에 추가하면 됩니다.
            // "super.tenants" -> SuperTenantsFragment()
            
            else -> Fragment() // 준비 중인 페이지 Placeholder
        }
    }
}
