package com.example.flowdesk_android.feature.user_management.presentation.users.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.user_management.domain.model.User
import com.example.flowdesk_android.databinding.ItemUserCardBinding

class UserAdapter(private val onItemClick: (User) -> Unit) :
    ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserCardBinding,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.root.setOnClickListener { onItemClick(user) }

            binding.tvUserName.text = user.userName
            binding.tvUserEmail.text = user.userEmail

            // isActive is boolean
            if (user.isActive) {
                binding.tvActiveStatus.text = "• 활성"
                binding.tvActiveStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.color_success_active))
            } else {
                binding.tvActiveStatus.text = "• 비활성"
                binding.tvActiveStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.slate_400))
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userSeq == newItem.userSeq
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
