package com.example.flowdesk_android.feature.super_admin.presentation.actions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.core.extension.showTopToast
import com.example.flowdesk_android.databinding.FragmentSuperAdminActionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuperAdminActionsFragment : Fragment() {

    private val viewModel: ActionsViewModel by viewModels()

    private var _binding: FragmentSuperAdminActionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ActionAdapter

    private var bannerDismissed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBanner(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBanner(view: View) {
        val banner = binding.bannerInfo
        val btnClose = binding.btnCloseBanner

        if (bannerDismissed) {
            banner.visibility = View.GONE
            return
        }

        btnClose.setOnClickListener {
            banner.animate()
                .alpha(0f)
                .translationY(-banner.height.toFloat())
                .setDuration(280)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        banner.visibility = View.GONE
                        banner.alpha = 1f
                        banner.translationY = 0f
                        bannerDismissed = true
                    }
                })
                .start()
        }
    }

    private fun setupRecyclerView() {
        adapter = ActionAdapter(
            onToggleStatusClick = { action ->
                val label = if (action.isActive) {
                    getString(R.string.label_status_inactive) + "화"
                } else {
                    getString(R.string.label_status_active) + "화"
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(label + " " + getString(R.string.label_action_confirm))
                    .setMessage(getString(R.string.action_status_toggle_confirm_message, action.displayName, label))
                    .setPositiveButton(label) { _, _ ->
                        viewModel.toggleStatus(action)
                    }
                    .setNegativeButton(getString(R.string.label_action_cancel), null)
                    .show()
            },
            onDeleteClick = { action ->
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.action_delete_title))
                    .setMessage(getString(R.string.action_delete_message, action.displayName))
                    .setPositiveButton(getString(R.string.label_action_delete)) { _, _ ->
                        viewModel.deleteAction(action.actionId)
                    }
                    .setNegativeButton(getString(R.string.label_action_cancel), null)
                    .show()
            }
        )
        binding.rvActions.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnCreateAction.setOnClickListener {
            CreateActionBottomSheet { viewModel.triggerRefresh() }
                .show(childFragmentManager, CreateActionBottomSheet.TAG)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is ActionListUiState.Loading
                        binding.llEmpty.isVisible     = state is ActionListUiState.Empty
                        binding.rvActions.isVisible   = state is ActionListUiState.Success

                        if (state is ActionListUiState.Success) {
                            val all      = state.actions
                            val active   = all.count { it.isActive }
                            val inactive = all.count { !it.isActive }
                            binding.tvBadgeTotal.text    = getString(R.string.label_status_count_total, all.size)
                            binding.tvBadgeActive.text   = getString(R.string.label_status_count_active, active)
                            binding.tvBadgeInactive.text = getString(R.string.label_status_count_inactive, inactive)
                        }
                    }
                }

                launch {
                    viewModel.filteredActions.collect { actions ->
                        adapter.submitList(actions)
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ActionListEvent.ActionCreated       -> showTopToast(getString(R.string.action_msg_created))
                            is ActionListEvent.ActionDeleted       -> showTopToast(getString(R.string.action_msg_deleted))
                            is ActionListEvent.ActionStatusChanged -> showTopToast(getString(R.string.action_msg_status_changed))
                            is ActionListEvent.Error               -> showTopToast(event.message)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = SuperAdminActionsFragment()
    }
}
