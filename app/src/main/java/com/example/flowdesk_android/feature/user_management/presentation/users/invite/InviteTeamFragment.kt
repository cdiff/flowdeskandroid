package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentUserManagementUserInviteBinding
import com.example.flowdesk_android.databinding.ItemSignupCompletedFieldBinding
import androidx.navigation.fragment.FragmentNavigatorExtras
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet

@AndroidEntryPoint
class InviteTeamFragment : Fragment(R.layout.fragment_user_management_user_invite) {

    private var _binding: FragmentUserManagementUserInviteBinding? = null
    private val binding get() = _binding!!

    // Activity 범위 ViewModel - 이후 Password/Role Fragment와 데이터 공유
    private val viewModel: InviteTeamViewModel by activityViewModels()

    private var isFirstLaunch = true

    private val phoneTextWatcher = object : android.text.TextWatcher {
        private var isFormatting = false
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            if (isFormatting) return
            val step = viewModel.currentStep.value
            if (step != InviteStep.TEL && step != InviteStep.HP) return
            isFormatting = true
            val clean = s.toString().replace(Regex("[-\\s]"), "")
            val formatted = formatPhone(clean)
            binding.etCurrentInput.setText(formatted)
            binding.etCurrentInput.setSelection(formatted.length)
            isFormatting = false
        }
    }

    private fun formatPhone(clean: String): String {
        return when {
            clean.length <= 3 -> clean
            clean.length <= 7 -> "${clean.substring(0, 3)}-${clean.substring(3)}"
            clean.length <= 11 -> "${clean.substring(0, 3)}-${clean.substring(3, 7)}-${clean.substring(7)}"
            else -> "${clean.substring(0, 3)}-${clean.substring(3, 7)}-${clean.substring(7, 11)}"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserManagementUserInviteBinding.bind(view)

        // Window insets 처리 (카메라 영역 침범 방지)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        // 완전히 처음 진입한 최초 1회만 첫 단계 데이터 리셋 수행
        if (isFirstLaunch) {
            viewModel.resetSteps()
            isFirstLaunch = false
        }

        setupEmailChips()
        setupListeners()
        observeViewModel()
    }

    private fun setupEmailChips() {
        val clickListener = View.OnClickListener { v ->
            val domain = when (v.id) {
                R.id.chipNaver -> "naver.com"
                R.id.chipGmail -> "gmail.com"
                R.id.chipDaum -> "daum.net"
                R.id.chipHanmail -> "hanmail.net"
                R.id.chipNate -> "nate.com"
                else -> ""
            }
            if (domain.isNotEmpty()) {
                val currentText = binding.etCurrentInput.text.toString().trim()
                val id = if (currentText.contains("@")) currentText.substringBefore("@") else currentText
                val completedEmail = "$id@$domain"
                binding.etCurrentInput.setText(completedEmail)
                binding.etCurrentInput.setSelection(completedEmail.length)
                handleNext()
            }
        }
        binding.chipNaver.setOnClickListener(clickListener)
        binding.chipGmail.setOnClickListener(clickListener)
        binding.chipDaum.setOnClickListener(clickListener)
        binding.chipHanmail.setOnClickListener(clickListener)
        binding.chipNate.setOnClickListener(clickListener)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            if (!viewModel.previousStep()) {
                findNavController().popBackStack()
            }
        }

        binding.etCurrentInput.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutNormalInputBox.isSelected = hasFocus
        }

        binding.etCurrentInput.addTextChangedListener(phoneTextWatcher)

        binding.etCurrentInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                handleNext()
                true
            } else false
        }

        binding.btnNext.setOnClickListener { handleNext() }

        binding.btnSkip.setOnClickListener {
            // 선택 단계 건너뛰기: 빈 값으로 다음 단계 진행
            val hasMore = viewModel.nextStep("")
            if (!hasMore) {
                val extras = FragmentNavigatorExtras(binding.layoutProgressIndicators to "progress_dots")
                findNavController().navigate(
                    R.id.action_inviteTeamFragment_to_invitePasswordFragment,
                    null,
                    null,
                    extras
                )
            }
        }
    }

    private fun handleNext() {
        val currentStep = viewModel.currentStep.value
        val inputValue = binding.etCurrentInput.text.toString().trim()

        if (!validateInput(currentStep, inputValue)) return

        val hasMore = viewModel.nextStep(inputValue)
        if (!hasMore) {
            val extras = FragmentNavigatorExtras(binding.layoutProgressIndicators to "progress_dots")
            findNavController().navigate(
                R.id.action_inviteTeamFragment_to_invitePasswordFragment,
                null,
                null,
                extras
            )
        }
    }

    private fun validateInput(step: InviteStep, value: String): Boolean {
        binding.tvInputError.visibility = View.GONE

        // 선택 항목은 빈 값 허용
        if (step.isOptional && value.isEmpty()) return true

        val errorMsg = checkSingleInput(step, value)
        if (errorMsg != null) {
            showError(errorMsg)
            return false
        }
        return true
    }

    private fun checkSingleInput(step: InviteStep, value: String): String? {
        if (value.isEmpty()) return "${step.label}을(를) 입력해주세요."

        if (step == InviteStep.EMAIL) {
            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (!emailPattern.matches(value)) return "올바른 이메일 형식이 아닙니다."
        }

        return null
    }

    private fun showError(message: String) {
        binding.tvInputError.text = message
        binding.tvInputError.visibility = View.VISIBLE
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentStep.collect { step -> applyStep(step) }
                }
                launch {
                    viewModel.completedItems.collect { items -> renderCompletedItems(items) }
                }
            }
        }
    }

    private fun getCustomTransition(): TransitionSet {
        return TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(Fade(Fade.IN).setDuration(220))
            addTransition(Fade(Fade.OUT).setDuration(180))
            addTransition(ChangeBounds().setDuration(300))
            interpolator = FastOutSlowInInterpolator()
        }
    }

    private fun applyStep(step: InviteStep) {
        TransitionManager.beginDelayedTransition(binding.scrollView, getCustomTransition())

        binding.tvStepTitle.text = step.titleText
        binding.tvInputLabel.text = step.label

        // 이메일 단계에서만 도메인 칩 표시
        binding.scrollEmailChips.visibility =
            if (step == InviteStep.EMAIL) View.VISIBLE else View.GONE

        // 선택 단계에서 건너뛰기 버튼 표시
        binding.btnSkip.visibility =
            if (step.isOptional) View.VISIBLE else View.GONE

        // 기존 저장 값 복원
        val existingValue = when (step) {
            InviteStep.USER_NAME -> viewModel.userName
            InviteStep.EMAIL -> viewModel.userEmail
            InviteStep.TEL -> viewModel.userTel
            InviteStep.HP -> viewModel.userHp
        }
        binding.etCurrentInput.setText(existingValue)
        binding.etCurrentInput.setSelection(existingValue.length)
        binding.etCurrentInput.hint = step.hint

        // 입력 타입 설정
        binding.etCurrentInput.inputType = when (step) {
            InviteStep.TEL, InviteStep.HP -> InputType.TYPE_CLASS_PHONE
            InviteStep.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT
            else -> InputType.TYPE_CLASS_TEXT
        }

        binding.etCurrentInput.requestFocus()

        // 에러/설명 초기화
        binding.tvInputError.visibility = View.GONE
        if (step.descText != null) {
            binding.tvInputDesc.text = step.descText
            binding.tvInputDesc.visibility = View.VISIBLE
        } else {
            binding.tvInputDesc.visibility = View.GONE
        }

        binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
    }

    private fun renderCompletedItems(items: List<Pair<String, String>>) {
        TransitionManager.beginDelayedTransition(binding.scrollView, getCustomTransition())
        binding.containerCompletedItems.removeAllViews()
        val steps = InviteStep.entries

        items.forEach { (label, value) ->
            val itemBinding = ItemSignupCompletedFieldBinding.inflate(
                layoutInflater, binding.containerCompletedItems, true
            )
            val targetStep = steps.find { it.label == label } ?: InviteStep.USER_NAME
            itemBinding.tvCompletedLabel.text = label

            // 이메일은 아이디/도메인 분리 표시
            if (targetStep == InviteStep.EMAIL && value.contains("@")) {
                itemBinding.tvCompletedValue.visibility = View.GONE
                itemBinding.layoutEmailCompletedValue.visibility = View.VISIBLE
                val parts = value.split("@")
                itemBinding.tvCompletedEmailId.text = parts[0]
                itemBinding.tvCompletedEmailDomain.text = "@${parts[1]}"
            } else {
                itemBinding.layoutEmailCompletedValue.visibility = View.GONE
                itemBinding.tvCompletedValue.visibility = View.VISIBLE
                itemBinding.tvCompletedValue.text = value
            }

            // 완료 카드 클릭 → 인라인 편집 모드
            itemBinding.root.setOnClickListener {
                if (itemBinding.layoutEditMode.visibility == View.GONE) {
                    itemBinding.layoutReadMode.visibility = View.GONE
                    itemBinding.layoutEditMode.visibility = View.VISIBLE
                    itemBinding.tvEditLabel.text = label
                    itemBinding.etEditValue.setText(value)

                    if (targetStep == InviteStep.TEL || targetStep == InviteStep.HP) {
                        itemBinding.etEditValue.inputType = InputType.TYPE_CLASS_PHONE
                        itemBinding.etEditValue.addTextChangedListener(object : android.text.TextWatcher {
                            private var isFormatting = false
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(editable: android.text.Editable?) {
                                if (isFormatting) return
                                isFormatting = true
                                val clean = editable.toString().replace(Regex("[-\\s]"), "")
                                val formatted = formatPhone(clean)
                                itemBinding.etEditValue.setText(formatted)
                                itemBinding.etEditValue.setSelection(formatted.length)
                                isFormatting = false
                            }
                        })
                    } else {
                        itemBinding.etEditValue.inputType = InputType.TYPE_CLASS_TEXT
                    }

                    itemBinding.etEditValue.setOnFocusChangeListener { _, hasFocus ->
                        itemBinding.layoutEditMode.isSelected = hasFocus
                    }
                    itemBinding.etEditValue.requestFocus()
                    itemBinding.etEditValue.setSelection(value.length)

                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(itemBinding.etEditValue, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

                    binding.scrollView.postDelayed({
                        var top = itemBinding.root.top
                        var parent = itemBinding.root.parent as? View
                        while (parent != null && parent != binding.scrollView) {
                            top += parent.top
                            parent = parent.parent as? View
                        }
                        binding.scrollView.smoothScrollTo(0, maxOf(0, top - 40))
                    }, 200)
                }
            }

            // 키보드 완료 → 값 저장
            itemBinding.etEditValue.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    val newValue = itemBinding.etEditValue.text.toString().trim()
                    val errorMsg = checkSingleInput(targetStep, newValue)
                    if (errorMsg != null) {
                        itemBinding.tvEditError.text = errorMsg
                        itemBinding.tvEditError.visibility = View.VISIBLE
                    } else {
                        itemBinding.tvEditError.visibility = View.GONE
                        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(itemBinding.etEditValue.windowToken, 0)
                        viewModel.updateCompletedItemValue(targetStep, newValue)
                    }
                    true
                } else false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
