package ir.doorbash.hexy.util;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
public class ColorUtil {

    private static final float PLAYER_PROGRESSBAR_ALPHA = 0.5f;
    private static final float PATH_CELL_ALPHA_TINT = 0.4f;

    public static Color getPlayerProgressColor(Color color) {
        float r = 1 - PLAYER_PROGRESSBAR_ALPHA + color.r * PLAYER_PROGRESSBAR_ALPHA;
        float g = 1 - PLAYER_PROGRESSBAR_ALPHA + color.g * PLAYER_PROGRESSBAR_ALPHA;
        float b = 1 - PLAYER_PROGRESSBAR_ALPHA + color.b * PLAYER_PROGRESSBAR_ALPHA;
        return new Color(r, g, b, 1.0f);
    }

    public static Color getPlayerPathCellColor(Color color) {
        float r = 1 - PATH_CELL_ALPHA_TINT + color.r * PATH_CELL_ALPHA_TINT;
        float g = 1 - PATH_CELL_ALPHA_TINT + color.g * PATH_CELL_ALPHA_TINT;
        float b = 1 - PATH_CELL_ALPHA_TINT + color.b * PATH_CELL_ALPHA_TINT;
        return new Color(r, g, b, 1.0f);
    }

}
