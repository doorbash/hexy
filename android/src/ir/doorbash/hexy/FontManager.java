package ir.doorbash.hexy;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Milad Doorbash on 12/26/2016.
 */
public class FontManager {

    public Typeface noto;
    public Typeface arialbd;

    private static FontManager instance;

    public static FontManager getInstance(Context context) {
        if (instance == null) {
            instance = new FontManager(context);
        }
        return instance;
    }

    private FontManager(Context context) {
        noto = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
        arialbd = Typeface.createFromAsset(context.getAssets(), "fonts/arialbd.ttf");
    }
}
