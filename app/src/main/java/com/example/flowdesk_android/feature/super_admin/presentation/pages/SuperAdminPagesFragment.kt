package com.example.flowdesk_android.feature.super_admin.presentation.pages

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuperAdminPagesFragment : Fragment() {

    private val viewModel: PagesViewModel by viewModels()

    private lateinit var rvPages: RecyclerView
    private lateinit var progressBar: View
    private lateinit var llEmpty: View
    private lateinit var etSearch: EditText
    private lateinit var btnCreatePage: View
    private lateinit var tvBadgeTotal: TextView
    private lateinit var tvBadgeActive: TextView
    private lateinit var tvBadgeInactive: TextView

    private lateinit var adapter: PageAdapter

    private var bannerDismissed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_super_admin_pages, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupBanner(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun bindViews(view: View) {
        rvPages        = view.findViewById(R.id.rv_pages)
        progressBar    = view.findViewById(R.id.progress_bar)
        llEmpty        = view.findViewById(R.id.ll_empty)
        etSearch       = view.findViewById(R.id.et_search)
        btnCreatePage  = view.findViewById(R.id.btn_create_page)
        tvBadgeTotal   = view.findViewById(R.id.tv_badge_total)
        tvBadgeActive  = view.findViewById(R.id.tv_badge_active)
        tvBadgeInactive = view.findViewById(R.id.tv_badge_inactive)
    }

    private fun setupBanner(view: View) {
        val banner = view.findViewById<View>(R.id.banner_info)
        val btnClose = view.findViewById<View>(R.id.btn_close_banner)

        if (bannerDismissed) {
            banner.visibility = View.GONE
            return
        }

        btnClose.setOnClickListener {
            banner.animate()
                .alpha(0f)
                .translationY(-banner.height.toFloat())
                .setDuration(280)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        banner.visibility = View.GONE
                        banner.alpha = 1f
                        banner.translationY = 0f
                        bannerDismissed = true
                    }
                })
                .start()
        }
    }

    private fun setupRecyclerView() {
        adapter = PageAdapter(
            onToggleStatusClick = { page ->
                val action = if (page.isActive) "비활성화" else "활성화"
                AlertDialog.Builder(requireContext())
                    .setTitle("$action 확인")
                    .setMessage("'${page.displayName}' 페이지를 ${action}하시겠습니까?")
                    .setPositiveButton(action) { _, _ ->
                        viewModel.updatePageStatus(page, !page.isActive)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            },
            onDeleteClick = { page ->
                AlertDialog.Builder(requireContext())
                    .setTitle("페이지 삭제")
                    .setMessage("'${page.displayName}' 페이지를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")
                    .setPositiveButton("삭제") { _, _ ->
                        viewModel.deletePage(page.pageId)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            },
            onToggleParentClick = { page ->
                viewModel.toggleParent(page.pageId)
            },
            expandedParents = viewModel.expandedParents // ViewModel에 노출 필요
        )
        rvPages.adapter = adapter
    }

    private fun setupListeners() {
        // 새 페이지 생성 버튼
        btnCreatePage.setOnClickListener {
            CreatePageBottomSheetFragment { viewModel.fetchPages() }
                .show(childFragmentManager, CreatePageBottomSheetFragment.TAG)
        }

        // 검색
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        progressBar.isVisible = state is PageListUiState.Loading
                        llEmpty.isVisible     = state is PageListUiState.Empty
                        rvPages.isVisible     = state is PageListUiState.Success

                        // 전체 원본 데이터 기준으로 뱃지 업데이트
                        if (state is PageListUiState.Success) {
                            val allPages = state.pages
                            val active   = allPages.count { it.isActive }
                            val inactive = allPages.count { !it.isActive }
                            tvBadgeTotal.text   = "총 ${allPages.size}개"
                            tvBadgeActive.text  = "활성 ${active}개"
                            tvBadgeInactive.text = "비활성 ${inactive}개"
                        }
                    }
                }

                launch {
                    viewModel.filteredPages.collect { pages ->
                        adapter.submitList(pages)
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is PageListEvent.PageCreated -> Toast.makeText(context, "페이지가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                            is PageListEvent.PageDeleted -> Toast.makeText(context, "페이지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            is PageListEvent.PageUpdated -> Toast.makeText(context, "페이지 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            is PageListEvent.Error -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = SuperAdminPagesFragment()
    }
}
