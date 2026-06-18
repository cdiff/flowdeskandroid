package com.example.flowdesk_android.feature.system_management.presentation.status

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentStatusEditBinding
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.core.extension.showCustomDropdown
import com.example.flowdesk_android.core.extension.setReadOnly
import com.example.flowdesk_android.core.extension.setupFocusHighlight
import com.example.flowdesk_android.core.extension.updateColorIndicator
import com.example.flowdesk_android.core.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatusEditFragment : Fragment() {

    private val viewModel: TenantStatusViewModel by activityViewModels()

    private var tenantStatusId: Long = -1L
    private var defaultGroup: String? = null

    private var _binding: FragmentStatusEditBinding? = null
    private val binding get() = _binding!!

    // Validation 상태 제어용
    private var keyTouched = false
    private enum class ValidationState { NEUTRAL, VALID, ERROR }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tenantStatusId = it.getLong("tenantStatusId", -1L)
            defaultGroup = it.getString("defaultGroup")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Window insets 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setupFocusHighlights()
        setupColorPicker()
        setupGroupAutoComplete()
        setupListeners()
        
        viewModel.clearSelectedStatusDetail()
        setupDataBinding()
    }

    private fun setupFocusHighlights() {
        binding.etStatusGroup.setupFocusHighlight(binding.lineStatusGroup)
        binding.etStatusName.setupFocusHighlight(binding.lineStatusName)
        binding.etStatusDesc.setupFocusHighlight(binding.lineStatusDesc)
        binding.etStatusSort.setupFocusHighlight(binding.lineStatusSort)
    }

    private var groupPopup: ListPopupWindow? = null

    private fun setupGroupAutoComplete() {
        val groups = viewModel.statusGroups.value.toMutableList()
        groups.add("직접 입력")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_dropdown,
            android.R.id.text1,
            groups
        )

        binding.layoutGroupSelector.setOnClickListener {
            showGroupDropdown(adapter, groups)
        }

        binding.btnGroupInputReset.setOnClickListener {
            binding.etStatusGroup.setText("")
            binding.layoutGroupInput.visibility = View.GONE
            binding.layoutGroupSelector.visibility = View.VISIBLE
            binding.tvStatusGroupSelected.text = "그룹 선택"

            binding.etStatusGroup.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etStatusGroup.windowToken, 0)
        }
    }

    private fun showGroupDropdown(adapter: ArrayAdapter<String>, groups: List<String>) {
        groupPopup?.dismiss()
        groupPopup = binding.layoutGroupSelector.showCustomDropdown(adapter) { position ->
            val selected = groups[position]
            if (selected == "직접 입력") {
                binding.etStatusGroup.setText("")
                binding.layoutGroupSelector.visibility = View.GONE
                binding.layoutGroupInput.visibility = View.VISIBLE
                binding.etStatusGroup.requestFocus()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(binding.etStatusGroup, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            } else {
                binding.tvStatusGroupSelected.text = selected
                binding.tvStatusGroupSelected.setTextColor(Color.parseColor("#0F172A"))
                binding.etStatusGroup.setText(selected)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 상태 고유 키 포커싱 및 실시간 검증 리스너
        binding.etStatusKey.setOnFocusChangeListener { _, hasFocus ->
            binding.lineStatusKey.setBackgroundColor(
                Color.parseColor(if (hasFocus) "#3B82F6" else "#E2E8F0")
            )
            if (!hasFocus) {
                keyTouched = true
                validateKey(binding.etStatusKey.text.toString().trim())
            }
        }
        
        binding.etStatusKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (keyTouched) validateKey(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            performSave()
        }
    }

    private fun validateKey(text: String) {
        val state = when {
            text.isEmpty() -> ValidationState.NEUTRAL
            viewModel.validateStatusKey(text) -> ValidationState.VALID
            else -> ValidationState.ERROR
        }
        applyValidationState(binding.icValidKey, binding.tvHintKey, state)
    }

    private fun applyValidationState(icon: ImageView, hint: TextView, state: ValidationState) {
        val (iconColor, hintColor) = when (state) {
            ValidationState.VALID   -> Pair("#10B981", "#10B981")
            ValidationState.ERROR   -> Pair("#EF4444", "#EF4444")
            ValidationState.NEUTRAL -> Pair("#E2E8F0", "#94A3B8")
        }
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.parseColor(iconColor)))
        hint.setTextColor(Color.parseColor(hintColor))
    }

    private fun setupDataBinding() {
        if (tenantStatusId != -1L) {
            binding.tvTitle.visibility = View.GONE
            binding.btnSave.text = "저장하기"
            binding.tvHeaderTitle.visibility = View.VISIBLE
            binding.tvHeaderTitle.text = "상태 상세페이지"
            binding.scrollView.visibility = View.INVISIBLE
        } else {
            binding.tvTitle.text = "새로운 진행 단계를\n추가할 수 있어요."
            binding.tvHeaderTitle.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                        binding.btnSave.isEnabled = !loading
                    }
                }

                launch {
                    viewModel.selectedStatusDetail.collect { detail ->
                        if (tenantStatusId != -1L) {
                            if (detail != null) {
                                bindData(detail)
                                binding.scrollView.visibility = View.VISIBLE
                            }
                        } else {
                            bindData(null)
                        }
                    }
                }
            }
        }

        if (tenantStatusId != -1L) {
            viewModel.loadStatusDetail(tenantStatusId)
        } else {
            bindData(null)
        }
    }

    private fun bindData(detail: TenantStatus?) {
        if (detail != null) {
            binding.tvTitle.visibility = View.GONE
            binding.btnSave.text = "저장하기"
            binding.tvHeaderTitle.visibility = View.VISIBLE
            binding.tvHeaderTitle.text = "상태 상세페이지"

            binding.layoutStatusGroup.visibility = View.VISIBLE
            binding.layoutGroupSelector.setReadOnly(true, binding.btnGroupDropdown)
            binding.tvStatusGroupSelected.text = detail.statusGroup

            binding.layoutGroupInput.visibility = View.GONE
            binding.etStatusGroup.setText(detail.statusGroup)

            binding.layoutStatusKey.visibility = View.VISIBLE
            binding.etStatusKey.setText(detail.statusKey)
            binding.etStatusKey.setReadOnly(true, binding.icValidKey, binding.tvHintKey)

            binding.etStatusName.setText(detail.statusName)
            binding.etStatusDesc.setText(detail.description)
            binding.tvStatusColor.text = detail.color ?: "#3B82F6"
            binding.etStatusSort.setText(detail.sortOrder.toString())
            binding.cbStatusActive.isChecked = detail.isActive
            binding.viewColorPreview.updateColorIndicator(detail.color ?: "#3B82F6")

            val created = DateUtils.formatIsoDate(detail.createdAt)
            val updated = DateUtils.formatIsoDate(detail.updatedAt)
            binding.tvStatusDates.text = "등록일: $created   /   수정일: $updated"
            binding.tvStatusDates.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = "새로운 진행 단계를\n추가할 수 있어요."
            binding.tvTitle.visibility = View.VISIBLE
            binding.btnSave.text = "추가하기"
            binding.tvHeaderTitle.visibility = View.GONE

            binding.layoutStatusGroup.visibility = View.VISIBLE
            binding.layoutGroupSelector.setReadOnly(false, binding.btnGroupDropdown)

            binding.layoutStatusKey.visibility = View.VISIBLE
            binding.etStatusKey.setReadOnly(false, binding.icValidKey, binding.tvHintKey)

            binding.tvStatusDates.visibility = View.GONE

            binding.etStatusSort.setText("10")
            binding.tvStatusColor.text = "#3B82F6"
            binding.viewColorPreview.updateColorIndicator("#3B82F6")

            if (defaultGroup != null && defaultGroup != "all") {
                binding.tvStatusGroupSelected.text = defaultGroup
                binding.tvStatusGroupSelected.setTextColor(Color.parseColor("#0F172A"))
                binding.etStatusGroup.setText(defaultGroup)
            } else {
                binding.tvStatusGroupSelected.text = "그룹 선택"
                binding.tvStatusGroupSelected.setTextColor(Color.parseColor("#CBD5E1"))
                binding.etStatusGroup.setText("")
            }
        }
    }

    private fun performSave() {
        val group = binding.etStatusGroup.text.toString().trim()
        val name = binding.etStatusName.text.toString().trim()
        val desc = binding.etStatusDesc.text.toString().trim()
        val colorInput = binding.tvStatusColor.text.toString().trim()
        val sortStr = binding.etStatusSort.text.toString().trim()

        if (tenantStatusId == -1L && group.isEmpty()) {
            Toast.makeText(context, "업무 분류를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty()) {
            Toast.makeText(context, "상태 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!viewModel.validateColorHex(colorInput)) {
            Toast.makeText(context, "색상은 #RRGGBB 형식만 허용됩니다. (예: #3B82F6)", Toast.LENGTH_LONG).show()
            return
        }

        val sort = sortStr.toIntOrNull() ?: 10
        val isActive = binding.cbStatusActive.isChecked

        if (tenantStatusId != -1L) {
            viewModel.updateStatus(
                id = tenantStatusId,
                name = name,
                desc = desc,
                color = colorInput,
                sort = sort,
                isActive = isActive
            )
            Toast.makeText(context, "상담 상태가 수정되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        } else {
            val key = binding.etStatusKey.text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(context, "상태 고유 키(Key)를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            if (!viewModel.validateStatusKey(key)) {
                Toast.makeText(context, "상태 키는 영문 소문자, 숫자, 언더스코어(_)만 가능합니다.", Toast.LENGTH_LONG).show()
                return
            }

            if (viewModel.isDuplicateKey(group, key)) {
                Toast.makeText(context, "해당 그룹에 이미 동일한 상태 키가 존재합니다.", Toast.LENGTH_LONG).show()
                return
            }

            viewModel.createStatus(
                group = group,
                key = key,
                name = name,
                desc = desc,
                color = colorInput,
                sort = sort,
                isActive = isActive
            )
            Toast.makeText(context, "상담 상태가 추가되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun setupColorPicker() {
        binding.layoutColorSelector.setOnClickListener {
            val currentColor = binding.tvStatusColor.text.toString().trim().ifEmpty { "#3B82F6" }
            ColorPickerBottomSheet(
                initialColor = currentColor,
                onColorSelected = { hex ->
                    binding.tvStatusColor.text = hex
                    binding.viewColorPreview.updateColorIndicator(hex)
                }
            ).show(parentFragmentManager, "ColorPickerBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
