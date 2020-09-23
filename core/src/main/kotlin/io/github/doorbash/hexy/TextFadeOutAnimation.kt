package io.github.doorbash.hexy

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Interpolation.PowIn

/**
 * Created by Milad Doorbash on 10/16/2019.
 */
class TextFadeOutAnimation(private val text: String, color: Color, private val x: Float, private var y: Float) {
    private val color: Color
    private var timer = 0f
    var stop = false
    fun draw(batch: SpriteBatch?, dt: Float, font: BitmapFont?) {
        if (stop) return
        timer += dt
        font!!.color = color
        font.draw(batch, text, x, y)
        color[color.r, color.g, color.b] = 1 - interpolation.apply(timer)
        y += dt * Y_SPEED
        if (color.a < 0.1f) stop = true
    }

    companion object {
        private val interpolation: Interpolation = PowIn(3)
        private const val Y_SPEED = 20f
    }

    init {
        this.color = color.cpy()
    }
}