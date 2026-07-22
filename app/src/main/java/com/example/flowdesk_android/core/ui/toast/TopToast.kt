package com.example.flowdesk_android.core.ui.toast

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.R

enum class ToastType { SUCCESS, ERROR, INFO, WARNING }

/**
 * 하단 둥근 플로팅 스낵바 (Floating SnackBar)
 *
 * 1.5~2초간 하단에 둥근 캡슐 스타일로 노출 후 자동으로 소멸합니다.
 */
object TopToast {

    fun show(
        activity: Activity,
        message: String,
        type: ToastType = ToastType.INFO,
        durationMs: Long = 1800L
    ) {
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        
        // 기존에 이미 떠있는 토스트 뷰가 있다면 제거
        val existingView = rootView.findViewById<View>(R.id.floating_snackbar_root)
        if (existingView != null) {
            rootView.removeView(existingView)
        }

        val toastView = LayoutInflater.from(activity).inflate(R.layout.view_common_floating_snackbar, rootView, false)
        toastView.id = R.id.floating_snackbar_root

        toastView.findViewById<TextView>(R.id.tv_toast_message).text = message

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = (80 * activity.resources.displayMetrics.density).toInt()
        }

        rootView.addView(toastView, params)

        // 하단 슬라이드 업 + 페이드 인 애니메이션
        val slideIn = ObjectAnimator.ofFloat(toastView, View.TRANSLATION_Y, 100f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(toastView, View.ALPHA, 0f, 1f)
        AnimatorSet().apply {
            playTogether(slideIn, fadeIn)
            duration = 200
            start()
        }

        // 1.8초(1.5~2초 범위) 안내 후 부드럽게 페이드 아웃 + 슬라이드 다운 소멸
        toastView.postDelayed({
            if (toastView.parent != null) {
                val slideOut = ObjectAnimator.ofFloat(toastView, View.TRANSLATION_Y, 0f, 100f)
                val fadeOut = ObjectAnimator.ofFloat(toastView, View.ALPHA, 1f, 0f)
                AnimatorSet().apply {
                    playTogether(slideOut, fadeOut)
                    duration = 200
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            if (toastView.parent != null) {
                                rootView.removeView(toastView)
                            }
                        }
                    })
                    start()
                }
            }
        }, durationMs)
    }
}

/** Fragment에서 바로 쓸 수 있는 확장 함수 */
fun Fragment.showTopToast(message: String, type: ToastType = ToastType.INFO) {
    TopToast.show(requireActivity(), message, type)
}
