package com.liquidglass.blur3.drawable

import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.liquidglass.blur3.drawable.color.BlurredBackgroundColorProvider

internal abstract class BlurredBackgroundDrawable : Drawable() {

    var sourceOffsetX = 0f; var sourceOffsetY = 0f
    var _alpha = 255
    val radii = FloatArray(8)
    val paddedBounds = Rect()
    var liquidThickness = 0f
    var liquidIntensity = 0.75f
    var liquidIndex     = 1.5f

    protected var colorProvider: BlurredBackgroundColorProvider? = null
    var backgroundColor = 0; var shadowColor = 0
    var strokeColorTop  = 0; var strokeColorBottom = 0

    fun setSourceOffset(x: Float, y: Float) {
        if (sourceOffsetX != x || sourceOffsetY != y) {
            sourceOffsetX = x; sourceOffsetY = y
            onSourceOffsetChange()
        }
    }

    open fun onSourceOffsetChange() = dispatchPositionChange()

    override fun onBoundsChange(bounds: Rect) {
        paddedBounds.set(bounds)
        onBoundPropsChanged()
    }

    open fun onBoundPropsChanged() = dispatchPositionChange()

    fun setRadius(r: Float): BlurredBackgroundDrawable { radii.fill(r); onBoundPropsChanged(); return this }

    fun setColorProvider(p: BlurredBackgroundColorProvider): BlurredBackgroundDrawable {
        colorProvider = p; updateColors(); return this
    }

    open fun updateColors() {
        val p = colorProvider ?: return
        backgroundColor   = p.getBackgroundColor()
        shadowColor       = p.getShadowColor()
        strokeColorTop    = p.getStrokeColorTop()
        strokeColorBottom = p.getStrokeColorBottom()
    }

    fun getPositionRelativeSource(out: RectF) {
        out.set(paddedBounds); out.offset(sourceOffsetX, sourceOffsetY)
    }

    private val pos1 = RectF(); private val pos2 = RectF()
    private fun dispatchPositionChange() {
        getPositionRelativeSource(pos1)
        if (pos1 != pos2) { pos2.set(pos1); onSourceRelativePositionChanged(pos1) }
    }

    open fun onSourceRelativePositionChanged(position: RectF) {}

    override fun setAlpha(alpha: Int) { _alpha = alpha }
    override fun getAlpha() = _alpha
    override fun setColorFilter(cf: ColorFilter?) {}
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
