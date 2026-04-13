package com.liquidglass.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.liquidglass.R

class GlassNavigationBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GlassView(context, attrs) {

    private var selectedIndex  = 0
    var onTabSelected: ((Int) -> Unit)? = null

    private var activeColor    = Color.BLACK
    private var inactiveColor  = Color.argb(120, 0, 0, 0)
    private var indicatorColor = Color.argb(40, 0, 0, 0)
    private val textSize       = 12f


    private val tabs = mutableListOf<TabView>()



    private lateinit var tabContainer: LinearLayout
    private lateinit var indicator: View

    override fun onAttrsRead(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GlassNavigationBar)
        activeColor    = ta.getColor(R.styleable.GlassNavigationBar_glass_active_color, Color.BLACK)
        inactiveColor  = ta.getColor(R.styleable.GlassNavigationBar_glass_inactive_color, Color.argb(120, 0, 0, 0))
        indicatorColor = ta.getColor(R.styleable.GlassNavigationBar_glass_indicator_color, Color.argb(40, 0, 0, 0))
        ta.recycle()
    }

    override fun onGlassReady() {
        tabContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        indicator = View(context).apply {
            setBackgroundColor(indicatorColor)
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, view.height / 2f)
                }
            }
            clipToOutline = true
        }

        addView(indicator, LayoutParams(0, 0))
        addView(tabContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun addTab(@DrawableRes iconRes: Int, label: String) {
        val index = tabs.size
        val tab = TabView(context, iconRes, label, index == selectedIndex)
        tab.setOnClickListener { selectTab(index) }
        tabs.add(tab)
        tabContainer.addView(tab, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        if (index == selectedIndex) post { updateIndicator(index, false) }
    }

    fun selectTab(index: Int) {
        if (index == selectedIndex) return
        val prev = selectedIndex
        selectedIndex = index
        tabs.getOrNull(prev)?.setActive(false)
        tabs.getOrNull(index)?.setActive(true)
        updateIndicator(index, true)
        onTabSelected?.invoke(index)
    }

    fun setActiveColor(color: Int) {
        activeColor = color
        tabs.forEachIndexed { i, tab -> if (i == selectedIndex) tab.setColors(activeColor) }
    }

    fun setInactiveColor(color: Int) {
        inactiveColor = color
        tabs.forEachIndexed { i, tab -> if (i != selectedIndex) tab.setColors(inactiveColor) }
    }

    fun setIndicatorColor(color: Int) {
        indicatorColor = color
        indicator.setBackgroundColor(color)
    }

    // ── Indicator ─────────────────────────────────────────────────────────────

    private fun updateIndicator(index: Int, animate: Boolean) {
        if (tabs.isEmpty() || width == 0) return
        val tabW   = width / tabs.size
        val indicH = (height * 0.65f).toInt()
        val indicW = (tabW * 0.7f).toInt()
        val targetX = (tabW * index + (tabW - indicW) / 2).toFloat()
        val targetY = ((height - indicH) / 2).toFloat()

        if (animate) {
            ValueAnimator.ofFloat(indicator.x, targetX).apply {
                duration = 250
                interpolator = DecelerateInterpolator()
                addUpdateListener { indicator.x = it.animatedValue as Float }
                start()
            }
        } else {
            indicator.x = targetX
        }
        indicator.y = targetY
        indicator.layoutParams = indicator.layoutParams.also {
            it.width = indicW; it.height = indicH
        }
        indicator.requestLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateIndicator(selectedIndex, false)
    }

    // ── TabView ───────────────────────────────────────────────────────────────

    private inner class TabView(
        context: Context,
        @DrawableRes iconRes: Int,
        label: String,
        isActive: Boolean
    ) : LinearLayout(context) {

        private val icon = ImageView(context)
        private val text = TextView(context)

        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            val density = context.resources.displayMetrics.density

            icon.setImageResource(iconRes)
            icon.layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())

            text.text = label
            text.gravity = Gravity.CENTER
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            text.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.topMargin = (2 * density).toInt()
            }

            addView(icon)
            addView(text)
            setColors(if (isActive) activeColor else inactiveColor)
        }

        fun setActive(active: Boolean) {
            val targetColor = if (active) activeColor else inactiveColor
            ValueAnimator.ofArgb(text.currentTextColor, targetColor).apply {
                duration = 200
                addUpdateListener { setColors(it.animatedValue as Int) }
                start()
            }
        }

        fun setColors(color: Int) {
            icon.setColorFilter(color)
            text.setTextColor(color)
        }
    }
}