package com.example.flowdesk_android.feature.counsel_management.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.flowdesk_android.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CounselEditBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: CounselListViewModel by viewModels({ requireParentFragment() })

    private var counselSeq: Int = -1
    private var initialName: String = ""
    private var initialPhone: String = ""

    override fun getTheme(): Int = R.style.FilterBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            counselSeq = it.getInt(ARG_COUNSEL_SEQ, -1)
            initialName = it.getString(ARG_NAME, "")
            initialPhone = it.getString(ARG_PHONE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_counsel_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.isShouldRemoveExpandedCorners = false
            }
        }

        val btnClose = view.findViewById<ImageView>(R.id.btn_close)
        val etName = view.findViewById<EditText>(R.id.et_name)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val etMemo = view.findViewById<EditText>(R.id.et_memo)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm)

        etName.setText(initialName)
        etPhone.setText(initialPhone)
        etMemo.setText("") // 목록에는 메모 정보가 없으므로 빈칸으로 노출

        btnClose.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val memo = etMemo.text.toString().trim().ifBlank { null }

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "이름과 전화번호는 필수 입력 항목입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateCounselInfo(counselSeq, name, phone, memo)
            dismiss()
        }
    }

    companion object {
        private const val ARG_COUNSEL_SEQ = "counsel_seq"
        private const val ARG_NAME = "name"
        private const val ARG_PHONE = "phone"

        fun newInstance(counselSeq: Int, name: String, phone: String): CounselEditBottomSheetFragment {
            return CounselEditBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COUNSEL_SEQ, counselSeq)
                    putString(ARG_NAME, name)
                    putString(ARG_PHONE, phone)
                }
            }
        }
    }
}
