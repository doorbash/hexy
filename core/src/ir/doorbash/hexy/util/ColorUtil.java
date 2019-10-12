package ir.doorbash.hexy.util;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
public class ColorUtil {

    private static final float PLAYER_PROGRESSBAR_ALPHA = 0.5f;
    private static final float PATH_CELL_ALPHA_TINT = 0.4f;

    public static final int[] STROKE_COLORS = new int[] {
            0xff2196f3, // blue
            0xff795548, // brown
            0xff4caf50, // green
            0xffff9800, // orange
            0xff9c27b0, // purple
            0xfff44336, // red
            0xff3f51b5, // indigo

            0xffc4c340,
            0xff9e2e2e,
            0xffbe364a,
            0xff00a5a3,
            0xffa51f8a,
            0xff5b9220,
            0xffa64f08,
            0xffba9633,
            0xffa81640,
            0xff008bb8,
            0xffd128db,
            0xff14954e,
            0xffb4c74c,
            0xff2ea67d,
            0xff4db23a,
    };

    public static final int[] FILL_COLORS = new int[] {
            0xff6ec6ff,
            0xffa98274,
            0xff80e27e,
            0xffffc947,
            0xffd05ce3,
            0xffff7961,
            0xff757de8,

            0xfffffd46,
            0xfffd3535,
            0xffff5e75,
            0xff2cfefc,
            0xfffe2cd4,
            0xff94ff22,
            0xffff9844,
            0xffffc832,
            0xffff1c5d,
            0xff13c5ff,
            0xfff662ff,
            0xff22ff85,
            0xffedff89,
            0xff89ffd7,
            0xff9cff89,
    };

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
