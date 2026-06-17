package com.example.flowdesk_android.core.extension

import android.view.View
import android.widget.ListAdapter
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.example.flowdesk_android.R

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
