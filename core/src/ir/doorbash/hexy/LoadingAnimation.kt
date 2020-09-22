package ir.doorbash.hexy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable

/**
 * Created by Milad Doorbash on 8/8/2019.
 */
class LoadingAnimation(path: String?) : Disposable {
    var animation: Animation<TextureRegion>
    private val sheet: Texture
    var stateTime: Float
    fun render(dt: Float, batch: SpriteBatch?, x: Float, y: Float, width: Float, height: Float) {
        stateTime += dt
        val currentFrame = animation.getKeyFrame(stateTime, true)
        batch!!.draw(currentFrame, x, y, width, height)
    }

    override fun dispose() {
        sheet.dispose()
    }

    companion object {
        const val NUM_FRAMES = 8
        const val FRAME_DURATION = 0.09f
    }

    init {
        sheet = Texture(Gdx.files.internal(path), true)
        sheet.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        val tmp = TextureRegion.split(sheet, sheet.width / NUM_FRAMES, sheet.height)
        val frames = arrayOfNulls<TextureRegion>(NUM_FRAMES)
        var index = 0
        for (j in 0 until NUM_FRAMES) {
            frames[index++] = tmp[0][j]
        }
        animation = Animation(FRAME_DURATION, *frames)
        animation.playMode = Animation.PlayMode.LOOP
        stateTime = 0f
    }
}