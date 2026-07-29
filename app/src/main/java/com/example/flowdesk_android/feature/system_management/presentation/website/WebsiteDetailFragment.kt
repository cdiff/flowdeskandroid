package com.example.flowdesk_android.feature.system_management.presentation.website

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.core.extension.toFormattedDateString
import com.example.flowdesk_android.databinding.FragmentSystemWebsiteDetailBinding
import com.example.flowdesk_android.feature.system_management.domain.model.Website
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

@AndroidEntryPoint
class WebsiteDetailFragment : Fragment() {

    private var _binding: FragmentSystemWebsiteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WebsiteDetailViewModel by viewModels()
    private var webCode: String? = null
    
    // 상세 조회 모드 vs 수정 모드 플래그
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            webCode = it.getString("webCode")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemWebsiteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
        
        webCode?.let {
            viewModel.loadWebsiteDetail(it)
        } ?: run {
            showTopToast("웹사이트 코드가 올바르지 않습니다.")
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val code = webCode ?: return@setOnClickListener
            // 바로 수정 처리 수행
            performUpdate(code)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // UI 상태 수집
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is WebsiteDetailUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.scrollView.visibility = View.INVISIBLE
                                binding.layoutButtons.visibility = View.INVISIBLE
                                binding.btnSave.isEnabled = false
                            }
                            is WebsiteDetailUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.scrollView.visibility = View.VISIBLE
                                binding.layoutButtons.visibility = if (state.canUpdate) View.VISIBLE else View.GONE
                                binding.btnSave.isEnabled = state.canUpdate
                                bindWebsiteData(state.website, state.canUpdate)
                            }
                            is WebsiteDetailUiState.UpdateSuccess -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(getString(R.string.website_toast_updated))
                                // 이전 화면으로 리프레시 시그널 전달
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack() // 수정 성공 시 목록으로 복귀
                            }
                            is WebsiteDetailUiState.DeleteSuccess -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(getString(R.string.website_toast_deleted))
                                findNavController().popBackStack() // 삭제 후 이전 화면 탈출
                            }
                            is WebsiteDetailUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnSave.isEnabled = true
                                showTopToast(state.message)
                            }
                            is WebsiteDetailUiState.Idle -> {
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    }
                }

                // 토스트 메세지 수집
                launch {
                    viewModel.toastMessage.collect { message ->
                        showTopToast(message)
                    }
                }
            }
        }
    }

    private fun bindWebsiteData(website: Website, canUpdate: Boolean) {
        binding.etWebCode.setText(website.webCode)
        binding.etWebTitle.setText(website.webTitle)
        binding.etWebUrl.setText(website.webUrl)
        binding.etWebDesc.setText(website.webDesc ?: "")
        binding.etWebMemo.setText(website.webMemo ?: "")
        binding.etDuplicateDays.setText(website.duplicateAllowAfterDays.toString())
        binding.switchActive.isChecked = website.isActive
        
        // 날짜 텍스트 셋팅
        val created = website.createdAt?.toFormattedDateString() ?: "-"
        val updated = website.updatedAt?.toFormattedDateString() ?: "-"
        binding.tvDates.text = "등록일: $created   /   수정일: $updated"
        
        // 데이터 바인딩 직후 항상 편집 모드로 설정
        setEditMode(true, canUpdate)
    }

    private fun setEditMode(enabled: Boolean, canUpdate: Boolean) {
        isEditMode = true
        
        // 에디트 텍스트 항상 편집 가능 상태 설정 (코드 제외)
        binding.etWebTitle.isEnabled = canUpdate
        binding.etWebUrl.isEnabled = canUpdate
        binding.etWebDesc.isEnabled = canUpdate
        binding.etWebMemo.isEnabled = canUpdate
        binding.etDuplicateDays.isEnabled = canUpdate
        binding.switchActive.isEnabled = canUpdate
        
        binding.btnSave.text = "저장하기"
    }

    private fun performUpdate(code: String) {
        val title = binding.etWebTitle.text.toString().trim()
        val url = binding.etWebUrl.text.toString().trim()
        val desc = binding.etWebDesc.text.toString().trim()
        val memo = binding.etWebMemo.text.toString().trim()
        val duplicateDaysText = binding.etDuplicateDays.text.toString().trim()
        val isActive = binding.switchActive.isChecked

        if (title.isEmpty() || url.isEmpty()) {
            showTopToast("필수 항목(*)을 입력해 주세요.")
            return
        }

        val duplicateDays = duplicateDaysText.toIntOrNull() ?: 30

        // 로그인 유저의 seq는 임시로 1 혹은 기존 값으로 유지
        viewModel.updateWebsite(
            webCode = code,
            userSeq = 1,
            webUrl = url,
            webTitle = title,
            webImg = null,
            webDesc = desc.ifEmpty { null },
            webMemo = memo.ifEmpty { null },
            isActive = isActive,
            duplicateAllowAfterDays = duplicateDays
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
