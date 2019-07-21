package ir.doorbash.hexy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Game extends ApplicationAdapter {

    SpriteBatch batch;
    OrthographicCamera camera;
    Viewport viewport;
    TextureAtlas hexAtlas;
    Sprite white_hex;
    Texture whiteHex;
    FrameBuffer fbo;
    Sprite tiles;

    private Vector2 position = new Vector2();
    private float actualWidth;
    private float actualHeight;

    @Override
    public void create() {
        batch = new SpriteBatch();
        whiteHex = new Texture("spritesheets/hex_white.png");
        white_hex = new Sprite(whiteHex);
        hexAtlas = new TextureAtlas("spritesheets/hex3.txt");
//        white_hex = hexAtlas.createSprite("hex_blue");
//        blue_hex.setSize(40,46);
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(480, 800, camera);
        camera.zoom = 1.4f;

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 960, 1600, false);
        fbo.begin();

        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());

        batch.setProjectionMatrix(m);

        batch.begin();

        for (int xi = -3; xi < 22; xi++) {
            for (int yi = -3; yi < 37; yi++) {
                batch.draw(white_hex, xi * 44 + (yi % 2 == 0 ? 0 : 22), yi * 38, 40, 46);
            }
        }

        batch.end();

        fbo.end();

        tiles = new Sprite(fbo.getColorBufferTexture());

        tiles.flip(false,true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        drawTiles();

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        fbo.dispose();
        hexAtlas.dispose();
    }

    public void resize(int width, int height) {
//        camera.viewportWidth = width;
//        camera.viewportHeight = height;
        viewport.update(width, height);
    }

    private void drawTiles() {
        actualWidth = camera.zoom * camera.viewportWidth;
        actualHeight = camera.zoom * camera.viewportHeight;

        float leftX = position.x - actualWidth / 2f;
        float topY = position.y - actualHeight / 2f;

        int bottomYi = (int) Math.floor((topY + 19f) / 38f) - 1;
        int leftXi = (int) (bottomYi % 2 == 0 ? Math.floor((leftX + 22) / 44f) : Math.floor(leftX / 44f)) - 1;

        float firstX = leftXi * 44 + (bottomYi % 2 == 0 ? 0 : 22) - position.x;
        float firstY = bottomYi * 38 - position.y;

        position.x += 0.4;
        position.y += 0.4;

        tiles.setX(firstX);
        tiles.setY(firstY);
        tiles.draw(batch);
    }
}
