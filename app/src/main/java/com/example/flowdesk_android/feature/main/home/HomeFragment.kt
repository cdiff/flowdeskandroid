package com.example.flowdesk_android.feature.main.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    // TopBar and BottomNav logic moved to MainActivity
    // This fragment is now just for content
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Add content logic here if needed
    }
}
