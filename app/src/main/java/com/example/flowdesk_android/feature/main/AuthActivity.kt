package com.example.flowdesk_android.feature.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 아이콘을 라이트 배경 기준 다크 아이콘으로 설정 (windowLightStatusBar 대체)
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = true

        var statusBarTop = 0
        var navBarBottom = 0

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        fun isImmersiveDestination(destinationId: Int?): Boolean {
            return destinationId == R.id.loginFragment ||
                    destinationId == R.id.onboardingFragment ||
                    destinationId == R.id.splashFragment
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarTop = systemBars.top
            navBarBottom = systemBars.bottom
            val topPadding = if (isImmersiveDestination(navController.currentDestination?.id)) 0 else statusBarTop
            v.updatePadding(top = topPadding, bottom = navBarBottom)
            insets
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val topPadding = if (isImmersiveDestination(destination.id)) 0 else statusBarTop
            binding.root.updatePadding(top = topPadding, bottom = navBarBottom)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
