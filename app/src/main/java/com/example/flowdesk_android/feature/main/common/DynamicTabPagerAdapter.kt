package com.example.flowdesk_android.feature.main.common

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.flowdesk_android.feature.auth.domain.model.Menu
import com.example.flowdesk_android.feature.user_management.presentation.roles.list.RolesFragment
import com.example.flowdesk_android.feature.user_management.presentation.catalog.PermissionCatalogFragment
import com.example.flowdesk_android.feature.user_management.presentation.users.list.UserListFragment
import com.example.flowdesk_android.feature.super_admin.presentation.dashboard.SuperDashboardFragment
import com.example.flowdesk_android.feature.super_admin.presentation.tenants.TenantsFragment
import com.example.flowdesk_android.feature.super_admin.presentation.pages.SuperAdminPagesFragment
import com.example.flowdesk_android.feature.super_admin.presentation.actions.SuperAdminActionsFragment
import com.example.flowdesk_android.feature.counsel_management.presentation.dashboard.CounselDashboardFragment

class DynamicTabPagerAdapter(
    fragment: Fragment,
    private val tabs: List<Menu>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        val pageName = tabs[position].pageName
        return when (pageName) {
            "users" -> UserListFragment()
            "roles" -> RolesFragment()
            "permissions" -> PermissionCatalogFragment.newInstance()
            "super.dashboard" -> SuperDashboardFragment()
            "super.tenants"   -> TenantsFragment()
            "super.pages"     -> SuperAdminPagesFragment()
            "super.actions"   -> SuperAdminActionsFragment()

            // 상담 관리
            "counsels.dashboard" -> CounselDashboardFragment()

            // 추후 슈퍼 관리자나 시스템 관리 자식 페이지들도 여기에 추가하면 됩니다.
            
            else -> Fragment() // 준비 중인 페이지 Placeholder
        }
    }
}
