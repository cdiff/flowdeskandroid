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
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.example.flowdesk_android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class ColorPickerBottomSheet(
    private val initialColor: String = "#3B82F6",
    private val onColorSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var colorPickerView: ColorPickerView
    private lateinit var hueSeekBar: SeekBar
    private lateinit var colorPreview: View
    private lateinit var etHex: EditText
    private lateinit var btnConfirm: Button

    private var currentHex = initialColor
    private var currentHue = 200f
    private var isProgrammaticChange = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_color_picker_bottom_sheet, container, false)

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorPickerView = view.findViewById(R.id.color_picker_view)
        hueSeekBar = view.findViewById(R.id.hue_seek_bar)
        colorPreview = view.findViewById(R.id.view_color_preview_picker)
        etHex = view.findViewById(R.id.et_hex_value)
        btnConfirm = view.findViewById(R.id.btn_confirm_color)

        // Parse initial color
        val parsedColor = runCatching { Color.parseColor(initialColor) }.getOrDefault(Color.parseColor("#3B82F6"))
        val hsv = FloatArray(3)
        Color.colorToHSV(parsedColor, hsv)
        currentHue = hsv[0]
        val initialSat = hsv[1]
        val initialVal = hsv[2]

        // Set up SeekBar
        hueSeekBar.max = 360
        hueSeekBar.progress = currentHue.toInt()

        // Generate initial S-V palette and apply
        colorPickerView.setPaletteDrawable(BitmapDrawable(resources, generateSVPalette(currentHue)))

        // Position selector at the initial color after layout pass
        colorPickerView.post {
            val x = (initialSat * colorPickerView.width).toInt()
            val y = ((1f - initialVal) * colorPickerView.height).toInt()
            colorPickerView.setSelectorPoint(x, y)
            updateColorUI(parsedColor)
        }

        // ColorPicker selection listener
        colorPickerView.setColorListener(ColorEnvelopeListener { envelope, fromUser ->
            if (fromUser) {
                updateColorUI(envelope.color)
            }
        })

        // Hue SeekBar change listener
        hueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val hue = progress.toFloat()
                    currentHue = hue

                    // Calculate color at current selector coordinates
                    val point = colorPickerView.getSelectedPoint()
                    val rx = if (colorPickerView.width > 0) point.x.toFloat() / colorPickerView.width else 0.5f
                    val ry = if (colorPickerView.height > 0) point.y.toFloat() / colorPickerView.height else 0.5f

                    val s = rx.coerceIn(0f, 1f)
                    val v = (1f - ry).coerceIn(0f, 1f)
                    val color = Color.HSVToColor(floatArrayOf(hue, s, v))

                    colorPickerView.setPaletteDrawable(BitmapDrawable(resources, generateSVPalette(hue)))
                    colorPickerView.setSelectorPoint(point.x, point.y)

                    updateColorUI(color)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Manual HEX EditText entry listener
        etHex.addTextChangedListener(object : TextWatcher {
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

        btnConfirm.setOnClickListener {
            onColorSelected(currentHex)
            dismiss()
        }
    }

    private fun generateSVPalette(hue: Float): Bitmap {
        val width = 100
        val height = 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        val hsv = floatArrayOf(hue, 0f, 0f)
        for (y in 0 until height) {
            val v = 1f - (y.toFloat() / (height - 1))
            for (x in 0 until width) {
                val s = x.toFloat() / (width - 1)
                hsv[1] = s
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
        if (etHex.text.toString().uppercase() != hexText) {
            isProgrammaticChange = true
            etHex.setText(hexText)
            isProgrammaticChange = false
        }

        val previewDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8 * resources.displayMetrics.density
            setColor(selectedColor)
        }
        colorPreview.background = previewDrawable
        btnConfirm.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

        if (triggerSelector) {
            val hsv = FloatArray(3)
            Color.colorToHSV(selectedColor, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val valVal = hsv[2]

            currentHue = hue
            hueSeekBar.progress = hue.toInt()

            colorPickerView.setPaletteDrawable(BitmapDrawable(resources, generateSVPalette(hue)))

            colorPickerView.post {
                val x = (sat * colorPickerView.width).toInt()
                val y = ((1f - valVal) * colorPickerView.height).toInt()
                colorPickerView.setSelectorPoint(x, y)
            }
        }
    }
}
