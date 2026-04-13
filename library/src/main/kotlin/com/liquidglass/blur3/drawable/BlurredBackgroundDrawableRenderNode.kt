package com.liquidglass.blur3.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.RenderNode
import android.os.Build
import androidx.annotation.RequiresApi
import com.liquidglass.blur3.DownscaleScrollableNoiseSuppressor
import com.liquidglass.blur3.LiquidGlassEffect

@RequiresApi(31)
internal class BlurredBackgroundDrawableRenderNode(
    private val suppressor: DownscaleScrollableNoiseSuppressor
) : BlurredBackgroundDrawable() {

    private val renderNode     = RenderNode("BlurredNode").apply { setClipToBounds(true) }
    private val renderNodeFill = RenderNode("BlurredFill")
    private val strokePaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1f
    }

    private var liquidEffect: LiquidGlassEffect? = null
    private var dirty = true
    private val density = 2.75f

    @RequiresApi(33)
    fun setShaderCode(code: String) {
        liquidEffect = LiquidGlassEffect(renderNodeFill, code)
        dirty = true
    }

    override fun onBoundPropsChanged() {
        super.onBoundPropsChanged()
        if (!paddedBounds.isEmpty) {
            renderNodeFill.setPosition(0, 0, paddedBounds.width(), paddedBounds.height())
            renderNode.setPosition(0, 0, paddedBounds.width(), paddedBounds.height())
            dirty = true
        }
    }

    override fun onSourceOffsetChange() { super.onSourceOffsetChange(); dirty = true }
    override fun updateColors() { super.updateColors(); dirty = true }

    fun hasDisplayList() = renderNode.hasDisplayList()
    fun invalidate() { dirty = true }

    override fun draw(canvas: Canvas) {
        if (paddedBounds.isEmpty) return
        if (!canvas.isHardwareAccelerated) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(120, 200, 200, 200) }
            canvas.drawRoundRect(RectF(paddedBounds), radii[0], radii[0], p)
            return
        }
        if (!renderNode.hasDisplayList() || dirty) {
            buildDisplayList(); dirty = false
        }
        canvas.save()
        canvas.translate(paddedBounds.left.toFloat(), paddedBounds.top.toFloat())
        canvas.drawRenderNode(renderNode)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        super.setAlpha(alpha)
        renderNode.alpha = alpha / 255f
        dirty = true
    }

    private fun buildDisplayList() {
        val w = paddedBounds.width().toFloat()
        val h = paddedBounds.height().toFloat()

        if (Build.VERSION.SDK_INT >= 33) {
            liquidEffect?.update(
                0f, 0f, w, h,
                radii[0], radii[2], radii[4], radii[6],
                liquidThickness.takeIf { it > 0 } ?: (11f * density),
                liquidIntensity, liquidIndex, backgroundColor
            )
        }

        var c = renderNodeFill.beginRecording()
        c.save()
        c.translate(-sourceOffsetX, -sourceOffsetY)
        suppressor.drawInline(c, DownscaleScrollableNoiseSuppressor.DRAW_GLASS)
        c.restore()
        renderNodeFill.endRecording()

        c = renderNode.beginRecording()
        if (Color.alpha(backgroundColor) == 255) {
            c.drawColor(backgroundColor)
        } else {
            c.drawRenderNode(renderNodeFill)
            if (liquidEffect == null && Color.alpha(backgroundColor) != 0) {
                c.drawColor(backgroundColor)
            }
        }
        if (strokeColorTop != 0) {
            strokePaint.color = strokeColorTop
            c.drawRoundRect(0f, 0f, w, h, radii[0], radii[0], strokePaint)
        }
        renderNode.endRecording()
    }
}
