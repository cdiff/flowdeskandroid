package com.example.flowdesk_android.feature.content_management.presentation.board_post

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.example.flowdesk_android.databinding.FragmentContentBoardPostDetailBinding
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.feature.system_management.domain.model.BoardPost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class BoardPostDetailFragment : Fragment() {

    private var _binding: FragmentContentBoardPostDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardPostDetailViewModel by viewModels()
    private var boardId: Long = -1L
    private var postId: Long = -1L

    private var selectedStartDtm: String? = null
    private var selectedEndDtm: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            boardId = it.getLong("boardId", -1L)
            postId = it.getLong("postId", -1L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentBoardPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Window insets 처리 (상태바/카메라 홀 침범 예방)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setupUI()
        setupListeners()
        observeViewModel()

        if (postId != -1L) {
            viewModel.loadPostDetail(boardId, postId)
        }
    }

    private fun setupUI() {
        if (postId == -1L) {
            // 신규 등록 모드
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvHeaderTitle.visibility = View.GONE
            binding.tvDates.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
            binding.btnSave.text = getString(R.string.board_btn_save)

            binding.scrollView.visibility = View.VISIBLE
            binding.layoutButtons.visibility = View.VISIBLE
        } else {
            // 수정 모드
            binding.tvTitle.visibility = View.GONE
            binding.tvHeaderTitle.visibility = View.VISIBLE
            binding.tvHeaderTitle.text = getString(R.string.post_title_edit)
            binding.btnDelete.visibility = View.GONE // 삭제는 상세조회(Read) 화면의 더보기 메뉴에서 처리
            binding.btnSave.text = getString(R.string.board_btn_edit)
 
            // 로딩 전 숨김
            binding.scrollView.visibility = View.INVISIBLE
            binding.layoutButtons.visibility = View.INVISIBLE
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            performSave()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        // 공지 기간 설정 DatePickerDialog 바인딩
        binding.tvStartDtm.setOnClickListener {
            showDatePicker { date ->
                selectedStartDtm = "${date}T00:00:00"
                binding.tvStartDtm.text = date
            }
        }

        binding.tvEndDtm.setOnClickListener {
            showDatePicker { date ->
                selectedEndDtm = "${date}T23:59:59"
                binding.tvEndDtm.text = date
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 생성 모드: canWrite 구독 → 저장 버튼 제어
                if (postId == -1L) {
                    launch {
                        viewModel.canWrite.collectLatest { canWrite ->
                            binding.btnSave.isEnabled = canWrite
                            binding.layoutButtons.visibility = if (canWrite) View.VISIBLE else View.GONE
                        }
                    }
                }

                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is BoardPostDetailUiState.Idle -> {
                                binding.progressBar.visibility = View.GONE
                            }
                            is BoardPostDetailUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.btnSave.isEnabled = false
                                binding.btnDelete.isEnabled = false
                            }
                            is BoardPostDetailUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                // 수정 모드: canUpdate / canDelete 기준으로 버튼 제어
                                binding.btnSave.isEnabled = state.canUpdate
                                binding.btnSave.visibility = if (state.canUpdate) View.VISIBLE else View.GONE
                                binding.btnDelete.isEnabled = state.canDelete
                                binding.btnDelete.visibility = if (state.canDelete) View.VISIBLE else View.GONE
                                binding.layoutButtons.visibility =
                                    if (state.canUpdate || state.canDelete) View.VISIBLE else View.GONE
                                binding.scrollView.visibility = View.VISIBLE
                                bindData(state.post)
                            }
                            is BoardPostDetailUiState.SaveSuccess -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(if (postId == -1L) getString(R.string.post_toast_save_success_create) else getString(R.string.post_toast_save_success_edit))
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is BoardPostDetailUiState.DeleteSuccess -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(getString(R.string.post_toast_delete_success))
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is BoardPostDetailUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                // 에러 후 버튼 복원: 모드에 따라 권한 다시 적용
                                if (postId == -1L) {
                                    binding.btnSave.isEnabled = viewModel.canWrite.value
                                } else {
                                    binding.btnSave.isEnabled = true
                                    binding.btnDelete.isEnabled = true
                                }
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

    private fun bindData(post: BoardPost) {
        binding.etPostTitle.setText(post.title)
        binding.etPostContent.setText(post.content ?: "")
        binding.switchNotice.isChecked = post.isNotice
        
        // 시작일/종료일 날짜 출력
        selectedStartDtm = post.startDtm
        selectedEndDtm = post.endDtm
        binding.tvStartDtm.text = post.startDtm?.substringBefore("T") ?: ""
        binding.tvEndDtm.text = post.endDtm?.substringBefore("T") ?: ""

        val created = post.createdAt?.substringBefore("T") ?: "-"
        val updated = post.updatedAt?.substringBefore("T") ?: "-"
        binding.tvDates.text = getString(R.string.post_label_dates_format, created, updated)
        binding.tvDates.visibility = View.VISIBLE
    }

    private fun performSave() {
        val title = binding.etPostTitle.text.toString().trim()
        val content = binding.etPostContent.text.toString().trim()
        val isNotice = binding.switchNotice.isChecked

        if (title.isEmpty() || content.isEmpty()) {
            showTopToast(getString(R.string.board_toast_required_fields))
            return
        }

        // 기간 미선택(빈값) 시 null로 매핑하여 기간 제한 없도록 설정
        val startDtmToSend = if (selectedStartDtm.isNullOrEmpty()) null else selectedStartDtm
        val endDtmToSend = if (selectedEndDtm.isNullOrEmpty()) null else selectedEndDtm

        viewModel.savePost(
            boardId = boardId,
            postId = postId,
            title = title,
            content = content,
            isNotice = isNotice,
            isActive = true, // default active
            startDtm = startDtmToSend,
            endDtm = endDtmToSend
        )
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val monthStr = String.format("%02d", month + 1)
                val dayStr = String.format("%02d", dayOfMonth)
                onDateSelected("$year-$monthStr-$dayStr")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogCommonConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = getString(R.string.post_dialog_delete_title)
        dialogBinding.tvMessage.text = getString(R.string.post_dialog_delete_message)
        dialogBinding.cbConfirm.visibility = View.GONE

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            viewModel.deletePost(boardId, postId)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
