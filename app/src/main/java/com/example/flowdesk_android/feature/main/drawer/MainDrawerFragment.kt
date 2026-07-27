package com.example.flowdesk_android.feature.main.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentMainDrawerBinding
import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import com.example.flowdesk_android.feature.main.MainNavigator
import com.example.flowdesk_android.feature.main.MainUiState
import com.example.flowdesk_android.feature.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainDrawerFragment : Fragment() {

    private var _binding: FragmentMainDrawerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: MainDrawerAdapter

    private val navigator: MainNavigator?
        get() = activity as? MainNavigator

    private val drawerLayout: DrawerLayout?
        get() = activity?.findViewById(R.id.drawer_layout)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MainDrawerAdapter(
            onHeaderClick = { header ->
                viewModel.selectMenu(header.menu.pageName, header.hasSubItems)
                if (!header.hasSubItems) {
                    closeDrawer()
                    navigator?.navigateToTab(header.menu.pageName)
                }
            },
            onChevronClick = { header ->
                if (header.hasSubItems) {
                    viewModel.toggleExpand(header.menu.pageName)
                }
            },
            onSubItemClick = { subItem ->
                closeDrawer()
                viewModel.selectSubMenu(subItem.parentPageName, subItem.subId)
                navigator?.navigateToSubTab(subItem.parentPageName, subItem.subId, subItem.tabIndex)
            }
        )
        binding.rvDrawerMenuList.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        if (state is MainUiState.Success) {
                            setupHeader(state.data)
                        }
                    }
                }
                launch {
                    viewModel.drawerState.collect { drawerState ->
                        val rows = DrawerRowMapper.mapStateToDrawerRows(drawerState)
                        adapter.submitList(rows)
                    }
                }
            }
        }
    }

    private fun setupHeader(authInfo: AuthMeInfo) {
        val header = binding.layoutDrawerHeader
        header.tvDrawerUserName.text = authInfo.user.name.ifEmpty { "사용자" }
        header.tvDrawerUserEmail.text = authInfo.user.email

        header.btnCloseDrawer.setOnClickListener {
            closeDrawer()
        }

        header.btnDrawerMypage.setOnClickListener {
            closeDrawer()
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.myPageFragment)
        }

        header.btnDrawerLogout.setOnClickListener {
            closeDrawer()
            activity?.finish()
        }
    }

    private fun closeDrawer() {
        drawerLayout?.closeDrawer(GravityCompat.START)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
