package com.liquidglass.blur3

import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import androidx.annotation.RequiresApi

@RequiresApi(31)
internal class DownscaleScrollableNoiseSuppressor(private val density: Float) {

    companion object {
        const val DRAW_GLASS   = -2
        const val DRAW_FROSTED = -3
        private const val SIGMA = 0.57735f
        fun toSigma(r: Float)  = if (r > 0) SIGMA * r + 0.5f else 0f
        fun toRadius(s: Float) = if (s > 0.5f) (s - 0.5f) / SIGMA else 0f
        fun downscale(r: Float, scale: Float) = maxOf(1f, toRadius(toSigma(r) / scale))
        fun roundDown(v: Float, n: Int) = (v / n).toInt() * n
        fun roundUp(v: Float, n: Int)   = Math.ceil((v / n).toDouble()).toInt() * n
        fun satX2() = floatArrayOf(
            1.6f, -0.3f, -0.3f, 0f, 0f,
           -0.3f,  1.6f, -0.3f, 0f, 0f,
           -0.3f, -0.3f,  1.6f, 0f, 0f,
            0f,    0f,    0f,   1f, 0f
        )
    }

    inner class DownscaledRenderNode(name: String, subeffects: Int, notSimple: Boolean = false) {
        private val orig = RenderNode(null)
        private val down = Array(1 + subeffects) { RenderNode("${name}_d$it") }
        val restored: Array<RenderNode>
        private val simple: Boolean
        var scaleX = 1; var scaleY = 1
        var lastHash = 0L

        init {
            if (subeffects > 0 || notSimple) {
                restored = Array(1 + subeffects) { RenderNode(null) }
                simple = false
            } else {
                restored = down
                simple = true
            }
        }

        fun setEffect(e: RenderEffect) { down[0].setRenderEffect(e) }

        fun setBlur(r: Float, chain: RenderEffect? = null) {
            val rx = downscale(r, scaleX.toFloat())
            val ry = downscale(r, scaleY.toFloat())
            val blur = RenderEffect.createBlurEffect(rx, ry, Shader.TileMode.CLAMP)
            setEffect(if (chain != null) RenderEffect.createChainEffect(blur, chain) else blur)
        }

        fun invalidate(source: RenderNode) {
            val oW = source.width; val oH = source.height
            if (oW <= 0 || oH <= 0) return
            val dW = maxOf(1, oW / scaleX); val dH = maxOf(1, oH / scaleY)
            val sdX = dW.toFloat() / oW; val sdY = dH.toFloat() / oH
            val suX = oW.toFloat() / dW; val suY = oH.toFloat() / dH

            var hash = Blur3HashImpl.calcHash(source.uniqueId, dW.toLong())
            hash = Blur3HashImpl.calcHash(hash, dH.toLong())
            val force = !orig.hasDisplayList() || down.any { !it.hasDisplayList() } ||
                (!simple && restored.any { !it.hasDisplayList() })
            if (!force && lastHash == hash) return
            lastHash = hash

            orig.setPosition(0, 0, oW, oH)
            orig.beginRecording(oW, oH).also { c -> c.drawRenderNode(source); orig.endRecording() }

            down.forEachIndexed { i, dn ->
                dn.setPosition(0, 0, dW, dH)
                dn.beginRecording(dW, dH).also { c ->
                    if (i == 0) { c.scale(sdX, sdY); c.drawRenderNode(orig) }
                    else c.drawRenderNode(down[0])
                    dn.endRecording()
                }
                if (simple) {
                    dn.scaleX = suX; dn.scaleY = suY; dn.pivotX = 0f; dn.pivotY = 0f
                } else {
                    restored[i].setPosition(0, 0, oW, oH)
                    restored[i].beginRecording(oW, oH).also { c ->
                        c.scale(suX, suY); c.drawRenderNode(dn); restored[i].endRecording()
                    }
                }
            }
        }
    }

