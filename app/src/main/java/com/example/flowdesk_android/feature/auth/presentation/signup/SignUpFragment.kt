package com.example.flowdesk_android.feature.auth.presentation.signup

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentAuthSignupBinding
import com.example.flowdesk_android.databinding.ItemSignupCompletedFieldBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.transition.Fade
import androidx.transition.ChangeBounds
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_auth_signup) {

    private var _binding: FragmentAuthSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by activityViewModels()

    private val phoneTextWatcher = object : android.text.TextWatcher {
        private var isFormatting = false
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            if (isFormatting) return
            if (viewModel.currentStep.value != SignUpStep.PHONE) return

            isFormatting = true
            val clean = s.toString().replace(" ", "")
            val formatted = formatPhoneSpace(clean)
            binding.etCurrentInput.setText(formatted)
            binding.etCurrentInput.setSelection(formatted.length)
            isFormatting = false
        }
    }

    private fun formatPhoneSpace(clean: String): String {
        val length = clean.length
        return when {
            length <= 3 -> clean
            length <= 7 -> "${clean.substring(0, 3)} ${clean.substring(3)}"
            length <= 11 -> "${clean.substring(0, 3)} ${clean.substring(3, 7)} ${clean.substring(7)}"
            else -> "${clean.substring(0, 3)} ${clean.substring(3, 7)} ${clean.substring(7, 11)}"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthSignupBinding.bind(view)

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
                // 이미 골뱅이가 있다면 그 앞의 아이디 부분만 잘라낸 뒤 칩의 도메인을 결합함
                val id = if (currentText.contains("@")) {
                    currentText.substringBefore("@")
                } else {
                    currentText
                }
                val completedEmail = "$id@$domain"
                binding.etCurrentInput.setText(completedEmail)
                binding.etCurrentInput.setSelection(completedEmail.length) // 커서를 마지막으로

                // 자동완성 후 곧바로 다음 단계로 자동 진행하여 가속 가입 제공
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
        // 뒤로가기
        binding.btnBack.setOnClickListener {
            if (!viewModel.previousStep()) {
                findNavController().popBackStack()
            }
        }

        // 일반 필드 포커스 리스너
        binding.etCurrentInput.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutNormalInputBox.isSelected = hasFocus
        }

        // 전화번호 실시간 자동 띄어쓰기 감지
        binding.etCurrentInput.addTextChangedListener(phoneTextWatcher)

        // 키보드 완료(Done) 버튼 → 다음 버튼과 동일 동작
        binding.etCurrentInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                handleNext()
                true
            } else false
        }

        // 다음 버튼
        binding.btnNext.setOnClickListener { handleNext() }
    }

    /**
     * 현재 열려 있는 모든 인라인 수정 필드를 검사하고 자동 저장함.
     * @return true: 모두 정상이거나 열린 필드가 없음, false: 유효성 검사 실패(에러 표시 및 포커스 유지)
     */
    private fun saveActiveInlineEdits(): Boolean {
        val steps = SignUpStep.entries
        for (i in 0 until binding.containerCompletedItems.childCount) {
            val child = binding.containerCompletedItems.getChildAt(i)
            val itemBinding = ItemSignupCompletedFieldBinding.bind(child)
            if (itemBinding.layoutEditMode.visibility == View.VISIBLE) {
                val label = itemBinding.tvEditLabel.text.toString()
                val targetStep = steps.find { it.label == label } ?: SignUpStep.ADMIN_NAME
                val newValue = itemBinding.etEditValue.text.toString().trim()
                val errorMsg = checkSingleInput(targetStep, newValue)
                if (errorMsg != null) {
                    itemBinding.tvEditError.text = errorMsg
                    itemBinding.tvEditError.visibility = View.VISIBLE
                    itemBinding.etEditValue.requestFocus()
                    return false
                } else {
                    itemBinding.tvEditError.visibility = View.GONE
                    viewModel.updateCompletedItemValue(targetStep, newValue)
                    itemBinding.layoutEditMode.visibility = View.GONE
                    itemBinding.layoutReadMode.visibility = View.VISIBLE
                }
            }
        }
        return true
    }

    private fun handleNext() {
        // 1. 혹시 수정 중이던 이전 필드가 있다면 먼저 검사 및 자동 저장
        if (!saveActiveInlineEdits()) return

        val currentStep = viewModel.currentStep.value
        val inputValue = binding.etCurrentInput.text.toString().trim()

        if (!validateInput(currentStep, inputValue)) return

        val hasMore = viewModel.nextStep(inputValue)
        if (!hasMore) {
            // 모든 단계 완료 → 비밀번호 Fragment로 이동
            findNavController().navigate(R.id.action_signUpFragment_to_signUpPasswordFragment)
        }
        // 다음 단계 UI는 observeViewModel()에서 currentStep 변화로 자동 처리
    }

    private fun validateInput(step: SignUpStep, value: String): Boolean {
        binding.tvInputError.visibility = View.GONE

        val errorMsg = checkSingleInput(step, value)
        if (errorMsg != null) {
            showError(errorMsg)
            return false
        }
        return true
    }

    private fun checkSingleInput(step: SignUpStep, value: String): String? {
        if (value.isEmpty()) {
            return "${step.label}을(를) 입력해주세요."
        }

        if (step == SignUpStep.TENANT) {
            val pattern = Regex("^[a-z0-9][a-z0-9-]{2,29}$")
            if (!pattern.matches(value)) {
                return "영문 소문자, 숫자, 하이픈(-)만 사용, 3~30자"
            }
        }

        if (step == SignUpStep.EMAIL) {
            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (!emailPattern.matches(value)) {
                return "올바른 이메일 형식이 아닙니다."
            }
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
                // 단계 변화 관찰
                launch {
                    viewModel.currentStep.collect { step ->
                        applyStep(step)
                    }
                }

                // 완료 항목 관찰 → 컨테이너 갱신
                launch {
                    viewModel.completedItems.collect { items ->
                        renderCompletedItems(items)
                    }
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

    private fun applyStep(step: SignUpStep) {
        // 자연스러운 쫀득한 레이아웃 변경 트랜지션 시작
        TransitionManager.beginDelayedTransition(binding.scrollView, getCustomTransition())

        // 타이틀 및 라벨 변경
        binding.tvStepTitle.text = step.titleText
        binding.tvInputLabel.text = step.label

        // 이메일 단계일 때 도메인 간편 추천 칩 노출
        if (step == SignUpStep.EMAIL) {
            binding.scrollEmailChips.visibility = View.VISIBLE
        } else {
            binding.scrollEmailChips.visibility = View.GONE
        }

        // 기존에 저장된 값이 있으면 세팅 (없으면 빈칸)
        val existingValue = when (step) {
            SignUpStep.ADMIN_NAME -> viewModel.adminName
            SignUpStep.EMAIL -> viewModel.email
            SignUpStep.PHONE -> viewModel.phone
            SignUpStep.COMPANY -> viewModel.companyName
            SignUpStep.TENANT -> viewModel.tenantName
        }
        binding.etCurrentInput.setText(existingValue)
        binding.etCurrentInput.setSelection(existingValue.length) // 커서를 마지막으로
        binding.etCurrentInput.hint = step.hint

        // 입력타입 셋팅 (전화번호는 숫자 키패드, 이메일은 이메일 키패드, 나머지는 일반 텍스트)
        binding.etCurrentInput.inputType = when (step) {
            SignUpStep.PHONE -> InputType.TYPE_CLASS_PHONE
            SignUpStep.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT
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

        // 스크롤 최상단으로
        binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
    }

    private fun renderCompletedItems(items: List<Pair<String, String>>) {
        // 부모 스크롤뷰 기준으로 트랜지션을 걸어 전체 카드 추가가 자연스럽게 동작하도록 수정
        TransitionManager.beginDelayedTransition(binding.scrollView, getCustomTransition())
        binding.containerCompletedItems.removeAllViews()
        val steps = SignUpStep.entries
        // 뷰모델에서 이미 최신순(0번째 추가)으로 리스트를 주기 때문에 reversed() 없이 정방향 출력
        items.forEachIndexed { index, (label, value) ->
            val itemBinding = ItemSignupCompletedFieldBinding.inflate(
                layoutInflater, binding.containerCompletedItems, true
            )
            // 라벨 매칭을 통해 역순 정렬과 무관하게 정확한 단계를 탐색
            val targetStep = steps.find { it.label == label } ?: SignUpStep.ADMIN_NAME
            itemBinding.tvCompletedLabel.text = label

            if (targetStep == SignUpStep.EMAIL && value.contains("@")) {
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

            // 완료 항목 카드 클릭 시 인라인 편집 모드로 전환
            itemBinding.root.setOnClickListener {
                if (itemBinding.layoutEditMode.visibility == View.GONE) {
                    // 다른 열려있는 수정 카드가 있다면 먼저 저장 후 닫기
                    saveActiveInlineEdits()

                    itemBinding.layoutReadMode.visibility = View.GONE
                    itemBinding.layoutEditMode.visibility = View.VISIBLE

                    itemBinding.tvEditLabel.text = label
                    // 클로저에 캡처된 초기 value가 아닌 ViewModel의 최신값을 읽어옴
                    val currentValue = when (targetStep) {
                        SignUpStep.ADMIN_NAME -> viewModel.adminName
                        SignUpStep.EMAIL -> viewModel.email
                        SignUpStep.PHONE -> viewModel.phone
                        SignUpStep.COMPANY -> viewModel.companyName
                        SignUpStep.TENANT -> viewModel.tenantName
                    }
                    itemBinding.etEditValue.setText(currentValue)

                    // 전화번호는 숫자 키패드 지원 및 실시간 띄어쓰기 연동
                    if (targetStep == SignUpStep.PHONE) {
                        itemBinding.etEditValue.inputType = InputType.TYPE_CLASS_PHONE
                        itemBinding.etEditValue.addTextChangedListener(object : android.text.TextWatcher {
                            private var isFormatting = false
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(editable: android.text.Editable?) {
                                if (isFormatting) return
                                isFormatting = true
                                val clean = editable.toString().replace(" ", "")
                                val formatted = formatPhoneSpace(clean)
                                itemBinding.etEditValue.setText(formatted)
                                itemBinding.etEditValue.setSelection(formatted.length)
                                isFormatting = false
                            }
                        })
                    } else {
                        itemBinding.etEditValue.inputType = InputType.TYPE_CLASS_TEXT
                    }

                    // 수정 모드 에디터 포커스 리스너 연동: 다른 필드로 포커스가 이동하면 자동 저장
                    itemBinding.etEditValue.setOnFocusChangeListener { _, hasFocus ->
                        itemBinding.layoutEditMode.isSelected = hasFocus
                        if (!hasFocus && itemBinding.layoutEditMode.visibility == View.VISIBLE) {
                            val newValue = itemBinding.etEditValue.text.toString().trim()
                            val errorMsg = checkSingleInput(targetStep, newValue)
                            if (errorMsg == null && newValue.isNotEmpty()) {
                                itemBinding.tvEditError.visibility = View.GONE
                                viewModel.updateCompletedItemValue(targetStep, newValue)
                                itemBinding.layoutEditMode.visibility = View.GONE
                                itemBinding.layoutReadMode.visibility = View.VISIBLE
                            }
                        }
                    }

                    itemBinding.etEditValue.requestFocus()
                    itemBinding.etEditValue.setSelection(currentValue.length)

                    // 키보드 자동으로 올리기
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(itemBinding.etEditValue, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

                    // 키보드가 올라온 후 해당 입력창이 가려지지 않고 화면 상단에 잘 보이도록 스크롤 보정
                    binding.scrollView.postDelayed({
                        var top = itemBinding.root.top
                        var parent = itemBinding.root.parent as? View
                        while (parent != null && parent != binding.scrollView) {
                            top += parent.top
                            parent = parent.parent as? View
                        }
                        // 에디터 박스 상단 라벨이 넉넉히 보이도록 40px 정도 마진을 빼고 스크롤
                        binding.scrollView.smoothScrollTo(0, Math.max(0, top - 40))
                    }, 200)
                }
            }

            // 키보드 완료(Done) / Next 키를 누르면 수정을 적용함
            itemBinding.etEditValue.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                    val newValue = itemBinding.etEditValue.text.toString().trim()
                    val errorMsg = checkSingleInput(targetStep, newValue)
                    if (errorMsg != null) {
                        itemBinding.tvEditError.text = errorMsg
                        itemBinding.tvEditError.visibility = View.VISIBLE
                    } else {
                        itemBinding.tvEditError.visibility = View.GONE

                        // 키보드 닫기
                        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(itemBinding.etEditValue.windowToken, 0)

                        viewModel.updateCompletedItemValue(targetStep, newValue)
                        itemBinding.layoutEditMode.visibility = View.GONE
                        itemBinding.layoutReadMode.visibility = View.VISIBLE
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
