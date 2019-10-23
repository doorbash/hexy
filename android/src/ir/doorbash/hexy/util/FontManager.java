package ir.doorbash.hexy.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Milad Doorbash on 12/26/2016.
 */
public class FontManager {

    Typeface koodak;
    Typeface shabnamBold;
    Typeface yekan;
    Typeface robotoBold;
    Typeface robotoRegular;
    public Typeface noto;
    public Typeface arialbd;

    private static FontManager ourInstance;

    public static FontManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new FontManager(context);
        }
        return ourInstance;
    }

    private FontManager(Context context) {
        koodak = Typeface.createFromAsset(context.getAssets(), "fonts/BKOODB.TTF");
        robotoBold = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        shabnamBold = Typeface.createFromAsset(context.getAssets(), "fonts/Shabnam-Bold.ttf");
        yekan = Typeface.createFromAsset(context.getAssets(), "fonts/Yekan.ttf");
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        noto = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
        arialbd = Typeface.createFromAsset(context.getAssets(), "fonts/arialbd.ttf");
    }


    public Typeface getKoodak() {
        return koodak;
    }

    public Typeface getShabnamBold() {
        return shabnamBold;
    }

    public Typeface getYekan() {
        return yekan;
    }

    public Typeface getRobotoBold() {
        return robotoBold;
    }

    public Typeface getRobotoRegular() {
        return robotoRegular;
    }
}
