package com.example.flowdesk_android.core.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToolbarView(view)?.let { toolbar ->
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
                val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
                insets
            }
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
