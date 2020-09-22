package ir.doorbash.hexy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class SplashScreen internal constructor() : Screen {
    private val batch: SpriteBatch
    private val splashSprite: Sprite
    private val splashTexture: Texture
    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        splashSprite.draw(batch)
        batch.end()
    }

    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun dispose() {
        splashTexture.dispose()
        batch.dispose()
    }

    init {
        batch = SpriteBatch()
        splashTexture = Texture(Gdx.files.internal("gfx/splash.png"), true)
        splashTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        splashSprite = Sprite(splashTexture)
        splashSprite.setSize(Gdx.graphics.width / 2f, Gdx.graphics.width / 2f)
        splashSprite.setCenter(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
    }
}