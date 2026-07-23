package com.example.flowdesk_android.core.extension

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.core.ui.toast.ToastType
import com.example.flowdesk_android.core.ui.toast.TopToast
import com.example.flowdesk_android.data.local.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Fragment에서 하단에 둥근 플로팅 스낵바(Floating SnackBar) 메시지를 표시하는 확장 함수
 * (1.8초간 안내 후 자동으로 부드럽게 소멸)
 */
fun Fragment.showTopToast(message: String) {
    activity?.let { activeActivity ->
        TopToast.show(
            activity = activeActivity,
            message = message,
            type = ToastType.INFO
        )
    }
}

fun Fragment.showToast(message: String) {
    showTopToast(message)
}

fun Fragment.showFloatingSnackBar(message: String) {
    showTopToast(message)
}
/**
 * SessionManager의 특정 권한을 라이프사이클에 맞춰 반응형으로 관찰하는 확장 함수
 */
fun Fragment.observePermission(
    sessionManager: SessionManager,
    permissionKey: String,
    action: (Boolean) -> Unit
) {
    collectFlow(sessionManager.observePermission(permissionKey)) { hasPermission ->
        action(hasPermission)
    }
}

/**
 * 특정 권한 여부에 따라 View의 가시성(Visibility)을 실시간 반응형으로 바인딩하는 확장 함수
 */
fun View.visibleIfPermission(
    fragment: Fragment,
    sessionManager: SessionManager,
    permissionKey: String
) {
    fragment.collectFlow(sessionManager.observePermission(permissionKey)) { hasPermission ->
        this.visibility = if (hasPermission) View.VISIBLE else View.GONE
    }
}

/**
 * 특정 권한 여부에 따라 View의 활성화 상태(Enabled)를 실시간 반응형으로 바인딩하는 확장 함수
 */
fun View.enableIfPermission(
    fragment: Fragment,
    sessionManager: SessionManager,
    permissionKey: String
) {
    fragment.collectFlow(sessionManager.observePermission(permissionKey)) { hasPermission ->
        this.isEnabled = hasPermission
    }
}
