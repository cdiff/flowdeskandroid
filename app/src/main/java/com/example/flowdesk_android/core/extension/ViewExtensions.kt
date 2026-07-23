package com.example.flowdesk_android.core.extension

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ── 1. 기본 가시성 제어 확장 함수 ──────────────────────────
fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

// ── 2. Lifecycle-safe Flow 수집 확장 함수 ─────────────────
fun <T> Fragment.collectFlow(
    flow: Flow<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state) {
            flow.collect { action(it) }
        }
    }
}

// ── 3. 기존 UI 제어 확장 함수 ──────────────────────────────
/**
 * 공통 커스텀 드롭다운 ListPopupWindow 노출 확장 함수
 * 앵커 뷰에 지정된 스타일의 팝업창을 인플레이트하고 어댑터를 바인딩하여 띄워줍니다.
 */
fun View.showCustomDropdown(
    adapter: ListAdapter,
    onItemClick: (position: Int) -> Unit
): ListPopupWindow {
    val popup = ListPopupWindow(context).apply {
        anchorView = this@showCustomDropdown
        width = this@showCustomDropdown.width
        isModal = true
        height = ListPopupWindow.WRAP_CONTENT
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_spinner_popup))
        setAdapter(adapter)
        setOnItemClickListener { _, _, position, _ ->
            onItemClick(position)
            dismiss()
        }
    }
    popup.show()
    
    // 둥근 모서리가 잘리지 않도록 아웃라인 클립 해제 및 elevation(그림자) 적용
    popup.listView?.clipToOutline = false
    popup.listView?.elevation = 8f * context.resources.displayMetrics.density
    
    return popup
}

/**
 * 뷰를 읽기 전용 혹은 쓰기 모드로 변경
 */
fun View.setReadOnly(isReadOnly: Boolean, vararg siblingViews: View) {
    this.isEnabled = !isReadOnly
    this.isClickable = !isReadOnly
    this.isFocusable = !isReadOnly

    if (this is EditText) {
        this.setTextColor(Color.parseColor(if (isReadOnly) "#94A3B8" else "#0F172A"))
    } else if (this is TextView) {
        this.setTextColor(Color.parseColor(if (isReadOnly) "#94A3B8" else "#0F172A"))
    }

    siblingViews.forEach { sibling ->
        sibling.visibility = if (isReadOnly) View.GONE else View.VISIBLE
    }
}

/**
 * EditText 포커스 시 구분선(Line View) 색상 하이라이트 제어
 */
fun EditText.setupFocusHighlight(lineView: View, focusColorHex: String = "#3B82F6", defaultColorHex: String = "#E2E8F0") {
    this.setOnFocusChangeListener { _, hasFocus ->
        lineView.setBackgroundColor(
            Color.parseColor(if (hasFocus) focusColorHex else defaultColorHex)
        )
    }
}

/**
 * 둥근 사각형 색상 인디케이터 지정
 */
fun View.updateColorIndicator(hexColor: String, defaultColorHex: String = "#3B82F6") {
    try {
        val parsedColor = Color.parseColor(hexColor)
        val density = resources.displayMetrics.density
        val radius = 6 * density
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(parsedColor)
        }
        this.background = drawable
    } catch (e: Exception) {
        val density = resources.displayMetrics.density
        val radius = 6 * density
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.parseColor(defaultColorHex))
        }
        this.background = drawable
    }
}
