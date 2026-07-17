package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogInviteSuccessBinding
import com.example.flowdesk_android.databinding.FragmentInviteRoleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

@AndroidEntryPoint
class InviteRoleFragment : Fragment(R.layout.fragment_invite_role) {

    private var _binding: FragmentInviteRoleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InviteTeamViewModel by activityViewModels()
    private lateinit var roleAdapter: RoleSelectionAdapter

    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = androidx.transition.TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInviteRoleBinding.bind(view)

        // Window insets 처리 (카메라 영역 침범 방지)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        // 이전 Fragment에서 전달된 비밀번호 수신
        password = arguments?.getString("password") ?: ""

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleSelectionAdapter(
            onSelectionChanged = { },
            showOrderBadges = true
        ).apply {
            setOnAddRoleClickedListener { showAddRoleWarningDialog() }
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

    private fun showAddRoleWarningDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setMessage("역할 생성으로 넘어갈까요?\n대신 입력했던 내용들은 사라집니다.")
            .setPositiveButton("이동") { _, _ ->
                // R.id.usersFragment(초대 진입 전 메인 목록)까지 백스택을 팝하여 초대 화면들을 완전히 지우고 이동
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.usersFragment, false)
                    .build()
                findNavController().navigate(R.id.rolesFragment, null, navOptions)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnBackRole.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDoInvite.setOnClickListener {
            val selectedRoleIds = roleAdapter.getSelectedRoleIds()
            if (selectedRoleIds.isEmpty()) {
                android.widget.Toast.makeText(
                    context,
                    getString(R.string.error_select_at_least_one_role),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.inviteUser(
                password = password,
                roleIds = selectedRoleIds.toList()
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is InviteTeamUiState.Loading
                        binding.btnDoInvite.isEnabled = state !is InviteTeamUiState.Loading
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is InviteTeamEvent.Success -> {
                                showSuccessDialog(viewModel.userName, viewModel.userEmail)
                            }
                            is InviteTeamEvent.Error -> {
                                android.widget.Toast.makeText(
                                    context, event.message, android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.allRoles.collect { roles ->
                        if (roles.isNotEmpty()) {
                            // 리스트 끝에 "+ 추가하기" 더미 역할 아이템 추가
                            val rolesWithAddButton = roles.toMutableList().apply {
                                add(
                                    com.example.flowdesk_android.feature.user_management.domain.model.Role(
                                        roleId = -999,
                                        roleName = "ADD_NEW_ROLE",
                                        displayName = "+ 추가하기",
                                        description = null,
                                        isActive = true,
                                        userCount = 0,
                                        permissionCount = 0,
                                        createdAt = null
                                    )
                                )
                            }
                            roleAdapter.submitList(rolesWithAddButton)
                        }
                    }
                }
            }
        }
    }

    private fun showSuccessDialog(userName: String, userEmail: String) {
        val dialogBinding = DialogInviteSuccessBinding.inflate(layoutInflater)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDialogName.text = userName
        dialogBinding.tvDialogEmail.text = userEmail

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()

            // 목록 화면으로 돌아가며 refresh 신호 전달
            findNavController().previousBackStackEntry
                ?.savedStateHandle?.set("refresh", true)

            // inviteTeamFragment, invitePasswordFragment, inviteRoleFragment 3개 pop
            findNavController().popBackStack(R.id.inviteTeamFragment, true)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
