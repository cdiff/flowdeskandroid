package com.example.flowdesk_android.feature.auth.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.main.MainActivity
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_auth_splash) {

    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: SplashUiState) {
        when (state) {
            is SplashUiState.Loading -> {
                // 필요 시 로딩 프로그레스바 가시화 (XML에 기본 배치됨)
            }
            is SplashUiState.Success -> {
                navigateToMain(state.info)
            }
            is SplashUiState.NavigateToLogin -> {
                navigateToLogin()
            }
            is SplashUiState.Error -> {
                Toast.makeText(requireContext(), "인증 정보 갱신 실패: ${state.message}", Toast.LENGTH_SHORT).show()
                // 실패한 경우 로그인 화면으로 안전하게 넘겨서 재로그인하도록 처리
                navigateToLogin()
            }
            is SplashUiState.Idle -> {
                // 대기 상태
            }
        }
    }

    private fun navigateToMain(info: AuthMeInfo) {
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            // Gson을 사용해 복잡한 객체(AuthMeInfo)를 JSON 문자열로 간편하게 전달
            val jsonInfo = Gson().toJson(info)
            putExtra("EXTRA_AUTH_ME_INFO", jsonInfo)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToLogin() {
        try {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        } catch (e: Exception) {
            // 이미 화면이 이동했거나 중복 이벤트 발생 시 에러 방어
        }
    }
}
