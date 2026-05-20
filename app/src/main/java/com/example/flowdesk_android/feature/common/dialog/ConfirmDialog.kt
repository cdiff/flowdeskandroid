package com.example.flowdesk_android.feature.common.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 범용 확인 다이얼로그
 *
 * 사용 예시:
 * ```kotlin
 * ConfirmDialog.show(
 *     fragmentManager = childFragmentManager,
 *     title = "역할 삭제",
 *     message = "정말 삭제하시겠습니까?",
 *     confirmText = "삭제",
 *     cancelText = "취소",
 *     isDangerous = true
 * ) {
 *     viewModel.deleteRole(roleId)
 * }
 * ```
 */
class ConfirmDialog : DialogFragment() {

    var onConfirm: (() -> Unit)? = null

    companion object {
        private const val TAG = "ConfirmDialog"
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_CONFIRM = "confirmText"
        private const val ARG_CANCEL = "cancelText"
        private const val ARG_DANGEROUS = "isDangerous"

        fun show(
            fragmentManager: androidx.fragment.app.FragmentManager,
            title: String,
            message: String,
            confirmText: String = "확인",
            cancelText: String = "취소",
            isDangerous: Boolean = false,
            onConfirm: () -> Unit
        ) {
            ConfirmDialog().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_MESSAGE to message,
                    ARG_CONFIRM to confirmText,
                    ARG_CANCEL to cancelText,
                    ARG_DANGEROUS to isDangerous
                )
                this.onConfirm = onConfirm
            }.show(fragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val title = args.getString(ARG_TITLE, "")
        val message = args.getString(ARG_MESSAGE, "")
        val confirmText = args.getString(ARG_CONFIRM, "확인")
        val cancelText = args.getString(ARG_CANCEL, "취소")

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmText) { _, _ -> onConfirm?.invoke() }
            .setNegativeButton(cancelText, null)
            .create()
    }
}
