package com.example.flowdesk_android.feature.super_admin.presentation.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.R

/**
 * 슈퍼 관리자 - 페이지 관리 화면
 * TODO: ViewModel, Adapter 연결 및 API 기능 구현 예정
 */
class SuperAdminPagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_super_admin_pages, container, false)

    companion object {
        fun newInstance() = SuperAdminPagesFragment()
    }
}
