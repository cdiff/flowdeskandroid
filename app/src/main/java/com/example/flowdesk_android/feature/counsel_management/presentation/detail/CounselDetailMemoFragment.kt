package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentCounselDetailMemoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CounselDetailMemoFragment : Fragment() {

    private val viewModel: CounselDetailViewModel by viewModels({ requireParentFragment() })

    // Binding
    private var _binding: FragmentCounselDetailMemoBinding? = null
    private val binding get() = _binding!!

    private val memoAdapter = CounselMemoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCounselDetailMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMemos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMemos.adapter = memoAdapter

        observeViewModel()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.memoList.collect { list ->
                        memoAdapter.submitList(list)
                        if (list.isEmpty()) {
                            binding.llEmptyMemo.visibility = View.VISIBLE
                            binding.rvMemos.visibility = View.GONE
                        } else {
                            binding.llEmptyMemo.visibility = View.GONE
                            binding.rvMemos.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    viewModel.memoAddState.collect { state ->
                        when (state) {
                            is CounselUpdateState.Success -> {
                                binding.etMemoInput.setText("")
                                viewModel.resetMemoAddState()
                            }
                            is CounselUpdateState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetMemoAddState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSendMemo.setOnClickListener {
            val memoText = binding.etMemoInput.text?.toString()?.trim()
            if (memoText.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.counsel_toast_enter_memo), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addCounselMemo(memoText)
        }
    }
}
