package io.github.doorbash.hexy.util

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4

/**
 * Created by Milad Doorbash on 8/31/2019.
 */
class DebugSpriteBatch : SpriteBatch() {
    override fun switchTexture(texture: Texture) {
        // int x = 1/0;
        try {
            val lastTextureField = javaClass.superclass.getDeclaredField("lastTexture")
            lastTextureField.isAccessible = true
            val lastTexture = lastTextureField[this] as Texture?
            if (lastTexture != null) {
                println("****** switching from $lastTexture to $texture")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.switchTexture(texture)
    }

    override fun setProjectionMatrix(projection: Matrix4) {
        println("setProjectionMatrix()")
        super.setProjectionMatrix(projection)
    }

    override fun flush() {
        try {
            val idxField = javaClass.superclass.getDeclaredField("idx")
            idxField.isAccessible = true
            if (idxField[this] as Int > 0) {
                println(">>>>>>>>>>>>> FLUSH >>>>>> renderCalls = " + (renderCalls + 1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.flush()
    }

    override fun end() {
        println("************* END ******************")
        super.end()
    }

    override fun begin() {
        println("************* BEGIN ******************")
        super.begin()
    }
}