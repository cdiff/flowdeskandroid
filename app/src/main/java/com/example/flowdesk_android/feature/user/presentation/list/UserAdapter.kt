package com.example.flowdesk_android.feature.user.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.domain.model.User
import com.example.flowdesk_android.databinding.UserItemCardBinding

class UserAdapter(private val onItemClick: (User) -> Unit) :
    ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: UserItemCardBinding,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.root.setOnClickListener { onItemClick(user) }

            binding.tvUserName.text = user.userName
            binding.tvUserEmail.text = user.userEmail
            
            binding.tvAvatar.text = user.userName.firstOrNull()?.toString() ?: "?"

            // isActive is boolean
            if (user.isActive) {
                binding.ivStatus.setImageResource(R.drawable.ic_check_circle)
                binding.ivStatus.imageTintList = ContextCompat.getColorStateList(binding.root.context, R.color.green_accent)
            } else {
                binding.ivStatus.setImageResource(R.drawable.ic_close)
                binding.ivStatus.imageTintList = ContextCompat.getColorStateList(binding.root.context, R.color.red)
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
