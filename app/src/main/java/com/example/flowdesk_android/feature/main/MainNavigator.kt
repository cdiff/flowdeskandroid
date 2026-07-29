package com.example.flowdesk_android.feature.main

/**
 * 메인 화면 네비게이션 계약.
 * MainActivity가 구현하고, MainDrawerFragment 등이 이 interface만 바라본다.
 * 테스트 시 mock으로 대체 가능.
 */
interface MainNavigator {
    /** 탭 호스트 화면으로 이동 (pageName: 메뉴 pageName, tabIndex: 초기 탭 인덱스) */
    fun navigateToTab(pageName: String, tabIndex: Int = 0)

    /** 서브메뉴 탭 화면으로 이동 */
    fun navigateToSubTab(pageName: String, subId: String, tabIndex: Int)
}
