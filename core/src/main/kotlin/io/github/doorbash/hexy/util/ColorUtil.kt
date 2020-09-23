package io.github.doorbash.hexy.util

import com.badlogic.gdx.graphics.Color

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
object ColorUtil {
    private const val PLAYER_PROGRESSBAR_ALPHA = 0.5f
    private const val PATH_CELL_ALPHA_TINT = 0.4f
    val STROKE_COLORS = intArrayOf(
            -0xde690d,  // blue
            -0x86aab8,  // brown
            -0xb350b0,  // green
            -0x6800,  // orange
            -0x63d850,  // purple
            -0xbbcca,  // red
            -0xc0ae4b,  // indigo
            -0x3b3cc0,
            -0x61d1d2,
            -0x41c9b6,
            -0xff5a5d,
            -0x5ae076,
            -0xa46de0,
            -0x59b0f8,
            -0x4569cd,
            -0x57e9c0,
            -0xff7448,
            -0x2ed725,
            -0xeb6ab2,
            -0x4b38b4,
            -0xd15983,
            -0xb24dc6)
    val FILL_COLORS = intArrayOf(
            -0x913901,
            -0x567d8c,
            -0x7f1d82,
            -0x36b9,
            -0x2fa31d,
            -0x869f,
            -0x8a8218,
            -0x2ba,
            -0x2cacb,
            -0xa18b,
            -0xd30104,
            -0x1d32c,
            -0x6b00de,
            -0x67bc,
            -0x37ce,
            -0xe3a3,
            -0xec3a01,
            -0x99d01,
            -0xdd007b,
            -0x120077,
            -0x760029,
            -0x630077)

    fun getPlayerProgressColor(color: Color?): Color {
        val r = 1 - PLAYER_PROGRESSBAR_ALPHA + color!!.r * PLAYER_PROGRESSBAR_ALPHA
        val g = 1 - PLAYER_PROGRESSBAR_ALPHA + color.g * PLAYER_PROGRESSBAR_ALPHA
        val b = 1 - PLAYER_PROGRESSBAR_ALPHA + color.b * PLAYER_PROGRESSBAR_ALPHA
        return Color(r, g, b, 1.0f)
    }

    fun getPlayerPathCellColor(color: Color?): Color {
        val r = 1 - PATH_CELL_ALPHA_TINT + color!!.r * PATH_CELL_ALPHA_TINT
        val g = 1 - PATH_CELL_ALPHA_TINT + color.g * PATH_CELL_ALPHA_TINT
        val b = 1 - PATH_CELL_ALPHA_TINT + color.b * PATH_CELL_ALPHA_TINT
        return Color(r, g, b, 1.0f)
    }
}