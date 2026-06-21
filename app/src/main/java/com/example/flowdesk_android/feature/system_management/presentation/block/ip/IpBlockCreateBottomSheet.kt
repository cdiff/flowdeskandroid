package com.example.flowdesk_android.feature.system_management.presentation.block.ip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.flowdesk_android.R
import android.graphics.Color
import com.example.flowdesk_android.databinding.DialogIpBlockCreateBinding
import com.example.flowdesk_android.feature.system_management.domain.model.BlockIpItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IpBlockCreateBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogIpBlockCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BlockIpViewModel by activityViewModels()

    private var currentModeIndex = 0 // 0: 단일 등록, 1: 대량 등록

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIpBlockCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)

        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isShouldRemoveExpandedCorners = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupListeners()

        val defaultIp = arguments?.getString("ip")
        if (!defaultIp.isNullOrEmpty()) {
            binding.etBlockIp.setText(defaultIp)
        }
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = "IP 차단 등록"
        binding.tvHeaderSubtitle.text = "허가되지 않은 악성 트래픽 IP를 차단합니다."
        val hideMode = arguments?.getBoolean("hideModeSelector", false) ?: false
        binding.flModeContainer.visibility = if (hideMode) View.GONE else View.VISIBLE
        binding.btnCreate.text = "등록하기"
        selectMode(0, animate = false)
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnModeSingle.setOnClickListener { selectMode(0) }
        binding.btnModeBulk.setOnClickListener { selectMode(1) }

        binding.btnModeSingle.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                val btnWidth = binding.btnModeSingle.width
                if (btnWidth > 0) {
                    binding.btnModeSingle.removeOnLayoutChangeListener(this)
                    val lp = binding.vSegmentSelector.layoutParams
                    lp.width = btnWidth
                    binding.vSegmentSelector.layoutParams = lp
                    binding.vSegmentSelector.translationX = if (currentModeIndex == 0) {
                        binding.btnModeSingle.left.toFloat()
                    } else {
                        binding.btnModeBulk.left.toFloat()
                    }
                }
            }
        })

        binding.btnCreate.setOnClickListener {
            performCreate()
        }
    }

    private fun selectMode(index: Int, animate: Boolean = true) {
        currentModeIndex = index
        
        val targetX = if (index == 0) {
            binding.btnModeSingle.left.toFloat()
        } else {
            binding.btnModeBulk.left.toFloat()
        }
        
        if (animate) {
            binding.vSegmentSelector.animate()
                .translationX(targetX)
                .setDuration(250)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        } else {
            binding.vSegmentSelector.translationX = targetX
        }
        
        val activeColor = Color.parseColor("#3B82F6")
        val inactiveColor = Color.parseColor("#64748B")
        
        binding.btnModeSingle.setTextColor(if (index == 0) activeColor else inactiveColor)
        binding.btnModeBulk.setTextColor(if (index == 1) activeColor else inactiveColor)
        
        if (index == 0) {
            binding.llSingleIpContainer.visibility = View.VISIBLE
            binding.llBulkIpContainer.visibility = View.GONE
        } else {
            binding.llSingleIpContainer.visibility = View.GONE
            binding.llBulkIpContainer.visibility = View.VISIBLE
        }
    }

    private fun performCreate() {
        val isSingle = currentModeIndex == 0
        val reason = binding.etReason.text.toString().trim()
        
        if (reason.isEmpty()) {
            showError("차단 사유를 입력해주세요.")
            return
        }

        if (isSingle) {
            val ip = binding.etBlockIp.text.toString().trim()
            if (ip.isEmpty()) {
                showError("차단할 IP 주소를 입력해주세요.")
                return
            }

            setLoading(true)
            viewModel.addBlockIp(ip, reason, 1) { result ->
                setLoading(false)
                result.onSuccess {
                    Toast.makeText(requireContext(), "차단 IP가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }.onFailure { err ->
                    showError(err.message ?: "IP 차단 등록 실패")
                }
            }
        } else {
            val ips = binding.etBlockIps.text.toString().trim()
            if (ips.isEmpty()) {
                showError("차단할 IP 주소 목록을 입력해주세요.")
                return
            }

            setLoading(true)
            viewModel.addBulkBlockIp(ips, reason, 1) { result ->
                setLoading(false)
                result.onSuccess { bulkResult ->
                    val msg = "대량 IP 등록 완료 (성공: ${bulkResult.successCount}건, 건너뜀: ${bulkResult.skippedCount}건)"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    dismiss()
                }.onFailure { err ->
                    showError(err.message ?: "대량 IP 차단 등록 실패")
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreate.isEnabled = !isLoading
        binding.tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(ip: String? = null, hideModeSelector: Boolean = false): IpBlockCreateBottomSheet {
            val fragment = IpBlockCreateBottomSheet()
            val args = Bundle().apply {
                putString("ip", ip)
                putBoolean("hideModeSelector", hideModeSelector)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
