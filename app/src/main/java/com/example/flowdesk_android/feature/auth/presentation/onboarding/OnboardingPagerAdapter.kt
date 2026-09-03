package com.example.flowdesk_android.feature.auth.presentation.onboarding

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.R

class OnboardingPagerAdapter : RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder>() {

    private val pages = listOf(
        PageData(R.layout.item_onboarding_page_1, R.string.onboarding_title_1, "실시간"),
        PageData(R.layout.item_onboarding_page_2, R.string.onboarding_title_2, "업무 공간"),
        PageData(R.layout.item_onboarding_page_3, R.string.onboarding_title_3, "완벽 관리"),
        PageData(R.layout.item_onboarding_page_4, R.string.onboarding_title_4, "지금 바로")
    )

    data class PageData(val layoutRes: Int, val titleRes: Int, val highlightWord: String)

    override fun getItemCount(): Int = pages.size

    override fun getItemViewType(position: Int): Int = pages[position].layoutRes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val page = pages[position]
        val tvTitle = holder.itemView.findViewById<TextView>(R.id.tv_title) ?: return
        val context = holder.itemView.context
        val fullText = context.getString(page.titleRes)

        val spannable = SpannableStringBuilder(fullText)
        val startIndex = fullText.indexOf(page.highlightWord)
        if (startIndex >= 0) {
            val endIndex = startIndex + page.highlightWord.length
            val highlightColor = Color.parseColor("#3B82F6")
            spannable.setSpan(
                ForegroundColorSpan(highlightColor),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        tvTitle.text = spannable
    }

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
