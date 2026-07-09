package com.example.flowdesk_android.feature.system_management.presentation.board_type

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentSystemBoardTypeListBinding
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BoardTypeListFragment : Fragment() {

    private var _binding: FragmentSystemBoardTypeListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardTypeListViewModel by viewModels()
    private lateinit var adapter: BoardTypeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBoardTypeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // 상세/추가 화면 복귀 시 목록 새로고침 시그널 감지
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")
            ?.observe(viewLifecycleOwner) { refresh ->
                if (refresh == true) {
                    viewModel.triggerRefresh()
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = BoardTypeAdapter(
            onItemClicked = { item ->
                val bundle = Bundle().apply {
                    putLong("boardId", item.boardId)
                }
                findNavController().navigate(R.id.boardTypeDetailFragment, bundle)
            },
            onToggleStatusClicked = { item ->
                viewModel.toggleBoardTypeActive(item.boardId, item.isActive)
            },
            onDeleteClicked = { item ->
                showDeleteConfirmDialog(item)
            }
        )
        binding.rvBoardTypes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBoardTypes.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAdd.setOnClickListener {
            // 추가 모드로 상세 프래그먼트 이동 (boardId = -1L)
            val bundle = Bundle().apply {
                putLong("boardId", -1L)
            }
            findNavController().navigate(R.id.boardTypeDetailFragment, bundle)
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                viewModel.updateSearchQuery(text)
                binding.btnClearSearch.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BoardTypeListUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                            }
                            is BoardTypeListUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnAdd.visibility = if (state.canWrite) View.VISIBLE else View.GONE
                                adapter.setPermissions(state.canUpdate, state.canDelete)
                                adapter.submitList(state.items)

                                // 통계 데이터 바인딩
                                binding.tvStatTotal.text = state.totalCount.toString()
                                binding.tvStatActive.text = state.activeCount.toString()
                                binding.tvStatInactive.text = state.inactiveCount.toString()
                                binding.tvListCount.text = "  ${state.totalCount}건"

                                if (state.items.isEmpty()) {
                                    binding.llEmpty.visibility = View.VISIBLE
                                    binding.rvBoardTypes.visibility = View.GONE
                                } else {
                                    binding.llEmpty.visibility = View.GONE
                                    binding.rvBoardTypes.visibility = View.VISIBLE
                                }
                            }
                            is BoardTypeListUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(state.message)
                            }
                        }
                    }
                }

                launch {
                    viewModel.toastMessage.collectLatest { msg ->
                        showTopToast(msg)
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmDialog(item: BoardType) {
        val dialogBinding = DialogCommonConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = getString(R.string.board_dialog_delete_title)
        dialogBinding.tvMessage.text = getString(R.string.board_dialog_delete_message, item.name)
        dialogBinding.cbConfirm.visibility = View.GONE

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            viewModel.deleteBoardType(item.boardId)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        binding.rvBoardTypes.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