    private inner class Part {
        val node = RenderNode(null)
        var blurRadius = dp(8f)

        val glass = DownscaledRenderNode("glass", 0, true).apply {
            scaleX = 4; scaleY = 4
            setBlur(dp(1.66f), RenderEffect.createColorFilterEffect(
                ColorMatrixColorFilter(satX2())))
        }

        val blur = DownscaledRenderNode("blur", 0).apply {
            scaleX = 4; scaleY = 4
            setBlur(dp(8f))
        }

        val pos = Rect()
        var lastHash = 0L

        fun setPos(r: RectF) {
            pos.left   = roundDown(r.left,  16)
            pos.top    = roundDown(r.top,   16)
            pos.right  = roundUp(r.right,   16)
            pos.bottom = roundUp(r.bottom,  16)
        }

        fun updateBlur(radius: Float) {
            if (blurRadius == radius) return
            blurRadius = radius
            blur.setBlur(radius)
        }

        fun rebuild() {
            glass.invalidate(node)
            blur.invalidate(glass.restored[0])
        }
    }

    private val parts = ArrayList<Part>()
    private var count = 0
    private val resultNodes = Array(2) { RenderNode(null) }
    private var lastResultHash = 0L

    fun setupRenderNodes(positions: List<RectF>, n: Int) {
        count = n
        while (parts.size < count) parts.add(Part())
        repeat(count) { parts[it].setPos(positions[it]) }
    }

    fun setBlurRadius(radius: Float) {
        parts.forEach { it.updateBlur(radius) }
    }

    fun invalidateFromSourceNode(sourceNode: RenderNode, width: Int, height: Int): Boolean {
        var updated = 0
        repeat(count) { i ->
            val p = parts[i]
            val w = maxOf(1, p.pos.width())
            val h = maxOf(1, p.pos.height())

            val hash = Blur3HashImpl.calcHash(sourceNode.uniqueId, i.toLong())
            if (p.lastHash == hash && p.node.hasDisplayList()) return@repeat
            p.lastHash = hash

            p.node.setPosition(0, 0, w, h)
            val c = p.node.beginRecording(w, h)
            c.save()
            c.translate(-p.pos.left.toFloat(), -p.pos.top.toFloat())
            c.drawRenderNode(sourceNode)
            c.restore()
            p.node.endRecording()

            p.rebuild()
            updated++
        }
        return if (updated > 0) rebuildResult(width, height) else false
    }

    fun drawInline(canvas: Canvas, index: Int) {
        val ni = if (index == DRAW_GLASS) 0 else 1
        repeat(count) { b ->
            val p = parts[b]
            if (canvas.quickReject(p.pos.left.toFloat(), p.pos.top.toFloat(),
                    p.pos.right.toFloat(), p.pos.bottom.toFloat())) return@repeat
            canvas.save()
            canvas.translate(p.pos.left.toFloat(), p.pos.top.toFloat())
            canvas.drawRenderNode(resultNode(ni, b))
            canvas.restore()
        }
    }

    fun getVisiblePositions(out: MutableList<RectF>, startIndex: Int, expand: Int): Int {
        var c = 0
        repeat(count) { b ->
            val p = parts[b]
            val idx = startIndex + c
            val r = if (idx < out.size) out[idx] else RectF().also { out.add(it) }
            r.set(p.pos)
            r.inset(-expand.toFloat(), -expand.toFloat())
            c++
        }
        return c
    }

    private fun resultNode(ni: Int, bi: Int): RenderNode {
        val p = parts[bi]
        return if (ni == 0) p.glass.restored[0] else p.blur.restored[0]
    }

    private fun rebuildResult(width: Int, height: Int): Boolean {
        var hash = Blur3HashImpl.calcHash(width.toLong(), height.toLong())
        var force = false
        resultNodes.forEachIndexed { a, rn ->
            hash = Blur3HashImpl.calcHash(hash, rn.uniqueId)
            repeat(count) { b -> hash = Blur3HashImpl.calcHash(hash, resultNode(a, b).uniqueId) }
            if (!rn.hasDisplayList()) force = true
        }
        if (!force && hash == lastResultHash) return false
        lastResultHash = hash

        resultNodes.forEachIndexed { a, rn ->
            rn.setPosition(0, 0, width, height)
            val c = rn.beginRecording(width, height)
            repeat(count) { b ->
                val p = parts[b]
                c.save()
                c.translate(p.pos.left.toFloat(), p.pos.top.toFloat())
                c.drawRenderNode(resultNode(a, b))
                c.restore()
            }
            rn.endRecording()
        }
        return true
    }

    private fun dp(v: Float) = v * density
}
