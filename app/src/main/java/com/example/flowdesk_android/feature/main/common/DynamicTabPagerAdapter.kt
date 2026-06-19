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
import com.example.flowdesk_android.feature.counsel_management.presentation.list.CounselListFragment
import com.example.flowdesk_android.feature.counsel_management.presentation.calendar.CounselCalendarFragment
import com.example.flowdesk_android.feature.system_management.presentation.status.TenantStatusFragment
import com.example.flowdesk_android.feature.system_management.presentation.block.SystemBlockFragment

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
            "counsels"           -> CounselListFragment()
            "counsels.calendar", "counsels.reservations" -> CounselCalendarFragment()

            // 상태 관리 (테넌트 상태)
            "super.status", "super.statuses", "system.status", "system.statuses", "tenants.status" -> TenantStatusFragment()

            // 차단 관리 (IP, 휴대폰, 금칙어)
            "super.block", "super.blocks", "system.block", "system.blocks", "security.block", "security.blocks", "security" -> SystemBlockFragment()

            else -> Fragment() // 준비 중인 페이지 Placeholder
        }
    }
}
