package com.liquidglass.ui

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class GlassHostLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var firstDrawDone = false

    override fun dispatchDraw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            super.dispatchDraw(canvas)
            return
        }

        val w = width; val h = height
        if (w <= 0 || h <= 0) { super.dispatchDraw(canvas); return }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != VISIBLE) continue

            val node = when (child) {
                is GlassPanel          -> child.sourceNode
                is GlassButton         -> child.sourceNode
                is GlassNavigationBar  -> child.sourceNode
                else                   -> null
            } ?: continue

            node.setPosition(0, 0, w, h)
            val rc = node.beginRecording(w, h)
            background?.draw(rc)
            drawChildrenExcept(rc, child)
            node.endRecording()

            when (child) {
                is GlassPanel         -> child.invalidateBlurPipeline(w, h)
                is GlassButton        -> child.invalidateBlurPipeline(w, h)
                is GlassNavigationBar -> child.invalidateBlurPipeline(w, h)
            }
        }

        super.dispatchDraw(canvas)

        if (!firstDrawDone) {
            firstDrawDone = true
            invalidate()
        }
    }

    private fun drawChildrenExcept(canvas: Canvas, skip: View) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child === skip || child.visibility != VISIBLE) continue
            if (child is GlassPanel || child is GlassButton || child is GlassNavigationBar) continue
            canvas.save()
            canvas.translate(child.left.toFloat(), child.top.toFloat())
            child.draw(canvas)
            canvas.restore()
        }
    }
}