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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CounselDetailMemoFragment : Fragment() {

    private val viewModel: CounselDetailViewModel by viewModels({ requireParentFragment() })

    private lateinit var rvMemos: RecyclerView
    private lateinit var etMemoInput: EditText
    private lateinit var btnSendMemo: View

    private val memoAdapter = CounselMemoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_counsel_detail_memo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMemos = view.findViewById(R.id.rv_memos)
        etMemoInput = view.findViewById(R.id.et_memo_input)
        btnSendMemo = view.findViewById(R.id.btn_send_memo)

        rvMemos.layoutManager = LinearLayoutManager(requireContext())
        rvMemos.adapter = memoAdapter

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.memoList.collect { list ->
                        memoAdapter.submitList(list)
                    }
                }

                launch {
                    viewModel.memoAddState.collect { state ->
                        when (state) {
                            is CounselUpdateState.Success -> {
                                etMemoInput.setText("")
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
        btnSendMemo.setOnClickListener {
            val memoText = etMemoInput.text?.toString()?.trim()
            if (memoText.isNullOrBlank()) {
                Toast.makeText(requireContext(), "메모 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addCounselMemo(memoText)
        }
    }
}
