package com.example.flowdesk_android.feature.super_admin.presentation.pages

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
import com.example.flowdesk_android.databinding.FragmentSuperAdminPagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuperAdminPagesFragment : Fragment() {

    private val viewModel: PagesViewModel by viewModels()

    private var _binding: FragmentSuperAdminPagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PageAdapter

    private var bannerDismissed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperAdminPagesBinding.inflate(inflater, container, false)
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
        adapter = PageAdapter(
            onToggleStatusClick = { page ->
                val label = if (page.isActive) {
                    getString(R.string.label_status_inactive) + "화"
                } else {
                    getString(R.string.label_status_active) + "화"
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(label + " " + getString(R.string.label_action_confirm))
                    .setMessage(getString(R.string.page_status_toggle_confirm_message, page.displayName, label))
                    .setPositiveButton(label) { _, _ ->
                        viewModel.updatePageStatus(page, !page.isActive)
                    }
                    .setNegativeButton(getString(R.string.label_action_cancel), null)
                    .show()
            },
            onDeleteClick = { page ->
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.page_delete_title))
                    .setMessage(getString(R.string.page_delete_message, page.displayName))
                    .setPositiveButton(getString(R.string.label_action_delete)) { _, _ ->
                        viewModel.deletePage(page.pageId)
                    }
                    .setNegativeButton(getString(R.string.label_action_cancel), null)
                    .show()
            },
            onToggleParentClick = { page ->
                viewModel.toggleParent(page.pageId)
            },
            expandedParents = viewModel.expandedParents.value
        )
        binding.rvPages.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnCreatePage.setOnClickListener {
            CreatePageBottomSheet { viewModel.fetchPages() }
                .show(childFragmentManager, CreatePageBottomSheet.TAG)
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
                        binding.progressBar.isVisible = state is PageListUiState.Loading
                        binding.llEmpty.isVisible     = state is PageListUiState.Empty
                        binding.rvPages.isVisible     = state is PageListUiState.Success

                        if (state is PageListUiState.Success) {
                            val allPages = state.pages
                            val active   = allPages.count { it.isActive }
                            val inactive = allPages.count { !it.isActive }
                            binding.tvBadgeTotal.text   = getString(R.string.label_status_count_total, allPages.size)
                            binding.tvBadgeActive.text  = getString(R.string.label_status_count_active, active)
                            binding.tvBadgeInactive.text = getString(R.string.label_status_count_inactive, inactive)
                        }
                    }
                }

                launch {
                    viewModel.filteredPages.collect { pages ->
                        adapter.submitList(pages)
                    }
                }

                launch {
                    viewModel.expandedParents.collect { expanded ->
                        adapter.updateExpandedParents(expanded)
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is PageListEvent.PageCreated -> showTopToast(getString(R.string.page_msg_created))
                            is PageListEvent.PageDeleted -> showTopToast(getString(R.string.page_msg_deleted))
                            is PageListEvent.PageUpdated -> showTopToast(getString(R.string.page_msg_status_changed))
                            is PageListEvent.Error -> showTopToast(event.message)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = SuperAdminPagesFragment()
    }
}
