package com.example.flowdesk_android.feature.main.drawer

import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.auth.domain.model.Menu
import com.example.flowdesk_android.feature.main.MainDrawerState

object DrawerRowMapper {

    data class SubMenuItemDto(val id: String, val displayName: String, val tabIndex: Int)

    fun getSubMenuItems(parentMenu: Menu): List<SubMenuItemDto> {
        if (parentMenu.children.isNotEmpty()) {
            return parentMenu.children.mapIndexed { index, child ->
                SubMenuItemDto(child.pageName, child.displayName, index)
            }
        }
        return when (parentMenu.pageName) {
            "system_management" -> listOf(
                SubMenuItemDto("status", "테넌트 상태", 0),
                SubMenuItemDto("block", "차단 설정", 1),
                SubMenuItemDto("website", "웹사이트 관리", 2),
                SubMenuItemDto("board_type", "게시판 타입", 3)
            )
            "user_management", "super" -> listOf(
                SubMenuItemDto("users", "사용자 목록", 0),
                SubMenuItemDto("roles", "역할 목록", 1)
            )
            "content_management" -> listOf(
                SubMenuItemDto("board_post", "게시글 관리", 0)
            )
            else -> emptyList()
        }
    }

    fun mapStateToDrawerRows(state: MainDrawerState): List<DrawerRow> {
        val rows = mutableListOf<DrawerRow>()

        state.menuTree.forEach { menuDto ->
            val cleanDisplayName = if (menuDto.displayName.trim().endsWith("관리")) {
                val raw = menuDto.displayName.trim()
                raw.substring(0, raw.length - 2).trim()
            } else {
                menuDto.displayName
            }.replace("&", "·").trim()

            val iconRes = when (menuDto.pageName) {
                "super" -> R.drawable.ic_super_admin
                "user_management" -> R.drawable.ic_users
                "system_management" -> R.drawable.ic_system
                "content_management" -> R.drawable.ic_tenant_banner
                "counsel_management" -> R.drawable.ic_counsel
                else -> R.drawable.ic_default_menu
            }

            val subItems = getSubMenuItems(menuDto)
            val isExpanded = state.expandedPageNames.contains(menuDto.pageName)
            val isSelected = (menuDto.pageName == state.selectedPageName)

            rows.add(
                DrawerRow.Header(
                    menu = menuDto,
                    cleanDisplayName = cleanDisplayName,
                    iconRes = iconRes,
                    isExpanded = isExpanded,
                    isSelected = isSelected,
                    hasSubItems = subItems.isNotEmpty()
                )
            )

            if (isExpanded && subItems.isNotEmpty()) {
                subItems.forEach { subItem ->
                    val isSubSelected = (state.selectedSubId == subItem.id)
                    rows.add(
                        DrawerRow.SubItem(
                            parentPageName = menuDto.pageName,
                            subId = subItem.id,
                            displayName = subItem.displayName,
                            tabIndex = subItem.tabIndex,
                            isSelected = isSubSelected
                        )
                    )
                }
            }
        }

        return rows
    }
}
