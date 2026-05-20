package com.example.flowdesk_android.feature.user.presentation.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.UserFragmentListBinding
import com.example.flowdesk_android.feature.user.presentation.invite.InviteTeamBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class UserListFragment : Fragment(R.layout.user_fragment_list) {
    private var _binding: UserFragmentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserListViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = UserFragmentListBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            val bundle = Bundle().apply { putInt("user_id", user.userSeq) }
            findNavController().navigate(R.id.userDetailFragment, bundle)
        }
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupListeners() {
        binding.btnInviteTeam.setOnClickListener {
            val bottomSheet = InviteTeamBottomSheetFragment {
                viewModel.fetchUsers()
            }
            bottomSheet.show(childFragmentManager, InviteTeamBottomSheetFragment.TAG)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is UserListUiState.Loading
                        if (state is UserListUiState.Error) {
                            binding.tvTotalTitle.text = "Error: ${state.message}"
                            binding.tvTotalTitle.setTextColor(
                                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red)
                            )
                        } else {
                            binding.tvTotalTitle.text = "전체 사용자"
                            binding.tvTotalTitle.setTextColor(
                                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_text)
                            )
                        }
                    }
                }

                launch {
                    viewModel.filteredUsers.collect { users ->
                        userAdapter.submitList(users)
                        binding.tvTotalCount.text = "${users.size}명"

                        val activeCount = users.count { it.isActive }
                        val inactiveCount = users.size - activeCount

                        binding.tvActiveCount.text = activeCount.toString()
                        binding.tvInactiveCount.text = inactiveCount.toString()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
