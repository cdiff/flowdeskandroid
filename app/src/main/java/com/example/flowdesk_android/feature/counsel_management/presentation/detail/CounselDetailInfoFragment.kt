package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.FieldValueRequest
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselFieldValue
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

// 동적 뷰 메타데이터 파싱 태그용 Helper 클래스
private data class FieldTag(val fieldId: Int, val fieldType: String)

@AndroidEntryPoint
class CounselDetailInfoFragment : Fragment() {

    // 부모 Fragment(CounselDetailFragment)와 같은 ViewModel 공유
    private val viewModel: CounselDetailViewModel by viewModels({ requireParentFragment() })

    private lateinit var etUtmSource: TextInputEditText
    private lateinit var etUtmMedium: TextInputEditText
    private lateinit var etUtmCampaign: TextInputEditText
    private lateinit var etReserveTime: TextInputEditText
    private lateinit var etCounselMemo: TextInputEditText
    private lateinit var cardDynamicFields: View
    private lateinit var llDynamicFieldsContainer: LinearLayout
    private lateinit var btnSave: View

    private var selectedResvDtm: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_counsel_detail_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeViewModel()
        setupListeners()
    }

    private fun bindViews(view: View) {
        etUtmSource = view.findViewById(R.id.et_utm_source)
        etUtmMedium = view.findViewById(R.id.et_utm_medium)
        etUtmCampaign = view.findViewById(R.id.et_utm_campaign)
        etReserveTime = view.findViewById(R.id.et_reserve_time)
        etCounselMemo = view.findViewById(R.id.et_counsel_memo)
        cardDynamicFields = view.findViewById(R.id.card_dynamic_fields)
        llDynamicFieldsContainer = view.findViewById(R.id.ll_dynamic_fields_container)
        btnSave = view.findViewById(R.id.btn_save_info)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 상세 데이터로 필드 초기화
                launch {
                    viewModel.uiState.collect { state ->
                        if (state is CounselDetailUiState.Success) {
                            val d = state.detail
                            etUtmSource.setText(d.counselSource ?: "")
                            etUtmMedium.setText(d.counselMedium ?: "")
                            etUtmCampaign.setText(d.counselCampaign ?: "")
                            etCounselMemo.setText(d.counselMemo ?: "")
                            selectedResvDtm = d.counselResvDtm
                            etReserveTime.setText(d.counselResvDtm?.let { formatDisplay(it) } ?: "")

                            // 동적 필드 영역 렌더링
                            val fields = d.fieldValues
                            if (fields.isNotEmpty()) {
                                cardDynamicFields.visibility = View.VISIBLE
                                renderDynamicFields(fields)
                            } else {
                                cardDynamicFields.visibility = View.GONE
                            }
                        }
                    }
                }

                // 수정 결과 처리
                launch {
                    viewModel.updateState.collect { state ->
                        when (state) {
                            is CounselUpdateState.Success -> {
                                Toast.makeText(requireContext(), "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                viewModel.resetUpdateState()
                            }
                            is CounselUpdateState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetUpdateState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        etReserveTime.setOnClickListener {
            showDateTimePicker()
        }

        btnSave.setOnClickListener {
            // 동적 필드 정보 수집
            val dynamicFieldValues = mutableListOf<FieldValueRequest>()
            for (i in 0 until llDynamicFieldsContainer.childCount) {
                val textInputLayout = llDynamicFieldsContainer.getChildAt(i) as? TextInputLayout ?: continue
                val editText = textInputLayout.editText ?: continue
                val tag = editText.tag as? FieldTag ?: continue
                val rawVal = editText.text?.toString()?.trim()

                val req = when (tag.fieldType) {
                    "number" -> FieldValueRequest(
                        fieldId = tag.fieldId,
                        valueNumber = rawVal?.toDoubleOrNull()
                    )
                    "date" -> FieldValueRequest(
                        fieldId = tag.fieldId,
                        valueDate = rawVal?.ifBlank { null }
                    )
                    "datetime" -> FieldValueRequest(
                        fieldId = tag.fieldId,
                        valueDatetime = rawVal?.ifBlank { null }
                    )
                    else -> FieldValueRequest(
                        fieldId = tag.fieldId,
                        valueText = rawVal?.ifBlank { null }
                    )
                }
                dynamicFieldValues.add(req)
            }

            val request = CounselUpdateRequest(
                counselSource = etUtmSource.text?.toString()?.ifBlank { null },
                counselMedium = etUtmMedium.text?.toString()?.ifBlank { null },
                counselCampaign = etUtmCampaign.text?.toString()?.ifBlank { null },
                counselResvDtm = selectedResvDtm,
                counselMemo = etCounselMemo.text?.toString()?.ifBlank { null },
                fieldValues = dynamicFieldValues.ifEmpty { null }
            )
            viewModel.updateCounsel(request)
        }
    }

    private fun renderDynamicFields(fields: List<CounselFieldValue>) {
        llDynamicFieldsContainer.removeAllViews()
        val context = requireContext()
        val density = context.resources.displayMetrics.density
        val margin12 = (12 * density).toInt()

        fields.forEach { field ->
            val textInputLayout = TextInputLayout(context, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, margin12)
                }
                hint = field.label
                boxStrokeColor = Color.parseColor("#4285F4")
                setHintTextColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#4285F4")))
            }

            val textInputEditText = TextInputEditText(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 14f
                setTextColor(Color.parseColor("#1E293B"))
                tag = FieldTag(field.fieldId, field.fieldType)

                val rawValue = when (field.fieldType) {
                    "number" -> field.valueNumber?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                    "date" -> field.valueDate
                    "datetime" -> field.valueDatetime
                    else -> field.valueText
                }
                setText(rawValue ?: "")

                // 필드 타입별 입력 형식 처리
                when (field.fieldType) {
                    "number" -> {
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }
                    "date" -> {
                        inputType = android.text.InputType.TYPE_NULL
                        isFocusable = false
                        isClickable = true
                        setOnClickListener {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(context, { _, year, month, day ->
                                val dateStr = "%04d-%02d-%02d".format(year, month + 1, day)
                                setText(dateStr)
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    }
                    "datetime" -> {
                        inputType = android.text.InputType.TYPE_NULL
                        isFocusable = false
                        isClickable = true
                        setOnClickListener {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(context, { _, year, month, day ->
                                TimePickerDialog(context, { _, hour, minute ->
                                    val datetimeStr = "%04d-%02d-%02dT%02d:%02d:00".format(year, month + 1, day, hour, minute)
                                    setText(datetimeStr)
                                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    }
                    else -> {
                        inputType = android.text.InputType.TYPE_CLASS_TEXT
                    }
                }
            }

            textInputLayout.addView(textInputEditText)
            llDynamicFieldsContainer.addView(textInputLayout)
        }
    }

    private fun showDateTimePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val dtm = "%04d-%02d-%02dT%02d:%02d:00".format(year, month + 1, day, hour, minute)
                selectedResvDtm = dtm
                etReserveTime.setText(formatDisplay(dtm))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDisplay(iso: String): String {
        return try { "${iso.substring(0, 10)} ${iso.substring(11, 16)}" } catch (e: Exception) { iso }
    }
}
