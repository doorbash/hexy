package ir.doorbash.hexy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by Milad Doorbash on 10/16/2019.
 */
public class TextFadeOutAnimation {

    private static final float ALPHA_SPEED = 0.5f;
    private static final float Y_SPEED = 20;

    private String text;
    private Color color;
    private float x;
    private float y;

    public boolean stop = false;

    public TextFadeOutAnimation(String text, Color color, float x, float y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color.cpy();
    }

    public void draw(SpriteBatch batch, float dt, BitmapFont font) {
        if (stop) return;
        font.setColor(color);
        font.draw(batch, text, x, y);
        color.sub(0, 0, 0, dt * ALPHA_SPEED);
        y += dt * Y_SPEED;
        if (color.a == 0) stop = true;
    }
}
