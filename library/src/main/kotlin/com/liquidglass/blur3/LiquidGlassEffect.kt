package com.liquidglass.blur3

import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import androidx.annotation.RequiresApi

@RequiresApi(33)
internal class LiquidGlassEffect(private val node: RenderNode, shaderCode: String) {

    private val shader = RuntimeShader(shaderCode)

    private var resX = 0f; private var resY = 0f
    private var cX   = 0f; private var cY   = 0f
    private var sX   = 0f; private var sY   = 0f
    private var rLT  = 0f; private var rRT  = 0f
    private var rRB  = 0f; private var rLB  = 0f
    private var thi  = 0f; private var inten = 0f; private var idx = 0f
    private var fgColor = Int.MIN_VALUE

    init {
        node.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "img"))
    }

    fun update(
        left: Float, top: Float, right: Float, bottom: Float,
        radiusLT: Float, radiusRT: Float, radiusRB: Float, radiusLB: Float,
        thickness: Float, intensity: Float, index: Float, foregroundColor: Int
    ) {
        val rX = node.width.toFloat()
        val rY = node.height.toFloat()
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val h  = bottom - top
        val sx = (right - left) / 2f
        val sy = h / 2f

        var lt = radiusLT; var lb = radiusLB
        var rt = radiusRT; var rb = radiusRB
        if (lt + lb > h) { val a = lt / (lt + lb); lt = h * a; lb = h * (1f - a) }
        if (rt + rb > h) { val a = rt / (rt + rb); rt = h * a; rb = h * (1f - a) }

        fun diff(a: Float, b: Float) = Math.abs(a - b) > 0.1f
        val changed = diff(resX,rX)||diff(resY,rY)||diff(cX,cx)||diff(cY,cy)||
            diff(sX,sx)||diff(sY,sy)||diff(rLT,lt)||diff(rRT,rt)||diff(rRB,rb)||diff(rLB,lb)||
            diff(thi,thickness)||diff(inten,intensity)||diff(idx,index)||fgColor!=foregroundColor
        if (!changed) return

        resX=rX; resY=rY; cX=cx; cY=cy; sX=sx; sY=sy
        rLT=lt; rRT=rt; rRB=rb; rLB=lb
        thi=thickness; inten=intensity; idx=index; fgColor=foregroundColor

        val a = Color.alpha(foregroundColor) / 255f
        val r = Color.red(foregroundColor)   / 255f * a
        val g = Color.green(foregroundColor) / 255f * a
        val b = Color.blue(foregroundColor)  / 255f * a

        shader.setFloatUniform("resolution", rX, rY)
        shader.setFloatUniform("center", cx, cy)
        shader.setFloatUniform("size", sx, sy)
        shader.setFloatUniform("radius", rb, rt, lb, lt)
        shader.setFloatUniform("thickness", thickness)
        shader.setFloatUniform("refract_intensity", intensity)
        shader.setFloatUniform("refract_index", index)
        shader.setFloatUniform("foreground_color_premultiplied", r, g, b, a)

        node.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "img"))
    }
}
