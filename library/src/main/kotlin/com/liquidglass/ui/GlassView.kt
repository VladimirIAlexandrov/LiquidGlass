package com.liquidglass.ui

import android.content.Context
import android.graphics.Outline
import android.graphics.RectF
import android.graphics.RenderNode
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.liquidglass.R
import com.liquidglass.blur3.DownscaleScrollableNoiseSuppressor
import com.liquidglass.blur3.drawable.BlurredBackgroundDrawableRenderNode
import com.liquidglass.blur3.drawable.color.BlurredBackgroundColorProvider
import java.io.IOException

abstract class GlassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var suppressor: DownscaleScrollableNoiseSuppressor? = null
    private var glassDrawable: BlurredBackgroundDrawableRenderNode? = null

    val sourceNode: RenderNode? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) RenderNode(javaClass.simpleName)
        else null
    }

    private val positions = mutableListOf<RectF>()
    protected var ready = false

    private var fgColor      = 0x18FFFFFF
    private var radiusPx     = 0f
    private var blurPx       = 0f
    private var thicknessPx  = 0f
    private var intensity    = 0.75f

    init {
        setWillNotDraw(false)
        readAttrs(context, attrs)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) initGlass(context)
    }

    private fun readAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val density = context.resources.displayMetrics.density
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GlassView)
        radiusPx    = ta.getDimension(R.styleable.GlassView_glass_radius, 24 * density)
        blurPx      = ta.getDimension(R.styleable.GlassView_glass_blur, 8 * density)
        thicknessPx = ta.getDimension(R.styleable.GlassView_glass_thickness, 20 * density)
        intensity   = ta.getFloat(R.styleable.GlassView_glass_intensity, 0.75f)
        fgColor     = ta.getColor(R.styleable.GlassView_glass_foreground_color, 0x18FFFFFF)
        ta.recycle()
        onAttrsRead(context, attrs)
    }

    /** Override to read additional attributes in subclasses */
    protected open fun onAttrsRead(context: Context, attrs: AttributeSet?) {}

    @RequiresApi(31)
    private fun initGlass(context: Context) {
        val density = context.resources.displayMetrics.density

        suppressor = DownscaleScrollableNoiseSuppressor(density).also {
            if (blurPx > 0) it.setBlurRadius(blurPx)
        }

        glassDrawable = BlurredBackgroundDrawableRenderNode(suppressor!!).apply {
            setColorProvider(object : BlurredBackgroundColorProvider {
                override fun getBackgroundColor() = fgColor
            })
            setRadius(radiusPx)
            liquidThickness = thicknessPx
            liquidIntensity = intensity
        }

        if (Build.VERSION.SDK_INT >= 33) {
            loadShader(context)?.let { glassDrawable!!.setShaderCode(it) }
        }

        background = glassDrawable

        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
            }
        }

        ready = true
        onGlassReady()
    }

    /** Called when glass pipeline is initialized. Override for post-init setup. */
    protected open fun onGlassReady() {}

    // ── Called by GlassHostLayout ─────────────────────────────────────────────

    @RequiresApi(31)
    internal fun invalidateBlurPipeline(hostW: Int, hostH: Int) {
        if (!ready) return
        val node = sourceNode ?: return
        val sup  = suppressor  ?: return
        val drw  = glassDrawable ?: return

        drw.setSourceOffset(left.toFloat(), top.toFloat())
        rebuildPositions()
        sup.invalidateFromSourceNode(node, hostW, hostH)
        drw.invalidate()
        invalidate()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun setGlassRadius(px: Float) {
        radiusPx = px
        glassDrawable?.setRadius(px)
        invalidateOutline()
        invalidate()
    }

    fun setForegroundColor(color: Int) {
        fgColor = color
        glassDrawable?.updateColors()
        glassDrawable?.invalidate()
        invalidate()
    }

    fun setBlurRadius(px: Float) {
        blurPx = px
        suppressor?.setBlurRadius(px)
        glassDrawable?.invalidate()
        invalidate()
    }

    fun setThickness(px: Float) {
        thicknessPx = px
        glassDrawable?.liquidThickness = px
        glassDrawable?.invalidate()
        invalidate()
    }

    fun setIntensity(v: Float) {
        intensity = v
        glassDrawable?.liquidIntensity = v
        glassDrawable?.invalidate()
        invalidate()
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun rebuildPositions() {
        val sup = suppressor ?: return
        val drw = glassDrawable ?: return
        if (drw.paddedBounds.isEmpty) return
        positions.clear()
        val r = RectF()
        drw.getPositionRelativeSource(r)
        positions.add(r)
        sup.setupRenderNodes(positions, 1)
    }

    private fun loadShader(context: Context): String? = try {
        context.resources.openRawResource(R.raw.liquid_glass_shader)
            .use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (e: IOException) { null }
}