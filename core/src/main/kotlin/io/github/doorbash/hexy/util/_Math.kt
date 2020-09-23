package io.github.doorbash.hexy.util

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils

/**
 * Created by Milad Doorbash on 8/7/2019.
 */
object _Math {
    fun angleDistance(a: Float, b: Float): Float {
        var d = b - a
        while (d < -Math.PI) {
            d += 2 * Math.PI.toFloat()
        }
        while (d > Math.PI) {
            d -= 2 * Math.PI.toFloat()
        }
        return d
    }

    fun getCameraCurrentXYAngle(cam: OrthographicCamera): Float {
        return Math.atan2(cam.up.x.toDouble(), cam.up.y.toDouble()).toFloat() * MathUtils.radiansToDegrees
    }
}