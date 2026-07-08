package com.example.flowdesk_android.feature.system_management.presentation.website

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.FragmentSystemWebsiteListBinding
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.feature.system_management.domain.model.Website
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog

@AndroidEntryPoint
class WebsiteListFragment : Fragment() {

    private var _binding: FragmentSystemWebsiteListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WebsiteListViewModel by viewModels()
    private lateinit var adapter: WebsiteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemWebsiteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // 이전 상세/추가 화면으로부터의 리프레시 시그널 감지
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")
            ?.observe(viewLifecycleOwner) { refresh ->
                if (refresh == true) {
                    viewModel.refresh()
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = WebsiteAdapter(
            onItemClicked = { website ->
                // 상세 화면으로 이동 (상세/수정 통합 화면)
                val bundle = Bundle().apply {
                    putString("webCode", website.webCode)
                }
                findNavController().navigate(R.id.websiteDetailFragment, bundle)
            },
            onToggleStatusClicked = { website ->
                viewModel.updateWebsiteStatus(website.webCode, !website.isActive)
            },
            onDeleteClicked = { website ->
                showDeleteConfirmDialog(website)
            }
        )
        binding.rvWebsites.adapter = adapter
    }

    private fun setupListeners() {
        // 검색창 리스너
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                viewModel.updateSearchQuery(text)
                binding.btnClearSearch.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 검색어 초기화 버튼
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.updateSearchQuery("")
            binding.btnClearSearch.visibility = View.GONE
        }

        // 추가 버튼 클릭 (생성 화면으로 이동)
        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.websiteCreateFragment)
        }

        // 무한 스크롤 (NestedScrollView 바닥 감지)
        binding.nsvWebsite.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val contentHeight = v.getChildAt(0).measuredHeight
            val scrollHeight = v.measuredHeight
            if (scrollY >= contentHeight - scrollHeight - 100) {
                viewModel.loadNextPage()
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // UI 상태 수집
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is WebsiteListUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                            }
                            is WebsiteListUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                adapter.submitList(state.websites)
                                binding.tvListCount.text = "  ${state.totalCount}건"
                                
                                if (state.websites.isEmpty()) {
                                    binding.llEmpty.visibility = View.VISIBLE
                                    binding.rvWebsites.visibility = View.GONE
                                } else {
                                    binding.llEmpty.visibility = View.GONE
                                    binding.rvWebsites.visibility = View.VISIBLE
                                }
                            }
                            is WebsiteListUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.llEmpty.visibility = View.VISIBLE
                                binding.rvWebsites.visibility = View.GONE
                            }
                        }
                    }
                }

                // 수평 통계 라인 데이터 실시간 반영
                launch {
                    viewModel.totalWebsitesCount.collect { count ->
                        binding.tvStatTotalWebsites.text = count.toString()
                    }
                }
                
                launch {
                    viewModel.activeWebsitesCount.collect { count ->
                        binding.tvStatActiveWebsites.text = count.toString()
                    }
                }
                
                launch {
                    viewModel.inactiveWebsitesCount.collect { count ->
                        binding.tvStatInactiveWebsites.text = count.toString()
                    }
                }

                // 에러 메시지 토스트 반영
                launch {
                    viewModel.errorMessage.collect { message ->
                        showTopToast(message)
                    }
                }
            }
        }
    }



    private fun showDeleteConfirmDialog(website: Website) {
        val dialogBinding = DialogCommonConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = "웹사이트 삭제"
        dialogBinding.tvMessage.text = "'${website.webTitle}' 웹사이트를 삭제하시겠습니까?\n삭제된 웹사이트는 복구할 수 없습니다."
        dialogBinding.cbConfirm.visibility = View.GONE

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            viewModel.deleteWebsite(website.webCode)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        binding.rvWebsites.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
