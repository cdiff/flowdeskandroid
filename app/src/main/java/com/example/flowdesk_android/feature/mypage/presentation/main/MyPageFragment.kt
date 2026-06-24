package com.example.flowdesk_android.feature.mypage.presentation.main

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.auth.domain.model.Menu
import com.example.flowdesk_android.databinding.FragmentMypageMainBinding
import com.example.flowdesk_android.databinding.DialogUserLogoutAllConfirmationBinding
import com.example.flowdesk_android.core.extension.showTopToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment(R.layout.fragment_mypage_main) {

    private var _binding: FragmentMypageMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMypageMainBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        binding.clProfileSection.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.btnLogoutAll.setOnClickListener {
            showLogoutAllDialog()
        }

        observeViewModel()
        initNotificationSettings()
    }

    private fun showLogoutAllDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_logout_all_confirmation, null)
        val dialogBinding = DialogUserLogoutAllConfirmationBinding.bind(dialogView)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            viewModel.logoutAll()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is MyPageUiState.Success -> updateUI(state)
                            is MyPageUiState.Error -> {
                                showTopToast(state.message.takeIf { it.isNotEmpty() } ?: getString(R.string.mypage_error_load_failed))
                            }
                            is MyPageUiState.Loading -> { /* handle */ }
                        }
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            MyPageEvent.NavigateToLogin -> {
                                val intent = android.content.Intent(requireActivity(), com.example.flowdesk_android.feature.main.AuthActivity::class.java)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            is MyPageEvent.ShowToast -> {
                                showTopToast(event.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(state: MyPageUiState.Success) {
        val user = state.user.user
        binding.tvUserName.text = getString(R.string.mypage_msg_user_suffix, user.name)
        binding.tvRoleBadge.text = state.user.roles.firstOrNull()?.uppercase() ?: "MEMBER"
        binding.tvCompanyName.text = user.corpName

        val permissionCount = state.user.permissions.count { it.value }
        binding.tvPermissionCount.text = getString(R.string.mypage_msg_count_suffix, permissionCount)

        setupMyPermissionsGrid(state.user.menuTree)
    }

    private fun setupMyPermissionsGrid(menuTree: List<Menu>) {
        binding.glMyPermissions.removeAllViews()
        
        menuTree.sortedBy { it.order }.forEach { menuDto ->
             val itemLayout = LinearLayout(requireContext()).apply {
                 orientation = LinearLayout.VERTICAL
                 gravity = Gravity.CENTER
                 layoutParams = GridLayout.LayoutParams().apply {
                     width = 0
                     height = ViewGroup.LayoutParams.WRAP_CONTENT
                     columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                     setMargins(8, 8, 8, 8)
                 }
             }

             val iconView = ImageView(requireContext()).apply {
                 layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 48.dpToPx()).apply {
                     bottomMargin = 8.dpToPx()
                 }
                 background = ContextCompat.getDrawable(context, R.drawable.bg_card_rounded_border)
                 setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
                 
                 val iconRes = getIconForMenu(menuDto.pageName)
                 setImageResource(iconRes)
                 imageTintList = ContextCompat.getColorStateList(context, R.color.dack_gray_text)
             }

             val titleView = TextView(requireContext()).apply {
                 text = menuDto.displayName.replace("관리", "").trim()
                 textSize = 12f
                 setTextColor(ContextCompat.getColor(context, R.color.dack_gray_text))
                 gravity = Gravity.CENTER
             }

             itemLayout.addView(iconView)
             itemLayout.addView(titleView)
             binding.glMyPermissions.addView(itemLayout)
        }
    }



    private fun getIconForMenu(pageName: String): Int {
        return when (pageName) {
            "super" -> R.drawable.ic_super_admin
            "system_management" -> R.drawable.ic_system
            "counsel_management" -> R.drawable.ic_counsel
            "roles" -> R.drawable.ic_roles
            "users" -> R.drawable.ic_users
            "permissions" -> R.drawable.ic_permissions
            else -> R.drawable.ic_default_menu
        }
    }

    private fun initNotificationSettings() {
        val prefs = requireContext().getSharedPreferences("mypage_prefs", android.content.Context.MODE_PRIVATE)
        
        binding.switchNewCounsel.isChecked = prefs.getBoolean("key_new_counsel", true)
        binding.switchTeamInvite.isChecked = prefs.getBoolean("key_team_invite", true)
        binding.switchSecurityLogin.isChecked = prefs.getBoolean("key_security_login", true)

        binding.switchNewCounsel.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("key_new_counsel", isChecked).apply()
            showTopToast(getString(R.string.mypage_msg_notification_updated))
        }

        binding.switchTeamInvite.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("key_team_invite", isChecked).apply()
            showTopToast(getString(R.string.mypage_msg_notification_updated))
        }

        binding.switchSecurityLogin.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("key_security_login", isChecked).apply()
            showTopToast(getString(R.string.mypage_msg_notification_updated))
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
