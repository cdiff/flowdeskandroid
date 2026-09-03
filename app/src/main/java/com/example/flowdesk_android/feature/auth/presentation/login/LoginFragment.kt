package com.example.flowdesk_android.feature.auth.presentation.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.local.TokenManager
import com.example.flowdesk_android.databinding.FragmentAuthLoginBinding
import com.example.flowdesk_android.feature.main.MainActivity
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_auth_login) {

    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    private var isPasswordVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthLoginBinding.bind(view)

        setupAutoLoginState()
        setupInputInteractions()
        setupListeners()
        observeViewModel()
        handlePreFillData()
    }

    private fun setupAutoLoginState() {
        binding.cbAutoLogin.isChecked = tokenManager.isAutoLogin()
        binding.cbAutoLogin.setOnCheckedChangeListener { _, isChecked ->
            tokenManager.setAutoLogin(isChecked)
        }
    }

    private fun setupInputInteractions() {
        // 1. 포커스 상태에 따른 박스 테두리 반응
        binding.etTenant.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutTenantBox.isSelected = hasFocus
        }
        binding.etUserId.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutUserIdBox.isSelected = hasFocus
        }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutPasswordBox.isSelected = hasFocus
        }

        // 2. 사용자가 텍스트 입력 시 에러 상태 자동 소멸
        binding.etTenant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.layoutTenantBox.isActivated) {
                    binding.layoutTenantBox.isActivated = false
                    binding.tvTenantError.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etUserId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.layoutUserIdBox.isActivated) {
                    binding.layoutUserIdBox.isActivated = false
                    binding.tvEmailError.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.layoutPasswordBox.isActivated) {
                    binding.layoutPasswordBox.isActivated = false
                    binding.tvPasswordError.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 3. 키보드 액션 체이닝 (Next ➔ Next ➔ Done ➔ 로그인 시도)
        binding.etTenant.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etUserId.requestFocus()
                true
            } else false
        }

        binding.etUserId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etPassword.requestFocus()
                true
            } else false
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }
    }

    private fun setupListeners() {
        // 비밀번호 표시/숨김 토글 (커서 위치 보존)
        binding.ivPasswordToggle.setOnClickListener {
            val cursorPos = binding.etPassword.selectionEnd
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_lucide_eye_off)
            } else {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_lucide_eye)
            }
            binding.etPassword.setSelection(cursorPos.coerceAtLeast(0))
        }

        // 로그인 버튼
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        // 회원가입 페이지 이동
        val openSignUp = View.OnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        binding.layoutSignUpLink.setOnClickListener(openSignUp)
        binding.tvSignUpLink.setOnClickListener(openSignUp)

        // 하단 약관 및 고객지원 푸터 링크
        binding.tvTerms.setOnClickListener {
            openWebUrl("https://cdiff.github.io/flowdeskandroid/terms-of-service/")
        }
        binding.tvPrivacy.setOnClickListener {
            openWebUrl("https://cdiff.github.io/flowdeskandroid/privacy-policy/")
        }
        binding.tvSupport.setOnClickListener {
            openSupport()
        }
    }

    private fun openWebUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "웹 브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSupport() {
        openWebUrl("https://open.kakao.com/o/sNCXUTLi")
    }

    private fun attemptLogin() {
        val tenantName = binding.etTenant.text.toString().trim()
        val userId = binding.etUserId.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // 1. 초기 에러 상태 리셋
        clearAllErrors()

        // 2. 회사명 유효성 검사
        if (tenantName.isEmpty()) {
            binding.layoutTenantBox.isActivated = true
            binding.tvTenantError.text = "회사명을 입력해주세요."
            binding.tvTenantError.visibility = View.VISIBLE
            binding.etTenant.requestFocus()
            showSoftKeyboard(binding.etTenant)
            return
        }

        // 3. 이메일 유효성 검사 (빈 값 & 포맷 검증)
        if (userId.isEmpty()) {
            binding.layoutUserIdBox.isActivated = true
            binding.tvEmailError.text = "이메일 주소를 입력해주세요."
            binding.tvEmailError.visibility = View.VISIBLE
            binding.etUserId.requestFocus()
            showSoftKeyboard(binding.etUserId)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userId).matches()) {
            binding.layoutUserIdBox.isActivated = true
            binding.tvEmailError.text = "올바른 이메일 형식을 입력해주세요."
            binding.tvEmailError.visibility = View.VISIBLE
            binding.etUserId.requestFocus()
            showSoftKeyboard(binding.etUserId)
            return
        }

        // 4. 비밀번호 유효성 검사
        if (password.isEmpty()) {
            binding.layoutPasswordBox.isActivated = true
            binding.tvPasswordError.text = "비밀번호를 입력해주세요."
            binding.tvPasswordError.visibility = View.VISIBLE
            binding.etPassword.requestFocus()
            showSoftKeyboard(binding.etPassword)
            return
        }

        // 유효성 통과 시 키보드 닫기 및 로그인 실행
        hideSoftKeyboard()
        viewModel.login(tenantName, userId, password)
    }

    private fun clearAllErrors() {
        binding.layoutTenantBox.isActivated = false
        binding.tvTenantError.visibility = View.GONE

        binding.layoutUserIdBox.isActivated = false
        binding.tvEmailError.visibility = View.GONE

        binding.layoutPasswordBox.isActivated = false
        binding.tvPasswordError.visibility = View.GONE
    }

    private fun showSoftKeyboard(targetView: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(targetView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSoftKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun handlePreFillData() {
        arguments?.let { bundle ->
            val tenantName = bundle.getString("tenantName")
            val email = bundle.getString("email")
            val password = bundle.getString("password")
            val isDemo = bundle.getBoolean("EXTRA_DEMO_MODE", false)

            if (!tenantName.isNullOrEmpty()) {
                binding.etTenant.setText(tenantName)
            }
            if (!email.isNullOrEmpty()) {
                binding.etUserId.setText(email)
            }
            if (!password.isNullOrEmpty()) {
                binding.etPassword.setText(password)
            }

            if (isDemo) {
                Toast.makeText(requireContext(), "체험용 데모 계정 정보가 입력되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleLoginState(state)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Loading -> {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.alpha = 0.7f
                binding.etTenant.isEnabled = false
                binding.etUserId.isEnabled = false
                binding.etPassword.isEnabled = false
            }
            is LoginUiState.Success -> {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.alpha = 1.0f
                binding.etTenant.isEnabled = true
                binding.etUserId.isEnabled = true
                binding.etPassword.isEnabled = true

                val welcomeMsg = "환영합니다, ${state.info.user.name} (${state.info.user.corpName})님!"
                Toast.makeText(requireContext(), welcomeMsg, Toast.LENGTH_LONG).show()

                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                    val jsonInfo = Gson().toJson(state.info)
                    putExtra("EXTRA_AUTH_ME_INFO", jsonInfo)
                }
                startActivity(intent)
                requireActivity().finish()
            }
            is LoginUiState.Error -> {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.alpha = 1.0f
                binding.etTenant.isEnabled = true
                binding.etUserId.isEnabled = true
                binding.etPassword.isEnabled = true
                Toast.makeText(requireContext(), "로그인 실패: ${state.message}", Toast.LENGTH_LONG).show()
            }
            is LoginUiState.Idle -> {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.alpha = 1.0f
                binding.etTenant.isEnabled = true
                binding.etUserId.isEnabled = true
                binding.etPassword.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
