package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowdesk_android.R
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.FieldValueRequest
import com.example.flowdesk_android.core.extension.setReadOnly
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselFieldValue
import com.example.flowdesk_android.databinding.FragmentCounselDetailInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

// 동적 뷰 메타데이터 파싱 태그용 Helper 클래스
private data class FieldTag(val fieldId: Int, val fieldType: String)

@AndroidEntryPoint
class CounselDetailInfoFragment : Fragment() {

    // 부모 Fragment(CounselDetailFragment)와 같은 ViewModel 공유
    private val viewModel: CounselDetailViewModel by viewModels({ requireParentFragment() })

    // Binding
    private var _binding: FragmentCounselDetailInfoBinding? = null
    private val binding get() = _binding!!

    private var selectedResvDtm: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCounselDetailInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 상세 데이터로 필드 초기화
                launch {
                    viewModel.uiState.collect { state ->
                        if (state is CounselDetailUiState.Success) {
                            val d = state.detail
                            binding.etUtmSource.setText(d.counselSource ?: "")
                            binding.etUtmMedium.setText(d.counselMedium ?: "")
                            binding.etUtmCampaign.setText(d.counselCampaign ?: "")
                            binding.etCounselMemo.setText(d.counselMemo ?: "")
                            selectedResvDtm = d.counselResvDtm
                            binding.etReserveTime.setText(d.counselResvDtm?.let { formatDisplay(it) } ?: "")

                            // 동적 필드 영역 렌더링
                            val fields = d.fieldValues
                            if (fields.isNotEmpty()) {
                                binding.cardDynamicFields.visibility = View.VISIBLE
                                renderDynamicFields(fields)
                            } else {
                                binding.cardDynamicFields.visibility = View.GONE
                            }
                        }
                    }
                }

                // 수정 결과 처리
                launch {
                    viewModel.updateState.collect { state ->
                        when (state) {
                            is CounselUpdateState.Success -> {
                                Toast.makeText(requireContext(), getString(R.string.counsel_toast_info_updated), Toast.LENGTH_SHORT).show()
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
        binding.etReserveTime.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSaveInfo.setOnClickListener {
            val request = CounselUpdateRequest(
                counselSource = binding.etUtmSource.text?.toString()?.ifBlank { null },
                counselMedium = binding.etUtmMedium.text?.toString()?.ifBlank { null },
                counselCampaign = binding.etUtmCampaign.text?.toString()?.ifBlank { null },
                counselResvDtm = selectedResvDtm,
                counselMemo = binding.etCounselMemo.text?.toString()?.ifBlank { null },
                fieldValues = null
            )
            viewModel.updateCounsel(request)
        }
    }

    private fun renderDynamicFields(fields: List<CounselFieldValue>) {
        binding.llDynamicFieldsContainer.removeAllViews()
        val context = requireContext()
        val density = context.resources.displayMetrics.density
        val margin12 = (12 * density).toInt()

        fields.forEach { field ->
            val fieldContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, margin12)
                }
            }

            val labelView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = field.label
                textSize = 13f
                setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.slate_700))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding((4 * density).toInt(), 0, 0, (6 * density).toInt())
            }

            val editTextView = EditText(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (40 * density).toInt()
                )
                setBackgroundResource(R.drawable.bg_edit_text)
                setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)
                textSize = 14f
                setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.slate_800))
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
                        val calendarDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_calendar)?.mutate()
                        if (calendarDrawable != null) {
                            val sizePx = (18 * density).toInt()
                            calendarDrawable.setBounds(0, 0, sizePx, sizePx)
                            androidx.core.graphics.drawable.DrawableCompat.setTint(calendarDrawable, androidx.core.content.ContextCompat.getColor(context, R.color.slate_400))
                            setCompoundDrawables(null, null, calendarDrawable, null)
                        }
                    }
                    "datetime" -> {
                        inputType = android.text.InputType.TYPE_NULL
                        val calendarDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_calendar)?.mutate()
                        if (calendarDrawable != null) {
                            val sizePx = (18 * density).toInt()
                            calendarDrawable.setBounds(0, 0, sizePx, sizePx)
                            androidx.core.graphics.drawable.DrawableCompat.setTint(calendarDrawable, androidx.core.content.ContextCompat.getColor(context, R.color.slate_400))
                            setCompoundDrawables(null, null, calendarDrawable, null)
                        }
                    }
                    else -> {
                        inputType = android.text.InputType.TYPE_CLASS_TEXT
                    }
                }
                
                // 추가 정보 (동적 필드)는 수정 불가하므로 ReadOnly 처리
                setReadOnly(true)
            }

            fieldContainer.addView(labelView)
            fieldContainer.addView(editTextView)
            binding.llDynamicFieldsContainer.addView(fieldContainer)
        }
    }

    private fun showDateTimePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val dtm = "%04d-%02d-%02dT%02d:%02d:00".format(year, month + 1, day, hour, minute)
                selectedResvDtm = dtm
                binding.etReserveTime.setText(formatDisplay(dtm))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDisplay(iso: String): String {
        return try { "${iso.substring(0, 10)} ${iso.substring(11, 16)}" } catch (e: Exception) { iso }
    }
}
