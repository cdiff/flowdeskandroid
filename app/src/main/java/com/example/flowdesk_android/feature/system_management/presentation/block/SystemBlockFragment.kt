package com.example.flowdesk_android.feature.system_management.presentation.block

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentSystemBlockBinding
import com.example.flowdesk_android.feature.system_management.presentation.block.ip.BlockIpFragment
import com.example.flowdesk_android.feature.system_management.presentation.block.keyword.BlockKeywordFragment
import com.example.flowdesk_android.feature.system_management.presentation.block.phone.BlockPhoneFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SystemBlockFragment : Fragment() {

    private var _binding: FragmentSystemBlockBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = BlockPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = true // Enable swiping between tabs

        binding.cardTabIp.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.cardTabPhone.setOnClickListener { binding.viewPager.currentItem = 1 }
        binding.cardTabKeyword.setOnClickListener { binding.viewPager.currentItem = 2 }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectTabCard(position)
            }
        })
    }

    private fun selectTabCard(position: Int) {
        val ctx = requireContext()

        val selected = TabStyle(
            stroke = ContextCompat.getColor(ctx, R.color.brand_primary),
            bg = ContextCompat.getColor(ctx, R.color.bg_card_selected),
            iconTint = ContextCompat.getColor(ctx, R.color.brand_primary),
            textColor = ContextCompat.getColor(ctx, R.color.brand_primary),
            weight = 2f
        )

        val unselected = TabStyle(
            stroke = ContextCompat.getColor(ctx, R.color.tab_unselected_stroke),
            bg = ContextCompat.getColor(ctx, R.color.bg_card_unselected),
            iconTint = ContextCompat.getColor(ctx, R.color.text_hint),
            textColor = ContextCompat.getColor(ctx, R.color.text_tertiary),
            weight = 1f
        )

        val cards = listOf(binding.cardTabIp, binding.cardTabPhone, binding.cardTabKeyword)
        val icons = listOf(binding.ivTabIpIcon, binding.ivTabPhoneIcon, binding.ivTabKeywordIcon)
        val titles = listOf(binding.tvTabIpTitle, binding.tvTabPhoneTitle, binding.tvTabKeywordTitle)

        val llTabs = binding.llCardTabs
        TransitionManager.beginDelayedTransition(llTabs)

        for (i in cards.indices) {
            val style = if (i == position) selected else unselected

            val params = cards[i].layoutParams as LinearLayout.LayoutParams
            params.weight = style.weight
            cards[i].layoutParams = params

            cards[i].strokeColor = style.stroke
            cards[i].setCardBackgroundColor(ColorStateList.valueOf(style.bg))

            icons[i].imageTintList = ColorStateList.valueOf(style.iconTint)
            titles[i].setTextColor(style.textColor)
        }
    }

    data class TabStyle(
        val stroke: Int,
        val bg: Int,
        val iconTint: Int,
        val textColor: Int,
        val weight: Float
    )

    private inner class BlockPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> BlockIpFragment()
                1 -> BlockPhoneFragment()
                2 -> BlockKeywordFragment()
                else -> Fragment()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
