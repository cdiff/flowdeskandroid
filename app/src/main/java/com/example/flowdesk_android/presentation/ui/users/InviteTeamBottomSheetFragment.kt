package com.example.flowdesk_android.presentation.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.CreateUserRequest
import com.example.flowdesk_android.databinding.DialogInviteTeamBinding
import com.example.flowdesk_android.presentation.viewmodel.InviteTeamState
import com.example.flowdesk_android.presentation.viewmodel.InviteTeamViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InviteTeamBottomSheetFragment(private val onSuccess: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: DialogInviteTeamBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InviteTeamViewModel by viewModels()

    private lateinit var roleAdapter: RoleSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogInviteTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }
    private fun setupRecyclerView() {
        roleAdapter = RoleSelectionAdapter { _ ->
            // selection handled inside adapter
        }
        binding.rvRoles.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
            adapter = roleAdapter
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnInvite.setOnClickListener {
            val userId = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()
            val userName = binding.etUserName.text.toString()
            val userEmail = binding.etUserEmail.text.toString()
            val userTel = binding.etUserTel.text.toString()
            val userHp = binding.etUserHp.text.toString()

            if (userId.isBlank() || password.isBlank() || passwordConfirm.isBlank() || userName.isBlank() || userEmail.isBlank()) {
                Toast.makeText(context, "필수 항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoleIds = roleAdapter.getSelectedRoleIds()
            if (selectedRoleIds.isEmpty()) {
                Toast.makeText(context, "하나 이상의 역할을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CreateUserRequest(
                userId = userId,
                password = password,
                corpName = "Acme Corporation",
                userName = userName,
                userEmail = userEmail,
                userTel = userTel,
                userHp = userHp,
                roleIds = selectedRoleIds.toList()
            )

            viewModel.inviteUser(request)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        binding.progressBar.isVisible = state is InviteTeamState.Loading
                        binding.btnInvite.isEnabled = state !is InviteTeamState.Loading

                        when (state) {
                            is InviteTeamState.Success -> {
                                Toast.makeText(context, "팀원 초대가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                                dismiss()
                            }
                            is InviteTeamState.Error -> {
                                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> {}
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

    companion object {
        const val TAG = "InviteTeamBottomSheet"
    }
}
