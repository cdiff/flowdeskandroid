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
import com.example.flowdesk_android.databinding.FragmentUserDetailBinding
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class UserDetailFragment : BaseFragment(R.layout.fragment_user_detail) {

    private var _binding: FragmentUserDetailBinding? = null
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

    override fun getToolbarView(view: View): View? = view.findViewById(R.id.toolbar)
 
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentUserDetailBinding.bind(view)
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
        roleAdapter = RoleSelectionAdapter { _ ->
            // Selection changes handled internally in adapter
        }
        binding.rvRoles.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = roleAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.btnToggleStatus.setOnClickListener {
            currentUserData?.let { user ->
                viewModel.updateStatus(userId, !user.isActive)
            }
        }

        binding.btnSaveRoles.setOnClickListener {
            val name = binding.etInfoName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.toast_enter_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val corpName = binding.etInfoCompany.text.toString().trim().takeIf { it.isNotEmpty() }
            val email = binding.etInfoEmail.text.toString().trim()
            val tel = binding.etInfoTel.text.toString().trim().takeIf { it.isNotEmpty() }
            val hp = binding.etInfoHp.text.toString().trim().takeIf { it.isNotEmpty() }

            val roles = roleAdapter.getSelectedRoleIds().toList()

            viewModel.updateInfo(
                id = userId,
                corpName = corpName,
                userName = name,
                userEmail = email,
                userTel = tel,
                userHp = hp,
                roleIds = roles
            )
        }

        binding.btnChangePassword.setOnClickListener {
            val newPwd = binding.etNewPassword.text.toString()
            if (newPwd.length < 6) {
                Toast.makeText(requireContext(), getString(R.string.error_password_min_length), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.changePassword(userId, newPwd)
        }
        binding.btnInvalidateTokens.setOnClickListener {
            viewModel.invalidateTokens(userId)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is UserDetailUiState.Loading

                        when (state) {
                            is UserDetailUiState.Success -> {
                                bindUserData(state.user)
                            }
                            is UserDetailUiState.Error -> {
                                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> {}
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
                                binding.etNewPassword.text?.clear()
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

    @Suppress("DEPRECATION")
    private fun showTopToast(message: String) {
        val inflater = requireActivity().layoutInflater
        val layout = inflater.inflate(R.layout.view_common_toast_top, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message

        val toast = Toast(requireContext())
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    private fun createRoleBadge(label: String): TextView {
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val dp12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        val dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        return TextView(requireContext()).apply {
            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.bg_badge_black)
            setPadding(dp12, dp4, dp12, dp4)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = dp8
            layoutParams = params
        }
    }

    private fun bindUserData(user: UserDetail) {
        currentUserData = user
        
        // Header
        binding.tvName.text = user.userName
        binding.tvId.text = "@${user.userId}"
        
        // Set all assigned role badges
        val assignedRoles = user.availableRoles.filter { it.isAssigned }
        binding.llRoleBadges.removeAllViews()
        if (assignedRoles.isEmpty()) {
            val badge = createRoleBadge("사용자")
            binding.llRoleBadges.addView(badge)
        } else {
            assignedRoles.forEach { role ->
                val badge = createRoleBadge(role.displayName)
                binding.llRoleBadges.addView(badge)
            }
        }

        // Status
        if (user.isActive) {
            binding.tvStatusBadge.background = null
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_500))
            binding.tvStatusBadge.text = "활성"
            binding.btnToggleStatus.text = "비활성화"
        } else {
            binding.tvStatusBadge.background = null
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_500))
            binding.tvStatusBadge.text = "정지"
            binding.btnToggleStatus.text = "활성화"
        }

        // Basic Info
        binding.etInfoCompany.setText(user.corpName ?: "")
        binding.etInfoName.setText(user.userName)
        binding.etInfoEmail.setText(user.userEmail ?: "")
        binding.etInfoTel.setText(user.userTel ?: "")
        binding.etInfoHp.setText(user.userHp ?: "")

        // Registration Date
        user.regDtm?.let { isoString ->
            try {
                // Parse assuming format "2026-01-28T11:11:44.000Z"
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(isoString)
                
                if (date != null) {
                    val formatter = SimpleDateFormat("yyyy. MM. dd. a hh:mm", Locale.KOREA)
                    formatter.timeZone = TimeZone.getDefault()
                    binding.tvRegDate.text = formatter.format(date)
                } else {
                    binding.tvRegDate.text = isoString
                }
            } catch (e: Exception) {
                binding.tvRegDate.text = isoString
            }
        } ?: run {
            binding.tvRegDate.text = "-"
        }

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
