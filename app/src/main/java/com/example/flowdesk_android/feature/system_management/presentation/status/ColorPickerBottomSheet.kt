package com.example.flowdesk_android.feature.system_management.presentation.status

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.DialogColorPickerBottomSheetBinding

class ColorPickerBottomSheet(
    private val initialColor: String = "#3B82F6",
    private val onColorSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogColorPickerBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var currentHex = initialColor
    private var currentHue = 200f
    private var isProgrammaticChange = false

    override fun getTheme(): Int = R.style.ColorPickerDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogColorPickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 색상 파싱
        val parsedColor = runCatching { Color.parseColor(initialColor) }
            .getOrDefault(Color.parseColor("#3B82F6"))
        val hsv = FloatArray(3)
        Color.colorToHSV(parsedColor, hsv)
        currentHue = hsv[0]
        val initialSat = hsv[1]
        val initialVal = hsv[2]

        // 7색 무지개 그라디언트를 코드로 직접 적용 (XML <item offset> 방식은 호환성 문제 있음)
        applyRainbowGradient()

        // SeekBar 설정
        binding.hueSeekBar.max = 360
        binding.hueSeekBar.progress = currentHue.toInt()

        // 초기 S-V 팔레트 적용
        binding.colorPickerView.setPaletteDrawable(
            BitmapDrawable(resources, generateSVPalette(currentHue))
        )

        // 레이아웃 완료 후 셀렉터 초기 위치 지정
        binding.colorPickerView.post {
            val x = (initialSat * binding.colorPickerView.width).toInt()
            val y = ((1f - initialVal) * binding.colorPickerView.height).toInt()
            binding.colorPickerView.setSelectorPoint(x, y)
            updateColorUI(parsedColor)
        }

        // 팔레트 터치 리스너
        binding.colorPickerView.setColorListener(
            com.skydoves.colorpickerview.listeners.ColorEnvelopeListener { envelope, fromUser ->
                if (fromUser) updateColorUI(envelope.color)
            }
        )

        // Hue SeekBar 리스너
        binding.hueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val hue = progress.toFloat()
                currentHue = hue

                val point = binding.colorPickerView.getSelectedPoint()
                val rx = if (binding.colorPickerView.width > 0)
                    point.x.toFloat() / binding.colorPickerView.width else 0.5f
                val ry = if (binding.colorPickerView.height > 0)
                    point.y.toFloat() / binding.colorPickerView.height else 0.5f

                val s = rx.coerceIn(0f, 1f)
                val v = (1f - ry).coerceIn(0f, 1f)
                val color = Color.HSVToColor(floatArrayOf(hue, s, v))

                binding.colorPickerView.setPaletteDrawable(
                    BitmapDrawable(resources, generateSVPalette(hue))
                )
                binding.colorPickerView.setSelectorPoint(point.x, point.y)
                updateColorUI(color)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // HEX 직접 입력 리스너
        binding.etHexValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isProgrammaticChange) return
                val text = s?.toString()?.trim() ?: ""
                if (text.length == 6) {
                    runCatching { Color.parseColor("#$text") }.getOrNull()?.let { color ->
                        updateColorUI(color, triggerSelector = true)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnCancelColor.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmColor.setOnClickListener {
            onColorSelected(currentHex)
            dismiss()
        }
    }

    /**
     * SeekBar 뒤 무지개 바를 GradientDrawable로 코드에서 직접 생성합니다.
     * XML <item offset> 방식은 일부 API 버전에서 렌더링 오류가 있습니다.
     */
    private fun applyRainbowGradient() {
        val colors = intArrayOf(
            Color.parseColor("#FF0000"), // 빨
            Color.parseColor("#FFFF00"), // 주/노
            Color.parseColor("#00FF00"), // 초
            Color.parseColor("#00FFFF"), // 청
            Color.parseColor("#0000FF"), // 남
            Color.parseColor("#FF00FF"), // 보
            Color.parseColor("#FF0000")  // 빨 (순환)
        )
        val rainbowDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors).apply {
            cornerRadius = 7 * resources.displayMetrics.density
        }
        binding.root.post {
            // FrameLayout 안 첫 번째 View(무지개 바)에 적용
            val rainbowBar = binding.hueSeekBar.parent
                .let { it as? android.view.ViewGroup }
                ?.getChildAt(0)
            rainbowBar?.background = rainbowDrawable
        }
    }

    /** HSV 팔레트 Bitmap 생성 (Saturation X축, Value Y축) */
    private fun generateSVPalette(hue: Float): Bitmap {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        val hsv = floatArrayOf(hue, 0f, 0f)
        for (y in 0 until height) {
            val v = 1f - (y.toFloat() / (height - 1))
            for (x in 0 until width) {
                hsv[1] = x.toFloat() / (width - 1)
                hsv[2] = v
                pixels[y * width + x] = Color.HSVToColor(hsv)
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun updateColorUI(selectedColor: Int, triggerSelector: Boolean = false) {
        currentHex = String.format("#%06X", 0xFFFFFF and selectedColor)

        val hexText = String.format("%06X", 0xFFFFFF and selectedColor)
        if (binding.etHexValue.text.toString().uppercase() != hexText) {
            isProgrammaticChange = true
            binding.etHexValue.setText(hexText)
            isProgrammaticChange = false
        }

        // 컬러 미리보기 원 업데이트
        val previewDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(selectedColor)
        }
        binding.viewColorPreviewPicker.background = previewDrawable

        if (triggerSelector) {
            val hsv = FloatArray(3)
            Color.colorToHSV(selectedColor, hsv)
            currentHue = hsv[0]
            binding.hueSeekBar.progress = hsv[0].toInt()
            binding.colorPickerView.setPaletteDrawable(
                BitmapDrawable(resources, generateSVPalette(hsv[0]))
            )
            binding.colorPickerView.post {
                val x = (hsv[1] * binding.colorPickerView.width).toInt()
                val y = ((1f - hsv[2]) * binding.colorPickerView.height).toInt()
                binding.colorPickerView.setSelectorPoint(x, y)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
