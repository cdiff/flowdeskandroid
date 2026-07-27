package com.example.flowdesk_android.feature.main.drawer

import com.example.flowdesk_android.feature.auth.domain.model.Menu

/**
 * 드로어 RecyclerView 아이템 sealed class
 */
sealed class DrawerRow {
    abstract val id: String

    data class Header(
        val menu: Menu,
        val cleanDisplayName: String,
        val iconRes: Int,
        val isExpanded: Boolean,
        val isSelected: Boolean,
        val hasSubItems: Boolean
    ) : DrawerRow() {
        override val id: String = menu.pageName
    }

    data class SubItem(
        val parentPageName: String,
        val subId: String,
        val displayName: String,
        val tabIndex: Int,
        val isSelected: Boolean
    ) : DrawerRow() {
        override val id: String = "${parentPageName}_$subId"
    }
}
