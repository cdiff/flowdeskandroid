package com.example.flowdesk_android.core.extension

import androidx.fragment.app.Fragment
import com.example.flowdesk_android.core.ui.toast.ToastType
import com.example.flowdesk_android.core.ui.toast.TopToast

/**
 * Fragment에서 상단에 고품질 커스텀 디자인 애니메이션 토스트 메시지를 표시하는 확장 함수
 * (기존 showTopToast 호출처를 일절 수정하지 않고도 커스텀 팝업 UI를 노출합니다)
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
