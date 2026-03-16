package com.example.flowdesk_android.presentation.ui.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentUsersBinding
import com.example.flowdesk_android.presentation.viewmodel.UserManagementViewModel
import com.example.flowdesk_android.presentation.viewmodel.UsersState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserManagementFragment : Fragment(R.layout.fragment_users) {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserManagementViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsersBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            // Handle row click
            Toast.makeText(context, "Clicked: ${user.userName}", Toast.LENGTH_SHORT).show()
        }
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupListeners() {
        binding.btnInviteTeam.setOnClickListener {
            val bottomSheet = InviteTeamBottomSheetFragment {
                viewModel.fetchUsers() // Refresh list on success
            }
            bottomSheet.show(childFragmentManager, InviteTeamBottomSheetFragment.TAG)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                launch {
                    viewModel.usersState.collect { state ->
                        binding.progressBar.isVisible = state is UsersState.Loading
                        if (state is UsersState.Error) {
                            binding.tvTotalTitle.text = "Error: ${state.message}"
                            binding.tvTotalTitle.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red))
                        } else {
                            binding.tvTotalTitle.text = "전체 사용자"
                            binding.tvTotalTitle.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_text))
                        }
                    }
                }

                launch {
                    viewModel.filteredUsers.collect { users ->
                        userAdapter.submitList(users)

                        binding.tvTotalCount.text = "${users.size}명"
                        
                        val activeCount = users.count { it.isActive == 1 }
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
