package com.example.flowdesk_android.feature.user_management.presentation.users.detail

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentUserManagementUserDetailBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.example.flowdesk_android.feature.user_management.domain.model.UserDetail
import com.example.flowdesk_android.feature.user_management.presentation.users.detail.UserDetailEvent
import com.example.flowdesk_android.feature.user_management.presentation.users.detail.UserDetailUiState
import com.example.flowdesk_android.feature.user_management.presentation.users.detail.UserDetailViewModel
import com.example.flowdesk_android.feature.user_management.presentation.users.invite.RoleSelectionAdapter
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.flowdesk_android.core.base.BaseFragment
import androidx.navigation.fragment.findNavController
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.core.extension.toFormattedDateString

@AndroidEntryPoint
class UserDetailFragment : BaseFragment(R.layout.fragment_user_management_user_detail) {

    private var _binding: FragmentUserManagementUserDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserDetailViewModel by viewModels()

    private var userId: Int = -1
    private var currentUserData: UserDetail? = null
    private lateinit var roleAdapter: RoleSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_USER_ID, -1)
        }
    }

    override fun getToolbarView(view: View): View? = null
 
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentUserManagementUserDetailBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }
 
    override fun initView() {
        if (userId != -1) {
            viewModel.loadUserDetail(userId)
        } else {
            Toast.makeText(requireContext(), getString(R.string.toast_invalid_access), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleSelectionAdapter()
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

        binding.btnSaveRoles.setOnClickListener {
            val name = binding.etInfoName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.toast_enter_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = binding.etInfoEmail.text.toString().trim()
            val tel = binding.etInfoTel.text.toString().trim().takeIf { it.isNotEmpty() }
            val hp = binding.etInfoHp.text.toString().trim().takeIf { it.isNotEmpty() }

            val roles = roleAdapter.getSelectedRoleIds().toList()

            viewModel.updateInfo(
                id = userId,
                corpName = currentUserData?.corpName,
                userName = name,
                userEmail = email,
                userTel = tel,
                userHp = hp,
                roleIds = roles
            )
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is UserDetailUiState.Loading

                        when (state) {
                            is UserDetailUiState.Loading -> {}
                            is UserDetailUiState.Success -> {
                                bindUserData(state.user)
                            }
                            is UserDetailUiState.Error -> {
                                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is UserDetailEvent.StatusChanged -> {
                                showTopToast(getString(R.string.success_user_status_changed))
                                viewModel.loadUserDetail(userId) // Reload data
                            }
                            is UserDetailEvent.InfoUpdated -> {
                                showTopToast(getString(R.string.success_info_updated))
                                viewModel.loadUserDetail(userId)
                            }
                            is UserDetailEvent.RolesChanged -> {
                                showTopToast(getString(R.string.success_roles_changed))
                                viewModel.loadUserDetail(userId)
                            }
                            is UserDetailEvent.PasswordChanged -> {
                                showTopToast(getString(R.string.success_password_changed))
                            }
                            is UserDetailEvent.TokensInvalidated -> {
                                showTopToast(getString(R.string.success_tokens_invalidated))
                            }
                            is UserDetailEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }


    private fun bindUserData(user: UserDetail) {
        currentUserData = user
        
        binding.tvId.text = user.userId

        // Basic Info
        binding.tvInfoCompany.text = user.corpName ?: "-"
        binding.etInfoName.setText(user.userName)
        binding.etInfoEmail.setText(user.userEmail ?: "")
        binding.etInfoTel.setText(user.userTel ?: "")
        binding.etInfoHp.setText(user.userHp ?: "")

        // Registration Date
        binding.tvRegDate.text = user.regDtm?.toFormattedDateString() ?: "-"

        // Bind Roles
        val roles = user.availableRoles.map { userRole ->
            Role(
                roleId = userRole.roleId,
                roleName = userRole.roleName,
                displayName = userRole.displayName,
                description = userRole.roleName,
                isActive = userRole.isActive,
                userCount = 0,
                permissionCount = 0,
                createdAt = null
            )
        }
        roleAdapter.submitList(roles)
        roleAdapter.setSelectedRoleIds(user.assignedRoleIds.toSet())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Int) = UserDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_USER_ID, userId)
            }
        }
    }
}
