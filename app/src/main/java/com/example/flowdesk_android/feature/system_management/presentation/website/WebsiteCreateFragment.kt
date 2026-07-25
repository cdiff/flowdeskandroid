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
import com.example.flowdesk_android.databinding.FragmentSystemWebsiteCreateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

@AndroidEntryPoint
class WebsiteCreateFragment : Fragment() {

    private var _binding: FragmentSystemWebsiteCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WebsiteCreateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemWebsiteCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            performCreate()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 생성 권한 반응형 구독 — 권한 변경 시 즉시 버튼 상태 반영
                launch {
                    viewModel.canWrite.collectLatest { canWrite ->
                        binding.btnSave.visibility = if (canWrite) View.VISIBLE else View.GONE
                    }
                }

                // UI 상태 구독
                launch {
                    viewModel.uiState.collectLatest { state ->
                        when (state) {
                            is WebsiteCreateUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.btnSave.isEnabled = false
                            }
                            is WebsiteCreateUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                showTopToast(getString(R.string.website_toast_created))
                                // 이전 화면으로 리프레시 시그널 전달
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                findNavController().popBackStack()
                            }
                            is WebsiteCreateUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnSave.isEnabled = viewModel.canWrite.value
                                showTopToast(state.message)
                            }
                            is WebsiteCreateUiState.Idle -> {
                                binding.progressBar.visibility = View.GONE
                                binding.btnSave.isEnabled = viewModel.canWrite.value
                            }
                        }
                    }
                }
            }
        }
    }

    private fun performCreate() {
        val code = binding.etWebCode.text.toString().trim()
        val title = binding.etWebTitle.text.toString().trim()
        val url = binding.etWebUrl.text.toString().trim()
        val desc = binding.etWebDesc.text.toString().trim()
        val memo = binding.etWebMemo.text.toString().trim()
        val duplicateDaysText = binding.etDuplicateDays.text.toString().trim()
        val isActive = binding.switchActive.isChecked

        if (code.isEmpty() || title.isEmpty() || url.isEmpty()) {
            showTopToast("필수 항목(*)을 입력해 주세요.")
            return
        }

        val duplicateDays = duplicateDaysText.toIntOrNull() ?: 30

        viewModel.createWebsite(
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
