package com.liquidglass.blur3.drawable.color

internal interface BlurredBackgroundColorProvider {
    fun getBackgroundColor(): Int
    fun getShadowColor(): Int = 0
    fun getStrokeColorTop(): Int = 0
    fun getStrokeColorBottom(): Int = 0
}
