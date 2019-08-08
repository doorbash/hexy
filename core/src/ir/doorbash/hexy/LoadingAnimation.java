package ir.doorbash.hexy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Milad Doorbash on 8/8/2019.
 */
public class LoadingAnimation implements Disposable {

    public static final int NUM_FRAMES = 8;
    public static final float FRAME_DURATION = 0.09f;

    public Animation<TextureRegion> animation;
    private Texture sheet;
    public float stateTime;

    public LoadingAnimation(String path) {
        sheet = new Texture(Gdx.files.internal(path), true);
        sheet.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / NUM_FRAMES, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[NUM_FRAMES];
        int index = 0;
        for (int j = 0; j < NUM_FRAMES; j++) {
            frames[index++] = tmp[0][j];
        }
        animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;
    }

    public void render(float dt, SpriteBatch batch, float x, float y, float width, float height) {
        stateTime += dt;
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, x, y, width, height);
    }

    @Override
    public void dispose() {
        sheet.dispose();
    }
}
