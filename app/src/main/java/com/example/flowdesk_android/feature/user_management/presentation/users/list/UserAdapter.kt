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
import android.view.View
import android.transition.AutoTransition
import android.transition.TransitionManager

class UserAdapter(
    private val onItemClick: (User) -> Unit,
    private val onToggleStatusClick: (User) -> Unit,
    private val onDeleteUserClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    private val expandedUsers = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemUserCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.root.setOnClickListener { onItemClick(user) }

            binding.tvUserName.text = user.userName
            binding.tvUserEmail.text = user.userEmail

            // isActive is boolean
            if (user.isActive) {
                binding.tvActiveStatus.text = "• 활성"
                binding.tvActiveStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.color_success_active))
                binding.btnToggleStatus.text = "비활성화"
            } else {
                binding.tvActiveStatus.text = "• 비활성"
                binding.tvActiveStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.slate_400))
                binding.btnToggleStatus.text = "활성화"
            }

            // Hidden Menu Logic
            val isExpanded = expandedUsers.contains(user.userSeq)
            binding.llHiddenMenu.visibility = if (isExpanded) View.VISIBLE else View.GONE

            binding.ivMore.setOnClickListener {
                val isCurrentlyExpanded = expandedUsers.contains(user.userSeq)
                if (isCurrentlyExpanded) {
                    expandedUsers.remove(user.userSeq)
                } else {
                    expandedUsers.add(user.userSeq)
                }

                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }

                binding.llHiddenMenu.visibility = if (expandedUsers.contains(user.userSeq)) View.VISIBLE else View.GONE
            }

            binding.btnToggleStatus.setOnClickListener {
                onToggleStatusClick(user)
                expandedUsers.remove(user.userSeq)
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                binding.llHiddenMenu.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteUserClick(user)
                expandedUsers.remove(user.userSeq)
                val parent = itemView.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply { duration = 200 })
                }
                binding.llHiddenMenu.visibility = View.GONE
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
