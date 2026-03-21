package com.example.flowdesk_android.presentation.ui.users

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.flowdesk_android.data.remote.dto.MenuDto
import com.example.flowdesk_android.presentation.ui.roles.RolesFragment

class OrganizationPagerAdapter(
    fragment: Fragment,
    private val tabs: List<MenuDto>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        val pageName = tabs[position].pageName
        return when (pageName) {
            "users" -> UserManagementFragment()
            "roles" -> RolesFragment()
            else -> Fragment() // Placeholder for permissions
        }
    }
}
