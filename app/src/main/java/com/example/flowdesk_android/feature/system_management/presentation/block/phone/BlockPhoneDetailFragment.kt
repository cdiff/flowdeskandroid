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
import com.example.flowdesk_android.core.extension.showTopToast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockPhoneItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.example.flowdesk_android.core.base.BaseFragment

@AndroidEntryPoint
class BlockPhoneDetailFragment : BaseFragment(R.layout.fragment_block_phone_detail) {

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

    override fun getToolbarView(view: View): View = binding.layoutToolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentBlockPhoneDetailBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        setupListeners()
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
                showTopToast(getString(R.string.block_msg_enter_reason))
                return@setOnClickListener
            }
            updateReason(newReason)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    override fun observeViewModel() {
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
                                applyPermissions(state.canUpdate, state.canDelete)
                            }
                            is BlockPhoneDetailUiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                setInputsEnabled(false)
                                showTopToast(state.message)
                                findNavController().navigateUp()
                            }
                        }
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest { msg ->
                        showTopToast(msg)
                    }
                }
            }
        }
    }

    private fun applyPermissions(canUpdate: Boolean, canDelete: Boolean) {
        binding.etReason.isEnabled = canUpdate
        val buttonsContainer = binding.btnSaveInfo.parent as? android.widget.LinearLayout
        if (buttonsContainer != null) {
            if (!canUpdate && !canDelete) {
                buttonsContainer.visibility = View.GONE
            } else {
                buttonsContainer.visibility = View.VISIBLE
                binding.btnDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
                binding.btnSaveInfo.visibility = if (canUpdate) View.VISIBLE else View.GONE
                
                if (canUpdate && !canDelete) {
                    val params = binding.btnSaveInfo.layoutParams as android.widget.LinearLayout.LayoutParams
                    params.weight = 3f
                    binding.btnSaveInfo.layoutParams = params
                } else if (!canUpdate && canDelete) {
                    val params = binding.btnDelete.layoutParams as android.widget.LinearLayout.LayoutParams
                    params.weight = 3f
                    binding.btnDelete.layoutParams = params
                } else {
                    val deleteParams = binding.btnDelete.layoutParams as android.widget.LinearLayout.LayoutParams
                    deleteParams.weight = 1f
                    binding.btnDelete.layoutParams = deleteParams
                    
                    val saveParams = binding.btnSaveInfo.layoutParams as android.widget.LinearLayout.LayoutParams
                    saveParams.weight = 2f
                    binding.btnSaveInfo.layoutParams = saveParams
                }
            }
        }
    }

    private fun displayDetail(item: BlockPhoneItem) {
        binding.tvDetailPhoneNumber.text = formatPhoneNumber(item.blockHp)
        binding.tvDetailTenantId.text = "차단 ID: ${item.dbhIdx}"
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
                showTopToast(getString(R.string.block_phone_msg_updated))
                viewModel.loadDetail(blockHpId)
            }.onFailure {
                showTopToast(getString(R.string.block_msg_reason_update_failed))
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
                    showTopToast(getString(R.string.block_phone_msg_deleted))
                    findNavController().popBackStack()
                }.onFailure { err ->
                    showTopToast(err.message ?: getString(R.string.block_msg_reason_update_failed))
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
