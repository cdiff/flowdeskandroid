package com.example.flowdesk_android.feature.system_management.presentation.status

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.core.extension.showCustomDropdown
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatusEditFragment : Fragment() {

    private val viewModel: TenantStatusViewModel by activityViewModels()

    private var tenantStatusId: Long = -1L
    private var defaultGroup: String? = null

    private lateinit var tvTitle: TextView
    private lateinit var layoutStatusGroup: View
    private lateinit var etGroup: EditText
    private lateinit var btnGroupDropdown: View
    private lateinit var layoutGroupSelector: View
    private lateinit var layoutGroupInput: View
    private lateinit var tvStatusGroupSelected: TextView
    private lateinit var btnGroupInputReset: View
    private lateinit var layoutStatusKey: View
    private lateinit var etKey: EditText
    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var tvColor: TextView
    private lateinit var tvColorPreview: View
    private lateinit var layoutColorSelector: View
    private lateinit var etSort: EditText
    private lateinit var cbActive: CheckBox
    private lateinit var btnBack: View
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var lineGroup: View
    private lateinit var lineKey: View
    private lateinit var lineName: View
    private lateinit var lineDesc: View
    private lateinit var lineSort: View

    private lateinit var icValidKey: ImageView

    // Validation Hint TextViews
    private lateinit var tvHintKey: TextView
    private lateinit var tvStatusDates: TextView

    // Validation regex
    private val keyRegex = Regex("^[a-z0-9_]+$")
    private val hexRegex = Regex("^#[0-9A-Fa-f]{6}$")

    // 한 번이라도 포커스를 잃었는지 추적 (Blur 후 실시간 검증 활성화용)
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
    ): View? = inflater.inflate(R.layout.fragment_status_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle window insets (status bar & camera notch overlap)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        bindViews(view)
        setupFocusHighlights()
        setupColorPicker()
        setupGroupAutoComplete()
        setupListeners()
        
        viewModel.clearSelectedStatusDetail()
        setupDataBinding()
    }

    private fun bindViews(view: View) {
        tvTitle = view.findViewById(R.id.tv_title)
        layoutStatusGroup = view.findViewById(R.id.layout_status_group)
        etGroup = view.findViewById(R.id.et_status_group)
        btnGroupDropdown = view.findViewById(R.id.btn_group_dropdown)
        layoutGroupSelector = view.findViewById(R.id.layout_group_selector)
        layoutGroupInput = view.findViewById(R.id.layout_group_input)
        tvStatusGroupSelected = view.findViewById(R.id.tv_status_group_selected)
        btnGroupInputReset = view.findViewById(R.id.btn_group_input_reset)
        layoutStatusKey = view.findViewById(R.id.layout_status_key)
        etKey = view.findViewById(R.id.et_status_key)
        etName = view.findViewById(R.id.et_status_name)
        etDesc = view.findViewById(R.id.et_status_desc)
        tvColor = view.findViewById(R.id.tv_status_color)
        tvColorPreview = view.findViewById(R.id.view_color_preview)
        layoutColorSelector = view.findViewById(R.id.layout_color_selector)
        etSort = view.findViewById(R.id.et_status_sort)
        cbActive = view.findViewById(R.id.cb_status_active)
        btnBack = view.findViewById(R.id.btn_back)
        btnSave = view.findViewById(R.id.btn_save)
        progressBar = view.findViewById(R.id.progress_bar)

        // Focus lines
        lineGroup = view.findViewById(R.id.line_status_group)
        lineKey = view.findViewById(R.id.line_status_key)
        lineName = view.findViewById(R.id.line_status_name)
        lineDesc = view.findViewById(R.id.line_status_desc)
        lineSort = view.findViewById(R.id.line_status_sort)

        // Validation icons
        icValidKey = view.findViewById(R.id.ic_valid_key)

        // Validation hint TextViews
        tvHintKey = view.findViewById(R.id.tv_hint_key)
        tvStatusDates = view.findViewById(R.id.tv_status_dates)
    }

    private fun setupFocusHighlights() {
        setupFocusHighlight(etGroup, lineGroup)
        // etKey, etColor는 setupListeners에서 validation과 함께 처리
        setupFocusHighlight(etName, lineName)
        setupFocusHighlight(etDesc, lineDesc)
        setupFocusHighlight(etSort, lineSort)
    }

    private fun setupFocusHighlight(editText: EditText, lineView: View) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            lineView.setBackgroundColor(
                if (hasFocus) Color.parseColor("#3B82F6") else Color.parseColor("#E2E8F0")
            )
        }
    }

    private var groupPopup: ListPopupWindow? = null

    private fun setupGroupAutoComplete() {
        // ViewModel에 있는 기존 statusGroups을 로드하여 자동완성 어댑터 적용
        val groups = viewModel.statusGroups.value.toMutableList()

        // 마지막에 "직접 입력" 추가
        groups.add("직접 입력")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_dropdown,
            android.R.id.text1,
            groups
        )

        // 클릭하거나 화살표 누를 때, 스피너처럼 항상 드롭다운이 뜨도록 구성
        layoutGroupSelector.setOnClickListener {
            showGroupDropdown(adapter, groups)
        }

        btnGroupInputReset.setOnClickListener {
            etGroup.setText("")
            layoutGroupInput.visibility = View.GONE
            layoutGroupSelector.visibility = View.VISIBLE
            tvStatusGroupSelected.text = "그룹 선택"

            // 키보드 닫기
            etGroup.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(etGroup.windowToken, 0)
        }
    }

    private fun showGroupDropdown(adapter: ArrayAdapter<String>, groups: List<String>) {
        groupPopup?.dismiss()
        groupPopup = layoutGroupSelector.showCustomDropdown(adapter) { position ->
            val selected = groups[position]
            if (selected == "직접 입력") {
                // "직접 입력" 선택 시 기존 입력 비우고 입력 레이아웃 전환 & 키보드 활성화
                etGroup.setText("")
                layoutGroupSelector.visibility = View.GONE
                layoutGroupInput.visibility = View.VISIBLE
                etGroup.requestFocus()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(etGroup, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            } else {
                // 일반 그룹 선택 시 텍스트뷰와 에디트텍스트 모두에 값을 세팅
                tvStatusGroupSelected.text = selected
                etGroup.setText(selected)
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // ── 상태 고유 키: 포커스 아웃 시 검증, 이후 재진입하면 실시간 검증 ──
        etKey.setOnFocusChangeListener { _, hasFocus ->
            lineKey.setBackgroundColor(
                if (hasFocus) Color.parseColor("#3B82F6") else Color.parseColor("#E2E8F0")
            )
            if (!hasFocus) {
                keyTouched = true
                validateKey(etKey.text.toString().trim())
            }
        }
        etKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 한 번이라도 포커스를 잃은 적 있으면 실시간 검증 (에러 교정 피드백)
                if (keyTouched) validateKey(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSave.setOnClickListener {
            performSave()
        }
    }

    private fun validateKey(text: String) {
        val state = when {
            text.isEmpty() -> ValidationState.NEUTRAL
            keyRegex.matches(text) -> ValidationState.VALID
            else -> ValidationState.ERROR
        }
        applyValidationState(icValidKey, tvHintKey, state)
    }



    /** NEUTRAL: 회색 (입력 전 or 포커스 중)
     *  VALID:   초록색 (조건 충족)
     *  ERROR:   빨간색 (포커스 잃은 후 조건 불충족) */
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { loading ->
                        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                        btnSave.isEnabled = !loading
                    }
                }

                launch {
                    viewModel.selectedStatusDetail.collect { detail ->
                        if (tenantStatusId != -1L) {
                            if (detail != null) {
                                bindData(detail)
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
            tvTitle.visibility = View.GONE
            btnSave.text = "저장하기"

            // 수정 모드에서는 그룹 및 고유키 수정 불가하므로 비활성화 노출
            layoutStatusGroup.visibility = View.VISIBLE
            layoutGroupSelector.setReadOnly(true, btnGroupDropdown)
            tvStatusGroupSelected.text = detail.statusGroup

            layoutGroupInput.visibility = View.GONE
            etGroup.setText(detail.statusGroup)

            layoutStatusKey.visibility = View.VISIBLE
            etKey.setText(detail.statusKey)
            etKey.setReadOnly(true, icValidKey, tvHintKey)

            etName.setText(detail.statusName)
            etDesc.setText(detail.description)
            tvColor.text = detail.color ?: "#3B82F6"
            etSort.setText(detail.sortOrder.toString())
            cbActive.isChecked = detail.isActive
            updateColorIndicator(detail.color ?: "#3B82F6")

            // 등록일 / 수정일 노출
            val created = formatIsoDate(detail.createdAt)
            val updated = formatIsoDate(detail.updatedAt)
            tvStatusDates.text = "등록일: $created   /   수정일: $updated"
            tvStatusDates.visibility = View.VISIBLE
        } else {
            tvTitle.text = "상담 상태를\n새로 추가할 수 있어요."
            tvTitle.visibility = View.VISIBLE
            btnSave.text = "추가하기"

            layoutStatusGroup.visibility = View.VISIBLE
            layoutGroupSelector.setReadOnly(false, btnGroupDropdown)
            tvStatusGroupSelected.setTextColor(Color.parseColor("#0F172A"))

            layoutStatusKey.visibility = View.VISIBLE
            etKey.setReadOnly(false, icValidKey, tvHintKey)

            tvStatusDates.visibility = View.GONE

            etSort.setText("10")
            tvColor.text = "#3B82F6" // Default blue
            updateColorIndicator("#3B82F6")

            if (defaultGroup != null && defaultGroup != "all") {
                tvStatusGroupSelected.text = defaultGroup
                etGroup.setText(defaultGroup)
            } else {
                tvStatusGroupSelected.text = "그룹 선택"
                etGroup.setText("")
            }
        }
    }

    private fun View.setReadOnly(isReadOnly: Boolean, vararg siblingViews: View) {
        this.isEnabled = !isReadOnly
        this.isClickable = !isReadOnly
        this.isFocusable = !isReadOnly

        if (this is EditText) {
            this.setTextColor(Color.parseColor(if (isReadOnly) "#94A3B8" else "#0F172A"))
        } else if (this is TextView) {
            this.setTextColor(Color.parseColor(if (isReadOnly) "#94A3B8" else "#0F172A"))
        }

        siblingViews.forEach { sibling ->
            sibling.visibility = if (isReadOnly) View.GONE else View.VISIBLE
        }
    }

    private fun formatIsoDate(isoStr: String?): String {
        if (isoStr.isNullOrBlank()) return ""
        return try {
            val instant = java.time.Instant.parse(isoStr)
            val zoneId = java.time.ZoneId.systemDefault()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            instant.atZone(zoneId).format(formatter)
        } catch (e: Exception) {
            isoStr.replace("T", " ").substringBefore(".")
        }
    }

    private fun performSave() {
        val group = etGroup.text.toString().trim()
        val name = etName.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val colorInput = tvColor.text.toString().trim()
        val sortStr = etSort.text.toString().trim()

        // 1. 공통 유효성 검사
        if (tenantStatusId == -1L && group.isEmpty()) {
            Toast.makeText(context, "상태 그룹을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty()) {
            Toast.makeText(context, "상태 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 색상 HEX 형식 유효성 체크 (#RRGGBB) — 클래스 멤버 hexRegex 재사용
        if (!hexRegex.matches(colorInput)) {
            Toast.makeText(context, "색상은 #RRGGBB 형식만 허용됩니다. (예: #3B82F6)", Toast.LENGTH_LONG).show()
            return
        }

        val sort = sortStr.toIntOrNull() ?: 10
        val isActive = cbActive.isChecked

        val existingStatusesList = viewModel.filteredGroups.value.flatMap { it.items }

        if (tenantStatusId != -1L) {
            // Edit Mode
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
            // Add Mode
            val key = etKey.text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(context, "상태 고유 키(Key)를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            // 3. 상태 키 유효성 검사 (영문 소문자, 숫자, 언더스코어) — 클래스 멤버 keyRegex 재사용
            if (!keyRegex.matches(key)) {
                Toast.makeText(context, "상태 키는 영문 소문자, 숫자, 언더스코어(_)만 가능합니다.", Toast.LENGTH_LONG).show()
                return
            }

            // 4. 중복 체크: group + key 조합이 유니크해야 함
            val isDuplicate = existingStatusesList.any {
                it.statusGroup.equals(group, ignoreCase = true) &&
                it.statusKey.equals(key, ignoreCase = true)
            }

            if (isDuplicate) {
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
        // 컬러 선택 레이아웃 클릭 시 ColorPickerBottomSheet 오픈
        layoutColorSelector.setOnClickListener {
            val currentColor = tvColor.text.toString().trim().ifEmpty { "#3B82F6" }
            ColorPickerBottomSheet(
                initialColor = currentColor,
                onColorSelected = { hex ->
                    tvColor.text = hex
                    updateColorIndicator(hex)
                }
            ).show(parentFragmentManager, "ColorPickerBottomSheet")
        }
    }

    private fun updateColorIndicator(hexColor: String) {
        try {
            val parsedColor = Color.parseColor(hexColor)
            val density = resources.displayMetrics.density
            val radius = 6 * density // 6dp
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radius
                setColor(parsedColor)
            }
            tvColorPreview.background = drawable
        } catch (e: Exception) {
            // 에러 시 둥근 사각형으로 파란색을 기본으로 그림
            val density = resources.displayMetrics.density
            val radius = 6 * density
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radius
                setColor(Color.parseColor("#3B82F6"))
            }
            tvColorPreview.background = drawable
        }
    }
}
