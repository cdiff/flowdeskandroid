package com.example.flowdesk_android.presentation.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.data.remote.dto.UserDto
import com.example.flowdesk_android.databinding.ItemUserBinding

class UserAdapter(private val onItemClick: (UserDto) -> Unit) :
    ListAdapter<UserDto, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onItemClick: (UserDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserDto) {
            binding.root.setOnClickListener { onItemClick(user) }

            binding.tvUserName.text = user.userName
            binding.tvUserEmail.text = user.userEmail
            
            // Set initial
            binding.tvAvatar.text = user.userName.firstOrNull()?.toString() ?: "?"
            val isManager = user.userEmail?.startsWith("ceo") == true
            if (isManager) {
                binding.tvRoleBadge.text = "관리자"
                binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_black)
                binding.tvRoleBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
            } else {
                binding.tvRoleBadge.text = "팀원"
                binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_gray_border)
                binding.tvRoleBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }

            // isActive = 1 means active
            if (user.isActive == 1) {
                binding.ivStatus.setImageResource(R.drawable.ic_check_circle)
                binding.ivStatus.imageTintList = ContextCompat.getColorStateList(binding.root.context, R.color.green_accent)
            } else {
                binding.ivStatus.setImageResource(R.drawable.ic_close)
                binding.ivStatus.imageTintList = ContextCompat.getColorStateList(binding.root.context, R.color.red)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserDto>() {
        override fun areItemsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
            return oldItem.userSeq == newItem.userSeq
        }

        override fun areContentsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
            return oldItem == newItem
        }
    }
}
