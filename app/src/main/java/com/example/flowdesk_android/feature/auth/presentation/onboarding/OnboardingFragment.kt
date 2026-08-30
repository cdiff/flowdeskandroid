package com.example.flowdesk_android.feature.auth.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.local.TokenManager
import com.example.flowdesk_android.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // WindowInsets는 AuthActivity 루트에서 일괄 처리 (A안)
        setupViewPager()
        setupClickListeners()
    }


    private fun setupViewPager() {
        val adapter = OnboardingPagerAdapter()
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
                updateBottomButtons(position)
            }
        })
    }

    private fun updateIndicators(position: Int) {
        val dots = listOf(binding.dot0, binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { index, dot ->
            if (index == position) {
                dot.setBackgroundResource(R.drawable.bg_onboarding_indicator_active)
            } else {
                dot.setBackgroundResource(R.drawable.bg_onboarding_indicator_inactive)
            }
        }
    }

    private fun updateBottomButtons(position: Int) {
        val isLastPage = (position == 3)
        if (isLastPage) {
            binding.layoutNavButtons.visibility = View.GONE
            binding.layoutFinalButtons.visibility = View.VISIBLE
        } else {
            binding.layoutNavButtons.visibility = View.VISIBLE
            binding.layoutFinalButtons.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // [다음] 버튼 클릭 시 한 페이지씩 앞으로
        binding.btnNext.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < 4) {
                binding.viewPager.setCurrentItem(nextItem, true)
            }
        }

        // [건너뛰기] 또는 [플로우데스크 시작하기] 클릭 시 로그인 화면으로 이동
        binding.tvSkip.setOnClickListener {
            navigateToLogin()
        }

        binding.btnStart.setOnClickListener {
            navigateToLogin()
        }

        // [데모 계정으로 체험하기] 클릭 시 데모 플래그와 함께 로그인 화면 이동
        binding.btnDemo.setOnClickListener {
            navigateToLogin(isDemo = true)
        }
    }

    private fun navigateToLogin(isDemo: Boolean = false) {
        try {
            tokenManager.setOnboardingSeen(true)
            val bundle = Bundle().apply {
                putBoolean("EXTRA_DEMO_MODE", isDemo)
                if (isDemo) {
                    putString("tenantName", "demo_company")
                    putString("email", "demo_admin")
                    putString("password", "Admin123")
                }
            }
            findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment, bundle)
        } catch (e: Exception) {
            // 이미 화면 전환된 경우 방어
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
