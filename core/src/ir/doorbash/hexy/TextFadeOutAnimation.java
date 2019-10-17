package ir.doorbash.hexy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

/**
 * Created by Milad Doorbash on 10/16/2019.
 */
public class TextFadeOutAnimation {

    private final Interpolation interpolation = new Interpolation.PowIn(3);

    private static final float Y_SPEED = 20;

    private String text;
    private Color color;
    private float x;
    private float y;
    private float alpha = 1f;
    private float timer = 0f;

    public boolean stop = false;

    public TextFadeOutAnimation(String text, Color color, float x, float y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color.cpy();
    }

    public void draw(SpriteBatch batch, float dt, BitmapFont font) {
        if (stop) return;
        timer += dt;
        font.setColor(color);
        font.draw(batch, text, x, y);
        alpha = 1 - interpolation.apply(timer);
        color.set(color.r, color.g, color.b, alpha);
        y += dt * Y_SPEED;
        if (color.a < 0.1f) stop = true;
    }
}
