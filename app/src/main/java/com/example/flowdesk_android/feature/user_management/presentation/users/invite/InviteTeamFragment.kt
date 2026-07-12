package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.base.BaseFragment
import com.example.flowdesk_android.databinding.FragmentUserManagementUserInviteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InviteTeamFragment : BaseFragment(R.layout.fragment_user_management_user_invite) {

    private var _binding: FragmentUserManagementUserInviteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InviteTeamViewModel by viewModels()

    private lateinit var roleAdapter: RoleSelectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentUserManagementUserInviteBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getToolbarView(view: View): View? = view.findViewById(R.id.toolbar)

    override fun initView() {
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleSelectionAdapter { _ ->
            // Selection changes handled internally in adapter
        }
        binding.rvRoles.apply {
            layoutManager = com.google.android.flexbox.FlexboxLayoutManager(requireContext()).apply {
                flexDirection = com.google.android.flexbox.FlexDirection.ROW
                flexWrap = com.google.android.flexbox.FlexWrap.WRAP
                justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
            }
            adapter = roleAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnInvite.setOnClickListener {
            val userId = binding.etUserId.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()
            val userName = binding.etUserName.text.toString().trim()
            val userEmail = binding.etUserEmail.text.toString().trim()
            val userTel = binding.etUserTel.text.toString().trim()
            val userHp = binding.etUserHp.text.toString().trim()

            if (userId.isBlank() || password.isBlank() || passwordConfirm.isBlank() || userName.isBlank() || userEmail.isBlank()) {
                Toast.makeText(context, getString(R.string.error_required_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(context, getString(R.string.error_password_mismatch_simple), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoleIds = roleAdapter.getSelectedRoleIds()
            if (selectedRoleIds.isEmpty()) {
                Toast.makeText(context, getString(R.string.error_select_at_least_one_role), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.inviteUser(
                userId = userId,
                password = password,
                userName = userName,
                userEmail = userEmail,
                userTel = userTel,
                userHp = userHp,
                roleIds = selectedRoleIds.toList()
            )
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is InviteTeamUiState.Loading
                        binding.btnInvite.isEnabled = state !is InviteTeamUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is InviteTeamEvent.Success -> {
                                Toast.makeText(context, getString(R.string.success_user_invited), Toast.LENGTH_SHORT).show()
                                parentFragmentManager.setFragmentResult("invite_success", Bundle())
                                findNavController().popBackStack()
                            }
                            is InviteTeamEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.allRoles.collect { roles ->
                        if (roles.isNotEmpty()) {
                            roleAdapter.submitList(roles)
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
