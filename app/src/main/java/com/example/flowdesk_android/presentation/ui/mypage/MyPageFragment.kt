package com.example.flowdesk_android.presentation.ui.mypage

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
import com.example.flowdesk_android.data.remote.dto.MenuDto
import com.example.flowdesk_android.databinding.FragmentMypageBinding
import com.example.flowdesk_android.presentation.viewmodel.DashboardState
import com.example.flowdesk_android.presentation.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    // Reuse DashboardViewModel as it already fetches "Me" data
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMypageBinding.bind(view)

        // Handle window insets for status bar
        // Window insets handled by parent activity or root view if needed
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
        

        
        binding.clProfileSection.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboardState.collect { state ->
                    when (state) {
                        is DashboardState.Success -> {
                            updateUI(state)
                        }
                        is DashboardState.Error -> {
                            // Handle error
                        }
                        is DashboardState.Loading -> {
                            // Handle loading
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateUI(state: DashboardState.Success) {
        val user = state.data.user
        binding.tvUserName.text = "${user.userName}님"
        binding.tvRoleBadge.text = state.data.roles.firstOrNull()?.uppercase() ?: "MEMBER"
        binding.tvCompanyName.text = user.corpName
        
        val permissionCount = state.data.permissions.count { it.value }
        binding.tvPermissionCount.text = "${permissionCount}개"

        setupMyPermissionsGrid(state.data.menuTree)
        setupPermissionDetails(state.data.menuTree, state.data.permissions)
    }

    private fun setupMyPermissionsGrid(menuTree: List<MenuDto>) {
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
                 text = menuDto.displayName
                 textSize = 12f
                 setTextColor(ContextCompat.getColor(context, R.color.dack_gray_text))
                 gravity = Gravity.CENTER
             }

             itemLayout.addView(iconView)
             itemLayout.addView(titleView)
             binding.glMyPermissions.addView(itemLayout)
        }
    }

    private fun setupPermissionDetails(menuTree: List<MenuDto>, permissions: Map<String, Boolean>) {
        binding.llPermissionDetails.removeAllViews()

        menuTree.sortedBy { it.order }.forEach { menuDto ->
            val detailLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = 24.dpToPx()
                }
            }

            // Header (Icon + Title)
            val headerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = 8.dpToPx()
                }
            }
            
            val iconView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(24.dpToPx(), 24.dpToPx()).apply {
                    rightMargin = 8.dpToPx()
                }
                setImageResource(getIconForMenu(menuDto.pageName))
                imageTintList = ContextCompat.getColorStateList(context, R.color.dack_gray_text) 
            }

            val titleView = TextView(requireContext()).apply {
                text = menuDto.displayName
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            
            headerLayout.addView(iconView)
            headerLayout.addView(titleView)
            detailLayout.addView(headerLayout)

            // Chips Container (Using ChipGroup as standard FlowLayout alternative)
            val chipsContainer = com.google.android.material.chip.ChipGroup(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            // Infer permissions related to this menu
            val relatedPermissions = permissions.filter { (key, value) -> key.startsWith(menuDto.pageName) && value }
            
            if (relatedPermissions.isNotEmpty()) {
                relatedPermissions.forEach { (key, _) ->
                    val actionName = getActionNameFromKey(key)
                    val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                        text = actionName
                        textSize = 12f
                        setTextColor(ContextCompat.getColor(context, R.color.green_accent))
                        setChipBackgroundColorResource(R.color.green_light)
                        chipStrokeWidth = 0f
                        textStartPadding = 8.dpToPx().toFloat()
                        textEndPadding = 8.dpToPx().toFloat()
                    }
                    chipsContainer.addView(chip)
                }
            } else {
                 val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                        text = "전체 권한" 
                        textSize = 12f
                        setTextColor(ContextCompat.getColor(context, R.color.green_accent))
                        setChipBackgroundColorResource(R.color.green_light)
                        chipStrokeWidth = 0f
                 }
                 chipsContainer.addView(chip)
            }
            
            detailLayout.addView(chipsContainer)
            binding.llPermissionDetails.addView(detailLayout)
        }
    }

    private fun getIconForMenu(pageName: String): Int {
        return when (pageName) {
            "super" -> R.drawable.ic_super_admin
            "roles" -> R.drawable.ic_roles
            "users" -> R.drawable.ic_users
            "permissions" -> R.drawable.ic_permissions
            else -> R.drawable.ic_default_menu
        }
    }

    private fun getActionNameFromKey(key: String): String {
        return when {
            key.endsWith(".read") -> "목록 조회"
            key.endsWith(".create") -> "팀원 추가" 
            key.endsWith(".update") -> "정보 수정"
            key.endsWith(".delete") -> "삭제"
            else -> key.substringAfterLast(".")
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
