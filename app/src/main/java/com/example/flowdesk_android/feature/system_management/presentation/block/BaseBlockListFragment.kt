package com.example.flowdesk_android.feature.system_management.presentation.block

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListAdapter
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.FragmentBlockListBinding

abstract class BaseBlockListFragment<T> : Fragment() {

    private var _binding: FragmentBlockListBinding? = null
    protected val binding get() = _binding!!

    protected abstract val adapter: ListAdapter<T, *>

    // 추상 뷰 셋업 속성
    protected abstract val titleText: String
    protected abstract val searchHint: String
    protected abstract val emptyTitleText: String
    protected abstract val emptySubtitleText: String
    protected open val bannerText: String? = null

    // 자식 Fragment가 구현해야 하는 비즈니스 로직 연동 메서드
    protected abstract fun onSearchQueryChanged(query: String)
    protected abstract fun onLoadMore()
    protected abstract fun onAddClicked()
    protected abstract fun setupRecyclerView()
    protected abstract fun observeViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCommonUI()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupCommonUI() {
        binding.tvListTitle.text = titleText
        binding.etSearch.hint = searchHint
        binding.tvEmptyTitle.text = emptyTitleText
        binding.tvEmptySubtitle.text = emptySubtitleText

        // 안내 배너 처리
        bannerText?.let {
            binding.tvBannerText.text = it
            binding.clBanner.visibility = View.VISIBLE
        } ?: run {
            binding.clBanner.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnAdd.setOnClickListener {
            onAddClicked()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onSearchQueryChanged(s?.toString() ?: "")
                binding.btnClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClear.setOnClickListener {
            binding.etSearch.text.clear()
        }

        // NestedScrollView 무한 스크롤 연동
        binding.nsvBlock.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val contentHeight = v.getChildAt(0).measuredHeight
            val scrollHeight = v.measuredHeight
            if (scrollY >= contentHeight - scrollHeight - 100) {
                onLoadMore()
            }
        })
    }

    // 자식 Fragment에서 호출할 공통 UI 상태 제어 메서드들
    protected fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.llEmpty.visibility = View.GONE
    }

    protected fun showSuccess(items: List<T>, totalCount: Int) {
        binding.progressBar.visibility = View.GONE
        adapter.submitList(items)
        binding.tvListCount.text = "  ${totalCount}건"

        if (items.isEmpty()) {
            binding.llEmpty.visibility = View.VISIBLE
            binding.rvList.visibility = View.GONE
        } else {
            binding.llEmpty.visibility = View.GONE
            binding.rvList.visibility = View.VISIBLE
        }
    }

    protected fun updateWritePermission(canWrite: Boolean) {
        binding.btnAdd.visibility = if (canWrite) View.VISIBLE else View.GONE
    }

    protected fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.llEmpty.visibility = View.VISIBLE
        binding.rvList.visibility = View.GONE
        showTopToast(message)
    }

    override fun onDestroyView() {
        binding.rvList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
