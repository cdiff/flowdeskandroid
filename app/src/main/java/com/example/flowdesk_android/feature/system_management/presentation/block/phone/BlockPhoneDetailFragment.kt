package com.example.flowdesk_android.feature.system_management.presentation.block.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogCommonConfirmBinding
import com.example.flowdesk_android.databinding.FragmentBlockPhoneDetailBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockPhoneItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockPhoneDetailFragment : Fragment() {

    private val viewModel: BlockPhoneViewModel by activityViewModels()

    private var _binding: FragmentBlockPhoneDetailBinding? = null
    private val binding get() = _binding!!

    private var blockHpId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            blockHpId = it.getLong("blockHpId", -1L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockPhoneDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()

        if (blockHpId != -1L) {
            viewModel.loadDetail(blockHpId)
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
            updateReason(newReason)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.detailState.collect { state ->
                        when (state) {
                            is BlockPhoneDetailUiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                setInputsEnabled(false)
                            }
                            is BlockPhoneDetailUiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                setInputsEnabled(true)
                                displayDetail(state.item)
                            }
                            is BlockPhoneDetailUiState.Error -> {
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

    private fun displayDetail(item: BlockPhoneItem) {
        binding.tvDetailPhoneNumber.text = formatPhoneNumber(item.blockHp)
        binding.tvDetailTenantId.text = "테넌트 ID: ${item.tenantId}"
        binding.tvDetailCreatedBy.text = "등록자 ID: ${item.createdBy}"
        binding.tvDetailCreatedAt.text = "등록: ${item.createdAt ?: "-"}"
        binding.tvDetailUpdatedAt.text = "수정: ${item.updatedAt ?: "-"}"
        binding.etReason.setText(item.reason ?: "")

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
        binding.btnSaveInfo.isEnabled = enabled
        binding.btnDelete.isEnabled = enabled
    }

    private fun updateReason(newReason: String) {
        binding.progressBar.visibility = View.VISIBLE
        setInputsEnabled(false)
        viewModel.updateBlockPhone(blockHpId, newReason, 1) { result ->
            binding.progressBar.visibility = View.GONE
            setInputsEnabled(true)
            result.onSuccess {
                Toast.makeText(requireContext(), "차단 사유가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.loadDetail(blockHpId)
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

        dialogBinding.tvTitle.text = "휴대폰 차단 해제"
        dialogBinding.tvMessage.text = "${binding.tvDetailPhoneNumber.text} 번호의 차단을 해제하시겠습니까?\n해제하면 즉시 수신거부가 해제됩니다."
        dialogBinding.cbConfirm.visibility = View.GONE

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            binding.progressBar.visibility = View.VISIBLE
            setInputsEnabled(false)
            viewModel.deleteBlockPhone(blockHpId) { result ->
                binding.progressBar.visibility = View.GONE
                setInputsEnabled(true)
                result.onSuccess {
                    Toast.makeText(requireContext(), "휴대폰 차단이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure { err ->
                    Toast.makeText(requireContext(), err.message ?: "차단 해제 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun formatPhoneNumber(number: String): String {
        val cleanNumber = number.replace(Regex("[^0-9]"), "")
        return when {
            cleanNumber.length == 11 -> {
                "${cleanNumber.substring(0, 3)}-${cleanNumber.substring(3, 7)}-${cleanNumber.substring(7)}"
            }
            cleanNumber.length == 10 -> {
                if (cleanNumber.startsWith("02")) {
                    "${cleanNumber.substring(0, 2)}-${cleanNumber.substring(2, 6)}-${cleanNumber.substring(6)}"
                } else {
                    "${cleanNumber.substring(0, 3)}-${cleanNumber.substring(3, 6)}-${cleanNumber.substring(6)}"
                }
            }
            cleanNumber.length == 9 && cleanNumber.startsWith("02") -> {
                "${cleanNumber.substring(0, 2)}-${cleanNumber.substring(2, 5)}-${cleanNumber.substring(5)}"
            }
            else -> number
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
