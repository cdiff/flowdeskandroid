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

    private var selectedRole: Role? = null

    enum class Role {
        ADMIN, MANAGER, MEMBER, GUEST
    }

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

        setupListeners()
        setupRoleSelection()
        observeViewModel()
    }

    private fun setupRoleSelection() {
        binding.clRoleAdmin.setOnClickListener { selectRole(Role.ADMIN) }
        binding.clRoleManager.setOnClickListener { selectRole(Role.MANAGER) }
        binding.clRoleMember.setOnClickListener { selectRole(Role.MEMBER) }
        binding.clRoleGuest.setOnClickListener { selectRole(Role.GUEST) }
    }

    private fun selectRole(role: Role) {
        selectedRole = role

        val unselectedCard = R.drawable.bg_card_rounded_border
        val unselectedRadio = R.drawable.ic_radio_unselected
        val unselectedIconBg = R.drawable.bg_icon_gray
        val unselectedTitleColor = android.graphics.Color.parseColor("#3A485A")
        val unselectedSubtitleColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_text)
        val unselectedIconTint = android.graphics.Color.parseColor("#8BA1B8")

        val selectedCard = R.drawable.bg_card_rounded_border_selected
        val selectedRadio = R.drawable.ic_radio_selected
        val selectedIconBg = R.drawable.bg_icon_green
        val selectedTitleColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.login_blue)
        val selectedSubtitleColor = android.graphics.Color.parseColor("#7B8DA3")
        val selectedIconTint = android.graphics.Color.parseColor("#FFFFFF")

        fun resetCard(cl: View, radio: android.widget.ImageView, icon: android.widget.ImageView, title: android.widget.TextView, desc: android.widget.TextView) {
            cl.setBackgroundResource(unselectedCard)
            radio.setImageResource(unselectedRadio)
            icon.setBackgroundResource(unselectedIconBg)
            icon.setColorFilter(unselectedIconTint)
            title.setTextColor(unselectedTitleColor)
            desc.setTextColor(unselectedSubtitleColor)
        }

        fun setCard(cl: View, radio: android.widget.ImageView, icon: android.widget.ImageView, title: android.widget.TextView, desc: android.widget.TextView) {
            cl.setBackgroundResource(selectedCard)
            radio.setImageResource(selectedRadio)
            icon.setBackgroundResource(selectedIconBg)
            icon.setColorFilter(selectedIconTint)
            title.setTextColor(selectedTitleColor)
            desc.setTextColor(selectedSubtitleColor)
        }

        resetCard(binding.clRoleAdmin, binding.ivAdminRadio, binding.ivAdmin, binding.tvAdmin, binding.tvAdminDesc)
        resetCard(binding.clRoleManager, binding.ivManagerRadio, binding.ivManager, binding.tvManager, binding.tvManagerDesc)
        resetCard(binding.clRoleMember, binding.ivMemberRadio, binding.ivMember, binding.tvMember, binding.tvMemberDesc)
        resetCard(binding.clRoleGuest, binding.ivGuestRadio, binding.ivGuest, binding.tvGuest, binding.tvGuestDesc)

        binding.tvSelectedRole.visibility = View.VISIBLE

        when (role) {
            Role.ADMIN -> {
                setCard(binding.clRoleAdmin, binding.ivAdminRadio, binding.ivAdmin, binding.tvAdmin, binding.tvAdminDesc)
                binding.tvSelectedRole.text = "선택된 역할: 관리자"
            }
            Role.MANAGER -> {
                setCard(binding.clRoleManager, binding.ivManagerRadio, binding.ivManager, binding.tvManager, binding.tvManagerDesc)
                binding.tvSelectedRole.text = "선택된 역할: 매니저"
            }
            Role.MEMBER -> {
                setCard(binding.clRoleMember, binding.ivMemberRadio, binding.ivMember, binding.tvMember, binding.tvMemberDesc)
                binding.tvSelectedRole.text = "선택된 역할: 팀원"
            }
            Role.GUEST -> {
                setCard(binding.clRoleGuest, binding.ivGuestRadio, binding.ivGuest, binding.tvGuest, binding.tvGuestDesc)
                binding.tvSelectedRole.text = "선택된 역할: 게스트"
            }
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

            if (selectedRole == null) {
                Toast.makeText(context, "역할을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Using hardcoded corpName for now based on JSON example or empty
            val request = CreateUserRequest(
                userId = userId,
                password = password,
                corpName = "Acme Corporation",
                userName = userName,
                userEmail = userEmail,
                userTel = userTel,
                userHp = userHp
            )

            viewModel.inviteUser(request)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
