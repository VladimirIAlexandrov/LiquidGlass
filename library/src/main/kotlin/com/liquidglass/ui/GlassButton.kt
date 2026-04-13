package com.liquidglass.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.liquidglass.R

class GlassButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GlassView(context, attrs) {

    private var scaleOnPress = 0.95f
    private lateinit var textView: TextView

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onAttrsRead(context: Context, attrs: AttributeSet?) {
        textView = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            isSingleLine = true
        }
        addView(textView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val density = context.resources.displayMetrics.density
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GlassButton)
        scaleOnPress = ta.getFloat(R.styleable.GlassButton_glass_scale_on_press, 0.95f)
        val text = ta.getString(R.styleable.GlassButton_glass_text)
        if (text != null) textView.text = text
        textView.setTextColor(ta.getColor(R.styleable.GlassButton_glass_text_color, Color.WHITE))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            ta.getDimension(R.styleable.GlassButton_glass_text_size, 16 * density))
        ta.recycle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN   -> animateScale(scaleOnPress)
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> animateScale(1f)
        }
        return super.onTouchEvent(event)
    }

    private fun animateScale(to: Float) {
        animate().scaleX(to).scaleY(to)
            .setDuration(120)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun setText(text: String) { textView.text = text }
    fun setLabelColor(color: Int) { textView.setTextColor(color) }
    fun setTextSizePx(px: Float) { textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, px) }
}