package com.example.flowdesk_android.core.extension

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.R

/**
 * Fragment에서 상단에 커스텀 디자인 토스트 메시지를 표시하는 공통 확장 함수
 */
@Suppress("DEPRECATION")
fun Fragment.showTopToast(message: String) {
    val inflater = LayoutInflater.from(requireContext())
    val layout = inflater.inflate(R.layout.view_common_toast_top, null)
    layout.findViewById<TextView>(R.id.tv_toast_message).text = message

    Toast(requireContext()).apply {
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        duration = Toast.LENGTH_SHORT
        view = layout
        show()
    }
}
