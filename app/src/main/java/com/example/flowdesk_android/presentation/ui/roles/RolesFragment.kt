package com.example.flowdesk_android.presentation.ui.roles

import android.os.Bundle
import com.example.flowdesk_android.presentation.ui.users.CreateRoleBottomSheetFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R
import com.example.flowdesk_android.presentation.viewmodel.RolesState
import com.example.flowdesk_android.presentation.viewmodel.RolesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RolesFragment : Fragment() {

    private val viewModel: RolesViewModel by viewModels()
    private lateinit var roleAdapter: RoleAdapter

    private lateinit var rvRoles: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var tvTotalCount: TextView
    private lateinit var tvActiveCount: TextView
    private lateinit var tvInactiveCount: TextView
    private lateinit var btnCreateRole: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_roles, container, false)

        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchRoles()

        return view
    }

    private fun initViews(view: View) {
        rvRoles = view.findViewById(R.id.rv_roles)
        progressBar = view.findViewById(R.id.progress_bar)
        etSearch = view.findViewById(R.id.et_search)
        tvTotalCount = view.findViewById(R.id.tv_total_count)
        tvActiveCount = view.findViewById(R.id.tv_active_count)
        tvInactiveCount = view.findViewById(R.id.tv_inactive_count)
        btnCreateRole = view.findViewById(R.id.btn_create_role)
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleAdapter(
            onManagePermissionsClick = { role ->
                Toast.makeText(requireContext(), "Manage permissions for ${role.displayName}", Toast.LENGTH_SHORT).show()
            },
            onEditRoleClick = { role ->
                Toast.makeText(requireContext(), "Edit role ${role.displayName}", Toast.LENGTH_SHORT).show()
            },
            onMoreOptionsClick = { role, view ->
                val popup = PopupMenu(requireContext(), view)
                popup.menu.add(0, 1, 0, if (role.isActive == 1) "비활성화" else "활성화")
                popup.menu.add(0, 2, 0, "삭제")
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            Toast.makeText(requireContext(), "Toggle status", Toast.LENGTH_SHORT).show()
                            true
                        }
                        2 -> {
                            Toast.makeText(requireContext(), "Delete clicked", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        )
        rvRoles.adapter = roleAdapter
    }

    private fun setupListeners() {
        btnCreateRole.setOnClickListener {
            val bottomSheet = CreateRoleBottomSheetFragment {
                viewModel.fetchRoles()
            }
            bottomSheet.show(childFragmentManager, CreateRoleBottomSheetFragment.TAG)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchRoles(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.rolesState.collect { state ->
                        when(state) {
                            is RolesState.Loading -> {
                                progressBar.visibility = View.VISIBLE
                                rvRoles.visibility = View.GONE
                            }
                            is RolesState.Success -> {
                                progressBar.visibility = View.GONE
                                rvRoles.visibility = View.VISIBLE
                                
                                val total = state.roles.size
                                val active = state.roles.count { it.isActive == 1 }
                                val inactive = state.roles.count { it.isActive == 0 }

                                tvTotalCount.text = "${total}개"
                                tvActiveCount.text = active.toString()
                                tvInactiveCount.text = inactive.toString()
                            }
                            is RolesState.Error -> {
                                progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.filteredRoles.collect { roles ->
                        roleAdapter.submitList(roles)
                    }
                }
            }
        }
    }
}
