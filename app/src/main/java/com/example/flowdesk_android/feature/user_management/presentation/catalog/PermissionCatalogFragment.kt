package com.example.flowdesk_android.feature.user_management.presentation.catalog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.databinding.FragmentUserManagementRolePermissionCatalogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PermissionCatalogFragment : Fragment() {

    private var _binding: FragmentUserManagementRolePermissionCatalogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PermissionCatalogViewModel by viewModels()

    private lateinit var adapter: PermissionCatalogAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementRolePermissionCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }



    private fun setupRecyclerView() {
        adapter = PermissionCatalogAdapter()
        binding.rvPermissions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PermissionCatalogFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearch(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is PermissionCatalogUiState.Loading -> {
                                // 로딩 처리 (필요시)
                            }
                            is PermissionCatalogUiState.Success -> {
                                // 초기 로드 완료 - 별도 처리 없음 (filteredPages가 리스트 업데이트)
                            }
                            is PermissionCatalogUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.filteredPages.collect { pages ->
                        // 필터링된 리스트 적용
                        adapter.submitList(pages)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PermissionCatalogFragment()
    }
}
