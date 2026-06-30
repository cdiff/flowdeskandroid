package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogRoleCopyPermissionsBinding
import com.example.flowdesk_android.databinding.ItemRoleCopyBinding
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RoleCopyBottomSheetFragment(
    private val roles: List<Role>,
    private val currentRoleId: Int,
    private val onRoleSelected: (Role) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogRoleCopyPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRoleCopyPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 정렬: 현재 선택된 id는 제외
        val filteredRoles = roles.filter { it.roleId != currentRoleId }

        binding.rvRoles.adapter = RoleCopyAdapter(filteredRoles) {
            onRoleSelected(it)
            dismiss()
        }
        binding.rvRoles.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.isShouldRemoveExpandedCorners = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class RoleCopyAdapter(
        private val items: List<Role>,
        private val onClick: (Role) -> Unit
    ) : RecyclerView.Adapter<RoleCopyAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemRoleCopyBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRoleCopyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val role = items[position]
            holder.binding.tvTitle.text = role.displayName
            holder.binding.tvDesc.text = role.description
            holder.binding.clMain.setOnClickListener { onClick(role) }
        }

        override fun getItemCount(): Int = items.size
    }
}
