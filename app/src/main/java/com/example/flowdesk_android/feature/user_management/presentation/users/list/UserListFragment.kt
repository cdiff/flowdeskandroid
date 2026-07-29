package com.example.flowdesk_android.feature.user_management.presentation.users.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentUserManagementUserListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : Fragment(R.layout.fragment_user_management_user_list) {
    private var _binding: FragmentUserManagementUserListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserListViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserManagementUserListBinding.bind(view)

        parentFragmentManager.setFragmentResultListener("invite_success", viewLifecycleOwner) { _, _ ->
            viewModel.triggerRefresh()
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.triggerRefresh()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onItemClick = { user ->
                val bundle = Bundle().apply { putInt("user_id", user.userSeq) }
                findNavController().navigate(R.id.userDetailFragment, bundle)
            },
            onToggleStatusClick = { user ->
                viewModel.toggleUserStatus(user.userSeq, user.isActive)
            },
            onDeleteUserClick = { user ->
                viewModel.invalidateTokens(user.userSeq)
            }
        )
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupListeners() {
        binding.btnInviteTeam.setOnClickListener {
            findNavController().navigate(R.id.inviteTeamFragment)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
                binding.btnClear.isVisible = !s.isNullOrEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClear.setOnClickListener {
            binding.etSearch.text.clear()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is UserListUiState.Loading
                        if (state is UserListUiState.Error) {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                launch {
                    viewModel.filteredUsers.collect { users ->
                        userAdapter.submitList(users)
                        binding.tvTotalCount.text = "${users.size}명"

                        val activeCount = users.count { it.isActive }
                        val inactiveCount = users.size - activeCount

                        binding.tvActiveCount.text = "${activeCount}명"
                        binding.tvInactiveCount.text = "${inactiveCount}명"
                    }
                }

                launch {
                    viewModel.errorFlow.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is UserListEvent.TokensInvalidated -> {
                                Toast.makeText(requireContext(), getString(R.string.success_tokens_invalidated), Toast.LENGTH_SHORT).show()
                            }
                            is UserListEvent.StatusToggled -> {
                                Toast.makeText(requireContext(), getString(R.string.success_user_status_changed), Toast.LENGTH_SHORT).show()
                            }
                        }
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
