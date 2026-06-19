package com.example.flowdesk_android.feature.system_management.presentation.block.keyword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.databinding.FragmentBlockKeywordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockKeywordFragment : Fragment() {

    private var _binding: FragmentBlockKeywordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockKeywordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
