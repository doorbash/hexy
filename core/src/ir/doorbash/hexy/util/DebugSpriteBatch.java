package ir.doorbash.hexy.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import java.lang.reflect.Field;

/**
 * Created by Milad Doorbash on 8/31/2019.
 */
public class DebugSpriteBatch extends SpriteBatch {
    @Override
    protected void switchTexture(Texture texture) {
        // int x = 1/0;
        try {
            Field lastTextureField = getClass().getSuperclass().getDeclaredField("lastTexture");
            lastTextureField.setAccessible(true);
            Texture lastTexture = (Texture) lastTextureField.get(this);
            if (lastTexture != null) {
                System.out.println("****** switching from " + lastTexture + " to " + texture);
            }

            super.switchTexture(texture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection) {
        System.out.println("setProjectionMatrix()");
        super.setProjectionMatrix(projection);
    }

    @Override
    public void flush() {
        try {
            Field idxField = getClass().getSuperclass().getDeclaredField("idx");
            idxField.setAccessible(true);
            if (((int) idxField.get(this)) > 0) {
                System.out.println(">>>>>>>>>>>>> FLUSH >>>>>> renderCalls = " + (renderCalls + 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.flush();
    }

    @Override
    public void end() {
        System.out.println("************* END ******************");
        super.end();
    }

    @Override
    public void begin() {
        System.out.println("************* BEGIN ******************");
        super.begin();
    }
}
