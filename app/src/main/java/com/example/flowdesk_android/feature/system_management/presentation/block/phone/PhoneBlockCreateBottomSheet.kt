package com.example.flowdesk_android.feature.system_management.presentation.block.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.flowdesk_android.R
import android.graphics.Color
import com.example.flowdesk_android.databinding.DialogPhoneBlockCreateBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneBlockCreateBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogPhoneBlockCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BlockPhoneViewModel by activityViewModels()

    private var currentModeIndex = 0 // 0: 단일 등록, 1: 대량 등록

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPhoneBlockCreateBinding.inflate(inflater, container, false)
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
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = "휴대폰 번호 차단 등록"
        binding.tvHeaderSubtitle.text = "악성 스팸 전송 휴대폰 번호를 차단합니다."
        binding.flModeContainer.visibility = View.VISIBLE
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
            binding.llSinglePhoneContainer.visibility = View.VISIBLE
            binding.llBulkPhoneContainer.visibility = View.GONE
        } else {
            binding.llSinglePhoneContainer.visibility = View.GONE
            binding.llBulkPhoneContainer.visibility = View.VISIBLE
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
            val phoneInput = binding.etBlockPhone.text.toString().trim()
            if (phoneInput.isEmpty()) {
                showError("차단할 휴대폰 번호를 입력해주세요.")
                return
            }

            // 하이픈 제거 및 숫자 유효성 검사
            val cleanPhone = phoneInput.replace(Regex("[^0-9]"), "")
            if (cleanPhone.isEmpty() || cleanPhone.length < 9) {
                showError("유효한 휴대폰 번호를 입력해주세요.")
                return
            }

            setLoading(true)
            viewModel.addBlockPhone(cleanPhone, reason, 1) { result ->
                setLoading(false)
                result.onSuccess {
                    Toast.makeText(requireContext(), "차단 번호가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }.onFailure { err ->
                    showError(err.message ?: "휴대폰 차단 등록 실패")
                }
            }
        } else {
            val phonesInput = binding.etBlockPhones.text.toString().trim()
            if (phonesInput.isEmpty()) {
                showError("차단할 휴대폰 번호 목록을 입력해주세요.")
                return
            }

            // 입력 형식을 줄바꿈 또는 쉼표로 파싱하여 정제
            val lines = phonesInput.split(Regex("[\n,]+"))
            val cleanedSet = LinkedHashSet<String>()
            for (line in lines) {
                val clean = line.replace(Regex("[^0-9]"), "").trim()
                if (clean.isNotEmpty() && clean.length >= 9) {
                    cleanedSet.add(clean)
                }
            }

            if (cleanedSet.isEmpty()) {
                showError("차단할 유효한 번호가 없습니다.")
                return
            }

            // 줄바꿈 단위 문자열로 다시 묶어 서버로 발송
            val phonesPayload = cleanedSet.joinToString("\n")

            setLoading(true)
            viewModel.addBulkBlockPhone(phonesPayload, reason, 1) { result ->
                setLoading(false)
                result.onSuccess { bulkResult ->
                    val msg = "대량 번호 등록 완료 (성공: ${bulkResult.successCount}건, 건너뜀: ${bulkResult.skippedCount}건)"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    dismiss()
                }.onFailure { err ->
                    showError(err.message ?: "대량 휴대폰 차단 등록 실패")
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
        fun newInstance(): PhoneBlockCreateBottomSheet {
            return PhoneBlockCreateBottomSheet()
        }
    }
}
