package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.databinding.FragmentBlockKeywordDetailBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockWordItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.example.flowdesk_android.core.base.BaseFragment

@AndroidEntryPoint
class BlockKeywordDetailFragment : BaseFragment(R.layout.fragment_block_keyword_detail) {

    private val viewModel: BlockKeywordViewModel by activityViewModels()

    private var _binding: FragmentBlockKeywordDetailBinding? = null
    private val binding get() = _binding!!

    private var blockWordId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            blockWordId = it.getLong("blockWordId", -1L)
        }
    }

    override fun getToolbarView(view: View): View? = view.findViewById(R.id.layout_toolbar)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentBlockKeywordDetailBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        setupListeners()
        if (blockWordId != -1L) {
            viewModel.loadDetail(blockWordId)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveInfo.setOnClickListener {
            val newReason = binding.etReason.text.toString().trim()
            if (newReason.isEmpty()) {
                Toast.makeText(requireContext(), "차단 사유를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val matchType = getSelectedMatchType()
            updateBlockWord(matchType, newReason)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun getSelectedMatchType(): String {
        return when (binding.rgMatchType.checkedRadioButtonId) {
            R.id.rb_match_exact -> "EXACT"
            R.id.rb_match_regex -> "REGEX"
            else -> "CONTAINS"
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.detailState.collect { state ->
                        when (state) {
                            is BlockWordDetailUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                setInputsEnabled(false)
                            }
                            is BlockWordDetailUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                setInputsEnabled(true)
                                displayDetail(state.item)
                            }
                            is BlockWordDetailUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                setInputsEnabled(false)
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                findNavController().navigateUp()
                            }
                        }
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun displayDetail(item: BlockWordItem) {
        binding.tvDetailKeyword.text = item.blockWord
        binding.tvDetailTenantId.text = "차단 ID: ${item.dbwIdx}"
        binding.tvDetailCreatedBy.text = "등록자 ID: ${item.createdBy}"
        binding.tvDetailCreatedAt.text = "등록: ${item.createdAt ?: "-"}"
        binding.tvDetailUpdatedAt.text = "수정: ${item.updatedAt ?: "-"}"
        binding.etReason.setText(item.reason ?: "")

        when (item.matchType) {
            "EXACT" -> binding.rbMatchExact.isChecked = true
            "REGEX" -> binding.rbMatchRegex.isChecked = true
            else -> binding.rbMatchContains.isChecked = true
        }

        if (item.isActive) {
            binding.tvDetailStatusTag.text = "차단 중"
            binding.tvDetailStatusTag.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        } else {
            binding.tvDetailStatusTag.text = "해제됨"
            binding.tvDetailStatusTag.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        binding.etReason.isEnabled = enabled
        binding.rgMatchType.isEnabled = enabled
        binding.rbMatchContains.isEnabled = enabled
        binding.rbMatchExact.isEnabled = enabled
        binding.rbMatchRegex.isEnabled = enabled
        binding.btnSaveInfo.isEnabled = enabled
        binding.btnDelete.isEnabled = enabled
    }

    private fun updateBlockWord(matchType: String, newReason: String) {
        binding.progressBar.visibility = View.VISIBLE
        setInputsEnabled(false)
        viewModel.updateBlockWord(blockWordId, matchType, newReason, 1) { result ->
            binding.progressBar.visibility = View.GONE
            setInputsEnabled(true)
            result.onSuccess {
                Toast.makeText(requireContext(), "금칙어 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.loadDetail(blockWordId)
            }.onFailure {
                Toast.makeText(requireContext(), "수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogCommonConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = "금칙어 차단 해제"
        dialogBinding.tvMessage.text = "'${binding.tvDetailKeyword.text}' 단어의 차단을 해제하시겠습니까?\n해제하면 즉시 해당 단어 사용이 허용됩니다."
        dialogBinding.cbConfirm.visibility = View.GONE

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            binding.progressBar.visibility = View.VISIBLE
            setInputsEnabled(false)
            viewModel.deleteBlockWord(blockWordId) { result ->
                binding.progressBar.visibility = View.GONE
                setInputsEnabled(true)
                result.onSuccess {
                    Toast.makeText(requireContext(), "금칙어 차단이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure { err ->
                    Toast.makeText(requireContext(), err.message ?: "차단 해제 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
