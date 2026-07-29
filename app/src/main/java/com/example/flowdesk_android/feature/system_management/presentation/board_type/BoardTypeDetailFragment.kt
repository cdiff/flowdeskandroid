package com.example.flowdesk_android.feature.system_management.presentation.board_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentSystemBoardTypeDetailBinding
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.feature.system_management.domain.model.BoardType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BoardTypeDetailFragment : Fragment() {

    private var _binding: FragmentSystemBoardTypeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardTypeDetailViewModel by viewModels()
    private var boardId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            boardId = it.getLong("boardId", -1L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBoardTypeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        observeViewModel()

        if (boardId != -1L) {
            viewModel.loadBoardTypeDetail(boardId)
        }
    }

    private fun setupUI() {
        if (boardId == -1L) {
            // 생성 모드
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvDates.visibility = View.GONE
            binding.etBoardKey.isEnabled = true
            binding.btnSave.text = getString(R.string.board_btn_save)

            // 입력 폼 바로 표시 (로딩 없음)
            binding.scrollView.visibility = View.VISIBLE
            binding.layoutButtons.visibility = View.VISIBLE
        } else {
            // 상세/수정 모드
            binding.tvTitle.visibility = View.GONE
            binding.etBoardKey.isEnabled = false // 키 수정 불가
            binding.btnSave.text = getString(R.string.board_btn_edit)

            // 로딩 전에는 숨김
            binding.scrollView.visibility = View.INVISIBLE
            binding.layoutButtons.visibility = View.INVISIBLE
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            performSave()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // ─── 생성 모드: canWrite 구독 ────────────────────────────────────
                if (boardId == -1L) {
                    launch {
                        viewModel.canWrite.collectLatest { canWrite ->
                            binding.btnSave.isEnabled = canWrite
                            binding.layoutButtons.visibility = if (canWrite) View.VISIBLE else View.GONE
                        }
                    }
                }

                // ─── 공통 uiState 구독 ────────────────────────────────────────
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BoardTypeDetailUiState.Idle -> {
                                binding.progressBar.visibility = View.GONE
                            }
                            is BoardTypeDetailUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.btnSave.isEnabled = false
                            }
                            is BoardTypeDetailUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                // 수정 모드 → canUpdate 기준으로 버튼/폼 제어
                                binding.btnSave.isEnabled = state.canUpdate
                                binding.scrollView.visibility = View.VISIBLE
                                binding.layoutButtons.visibility = if (state.canUpdate) View.VISIBLE else View.GONE
                                bindData(state.boardType, state.canUpdate)
                            }
                            is BoardTypeDetailUiState.SaveSuccess -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(
                                    if (boardId == -1L) getString(R.string.board_toast_save_success_create)
                                    else getString(R.string.board_toast_save_success_edit)
                                )
                                // 목록 화면 리프레시 시그널 전달 후 이전 백스택으로 탈출
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is BoardTypeDetailUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                // 에러 후 버튼 복원: 모드에 따라 권한 다시 적용
                                val hasPermission = if (boardId == -1L) viewModel.canWrite.value
                                                   else (state as? BoardTypeDetailUiState.Error)?.let { false } ?: true
                                binding.btnSave.isEnabled = if (boardId == -1L) viewModel.canWrite.value else true
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

    private fun bindData(boardType: BoardType, canUpdate: Boolean) {
        binding.etBoardName.setText(boardType.name)
        binding.etBoardKey.setText(boardType.boardKey)
        binding.etBoardDesc.setText(boardType.description ?: "")
        binding.etSortOrder.setText(boardType.sortOrder.toString())
        binding.switchActive.isChecked = boardType.isActive

        // 권한에 따른 편집 가능 여부 제어
        binding.etBoardName.isEnabled = canUpdate
        binding.etBoardDesc.isEnabled = canUpdate
        binding.etSortOrder.isEnabled = canUpdate
        binding.switchActive.isEnabled = canUpdate

        val created = boardType.createdAt?.substringBefore("T") ?: "-"
        val updated = boardType.updatedAt?.substringBefore("T") ?: "-"
        binding.tvDates.text = getString(R.string.counsel_label_reg_date_prefix, created) + "   /   수정일: " + updated
        binding.tvDates.visibility = View.VISIBLE
    }

    private fun performSave() {
        val name = binding.etBoardName.text.toString().trim()
        val key = binding.etBoardKey.text.toString().trim()
        val desc = binding.etBoardDesc.text.toString().trim()
        val sortOrderText = binding.etSortOrder.text.toString().trim()
        val isActive = binding.switchActive.isChecked

        if (name.isEmpty() || key.isEmpty() || sortOrderText.isEmpty()) {
            showTopToast(getString(R.string.board_toast_required_fields))
            return
        }

        val sortOrder = sortOrderText.toIntOrNull() ?: 1

        viewModel.saveBoardType(
            boardId = boardId,
            boardKey = key,
            name = name,
            description = desc.ifEmpty { null },
            sortOrder = sortOrder,
            isActive = isActive
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
