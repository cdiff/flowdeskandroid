package com.example.flowdesk_android.core.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 루트 뷰에서 WindowInsets를 받아 툴바의 topMargin으로 적용합니다.
        // ConstraintLayout인 툴바의 padding을 직접 주면 자식 뷰들이 status bar 영역에 겹쳐서 배치되는 현상이 있어 topMargin을 조절하는 방식으로 개선합니다.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            getToolbarView(view)?.let { toolbar ->
                // 1. 툴바를 상태 표시줄 높이만큼 아래로 밀어냅니다. (이 방식은 툴바 높이가 보존되어 자식이 잘리지 않습니다)
                val lp = toolbar.layoutParams as? android.view.ViewGroup.MarginLayoutParams
                if (lp != null && lp.topMargin != systemBars.top) {
                    lp.topMargin = systemBars.top
                    toolbar.layoutParams = lp
                }

                // 2. 툴바의 그림자(Elevation)를 없애서 상태 표시줄과 툴바 경계면에 생기는 어두운 그림자 선을 제거합니다.
                toolbar.elevation = 0f

                // 3. 상태 표시줄 영역을 채울 흰색 가짜 뷰(StatusBar Background)를 동적으로 추가하여,
                // 카메라 영역부터 툴바까지 하얀색이 하나의 면으로 자연스럽게 이어지도록 합니다.
                val parent = toolbar.parent as? android.view.ViewGroup
                if (parent != null) {
                    var statusBarBg = parent.findViewWithTag<View>("status_bar_bg")
                    if (statusBarBg == null) {
                        statusBarBg = View(view.context).apply {
                            tag = "status_bar_bg"
                            setBackgroundColor(android.graphics.Color.WHITE)
                        }
                        
                        val bgLp = when (parent) {
                            is androidx.constraintlayout.widget.ConstraintLayout -> {
                                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                                    systemBars.top
                                ).apply {
                                    topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                                    startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                                    endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                                }
                            }
                            is android.widget.RelativeLayout -> {
                                android.widget.RelativeLayout.LayoutParams(
                                    android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                                    systemBars.top
                                ).apply {
                                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP)
                                }
                            }
                            is android.widget.FrameLayout -> {
                                android.widget.FrameLayout.LayoutParams(
                                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                    systemBars.top
                                ).apply {
                                    gravity = android.view.Gravity.TOP
                                }
                            }
                            else -> {
                                android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    systemBars.top
                                )
                            }
                        }
                        parent.addView(statusBarBg, 0, bgLp) // 맨 뒤에 추가
                    } else {
                        val bgLp = statusBarBg.layoutParams
                        if (bgLp.height != systemBars.top) {
                            bgLp.height = systemBars.top
                            statusBarBg.layoutParams = bgLp
                        }
                    }
                }
            }
            insets
        }

        initView()
        observeViewModel()
    }

    /**
     * 하위 상세 페이지에서 이 메서드를 오버라이드하여 각자 고유의 툴바 뷰를 반환합니다.
     * 예: return view.findViewById(R.id.layout_toolbar)
     */
    protected open fun getToolbarView(view: View): View? = null

    /** UI 초기화 (RecyclerView, Click Listener 등) */
    open fun initView() {}

    /** StateFlow / LiveData observe */
    open fun observeViewModel() {}
}
