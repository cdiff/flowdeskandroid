package com.example.flowdesk_android.feature.super_admin.presentation.actions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuperAdminActionsFragment : Fragment() {

    private val viewModel: ActionsViewModel by viewModels()

    private lateinit var rvActions: RecyclerView
    private lateinit var progressBar: View
    private lateinit var llEmpty: View
    private lateinit var etSearch: EditText
    private lateinit var btnCreateAction: View
    private lateinit var tvBadgeTotal: TextView
    private lateinit var tvBadgeActive: TextView
    private lateinit var tvBadgeInactive: TextView

    private lateinit var adapter: ActionAdapter

    private var bannerDismissed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_super_admin_actions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupBanner(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun bindViews(view: View) {
        rvActions       = view.findViewById(R.id.rv_actions)
        progressBar     = view.findViewById(R.id.progress_bar)
        llEmpty         = view.findViewById(R.id.ll_empty)
        etSearch        = view.findViewById(R.id.et_search)
        btnCreateAction = view.findViewById(R.id.btn_create_action)
        tvBadgeTotal    = view.findViewById(R.id.tv_badge_total)
        tvBadgeActive   = view.findViewById(R.id.tv_badge_active)
        tvBadgeInactive = view.findViewById(R.id.tv_badge_inactive)
    }

    private fun setupBanner(view: View) {
        val banner = view.findViewById<View>(R.id.banner_info)
        val btnClose = view.findViewById<View>(R.id.btn_close_banner)

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
                val label = if (action.isActive) "비활성화" else "활성화"
                AlertDialog.Builder(requireContext())
                    .setTitle("$label 확인")
                    .setMessage("'${action.displayName}' 액션을 ${label}하시겠습니까?")
                    .setPositiveButton(label) { _, _ ->
                        viewModel.toggleStatus(action)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            },
            onDeleteClick = { action ->
                AlertDialog.Builder(requireContext())
                    .setTitle("액션 삭제")
                    .setMessage("'${action.displayName}' 액션을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")
                    .setPositiveButton("삭제") { _, _ ->
                        viewModel.deleteAction(action.actionId)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        )
        rvActions.adapter = adapter
    }

    private fun setupListeners() {
        btnCreateAction.setOnClickListener {
            CreateActionBottomSheetFragment { viewModel.fetchActions() }
                .show(childFragmentManager, CreateActionBottomSheetFragment.TAG)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
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
                        progressBar.isVisible = state is ActionListUiState.Loading
                        llEmpty.isVisible     = state is ActionListUiState.Empty
                        rvActions.isVisible   = state is ActionListUiState.Success

                        if (state is ActionListUiState.Success) {
                            val all      = state.actions
                            val active   = all.count { it.isActive }
                            val inactive = all.count { !it.isActive }
                            tvBadgeTotal.text    = "총 ${all.size}개"
                            tvBadgeActive.text   = "활성 ${active}개"
                            tvBadgeInactive.text = "비활성 ${inactive}개"
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
                            is ActionListEvent.ActionCreated      -> Toast.makeText(context, "액션이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                            is ActionListEvent.ActionDeleted      -> Toast.makeText(context, "액션이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            is ActionListEvent.ActionStatusChanged-> Toast.makeText(context, "액션 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            is ActionListEvent.Error              -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
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
