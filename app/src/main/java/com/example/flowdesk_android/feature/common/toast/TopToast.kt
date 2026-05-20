package com.example.flowdesk_android.feature.common.toast

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
 * 커스텀 상단 토스트
 *
 * 사용 예시 (Fragment):
 * ```kotlin
 * TopToast.show(requireActivity(), "저장 완료", ToastType.SUCCESS)
 * ```
 */
object TopToast {

    fun show(
        activity: Activity,
        message: String,
        type: ToastType = ToastType.INFO,
        durationMs: Long = 2500
    ) {
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val toastView = LayoutInflater.from(activity).inflate(R.layout.common_toast_top, rootView, false)

        toastView.findViewById<TextView>(R.id.tv_toast_message).text = message

        // 타입별 배경 색 적용
        val bgColor = when (type) {
            ToastType.SUCCESS -> android.R.color.holo_green_dark
            ToastType.ERROR -> android.R.color.holo_red_dark
            ToastType.WARNING -> android.R.color.holo_orange_dark
            ToastType.INFO -> android.R.color.holo_blue_dark
        }
        toastView.setBackgroundColor(activity.getColor(bgColor))

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP }

        rootView.addView(toastView, params)

        // 슬라이드 인 애니메이션
        val slideIn = ObjectAnimator.ofFloat(toastView, View.TRANSLATION_Y, -200f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(toastView, View.ALPHA, 0f, 1f)
        AnimatorSet().apply {
            playTogether(slideIn, fadeIn)
            duration = 250
            start()
        }

        toastView.postDelayed({
            val slideOut = ObjectAnimator.ofFloat(toastView, View.TRANSLATION_Y, 0f, -200f)
            val fadeOut = ObjectAnimator.ofFloat(toastView, View.ALPHA, 1f, 0f)
            AnimatorSet().apply {
                playTogether(slideOut, fadeOut)
                duration = 250
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        rootView.removeView(toastView)
                    }
                })
                start()
            }
        }, durationMs)
    }
}

/** Fragment에서 바로 쓸 수 있는 확장 함수 */
fun Fragment.showTopToast(message: String, type: ToastType = ToastType.INFO) {
    TopToast.show(requireActivity(), message, type)
}
