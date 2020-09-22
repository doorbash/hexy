package ir.doorbash.hexy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SplashScreen implements Screen {
    private SpriteBatch batch;
    private Sprite splashSprite;
    private Texture splashTexture;

    SplashScreen() {
        super();
        batch = new SpriteBatch();
        splashTexture = new Texture(Gdx.files.internal("gfx/splash.png"), true);
        splashTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        splashSprite = new Sprite(splashTexture);
        splashSprite.setSize(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getWidth() / 2f);
        splashSprite.setCenter(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        splashSprite.draw(batch);
        batch.end();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {
        splashTexture.dispose();
        batch.dispose();
    }
}