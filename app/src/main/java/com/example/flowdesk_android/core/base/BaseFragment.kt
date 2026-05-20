package com.example.flowdesk_android.core.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()
    }

    /** UI 초기화 (RecyclerView, Click Listener 등) */
    open fun initView() {}

    /** StateFlow / LiveData observe */
    open fun observeViewModel() {}
}
