package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.TextView
import android.widget.ImageView

class RoleCopyBottomSheetFragment(
    private val roles: List<Role>,
    private val currentRoleId: Int,
    private val onRoleSelected: (Role) -> Unit
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_role_copy_permissions, container, false)
        val rvRoles = view.findViewById<RecyclerView>(R.id.rv_roles)
        
        // 정렬: 현재 선택된 id는 제외
        val filteredRoles = roles.filter { it.roleId != currentRoleId }

        rvRoles.adapter = RoleCopyAdapter(filteredRoles) {
            onRoleSelected(it)
            dismiss()
        }
        rvRoles.layoutManager = LinearLayoutManager(requireContext())
        
        return view
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.isShouldRemoveExpandedCorners = false
    }

    private class RoleCopyAdapter(
        private val items: List<Role>,
        private val onClick: (Role) -> Unit
    ) : RecyclerView.Adapter<RoleCopyAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tv_title)
            val tvDesc: TextView = view.findViewById(R.id.tv_desc)
            val clMain: View = view.findViewById(R.id.cl_main)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_role_copy, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val role = items[position]
            holder.tvTitle.text = role.displayName
            holder.tvDesc.text = role.description
            holder.clMain.setOnClickListener { onClick(role) }
        }

        override fun getItemCount(): Int = items.size
    }
}
