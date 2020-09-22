package ir.doorbash.hexy.util;

/**
 * Created by Milad Doorbash on 8/30/2019.
 */
public class Constants {
    //        private static final String ENDPOINT = "wss://cefd3aab.ngrok.io";
//    private static final String ENDPOINT = "ws://192.168.1.134:2222";
//    public static final String ENDPOINT = "ws://46.21.147.7:3333";
//    public static final String ENDPOINT = "ws://127.0.0.1:3333";
    public static final String GAME_ENDPOINT = "ws://192.168.1.134:2222";
    public static final String API_ENDPOINT = "http://192.168.1.134:3232";

    public static final int CONNECT_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;

    public static final int CONTROL_TOUCH = 1;
    public static final int CONTROL_FLOATING = 2;
    public static final int CONTROL_FIXED_LEFT = 3;
    public static final int CONTROL_FIXED_RIGHT = 4;
    public static final int CONTROL_DEVICE_ROTATION = 5;

    public static final String PREFS_NAME = "settings";

    public static final String DEFAULT_SETTINGS_LANGUAGE = "English";
    public static final String DEFAULT_SETTINGS_GRAPHICS = "high";
    public static final int DEFAULT_SETTINGS_CONTROL = CONTROL_TOUCH;

    public static final String KEY_SETTINGS_SOUND = "settings_sound";
    public static final String KEY_SETTINGS_GRAPHICS = "settings_graphics";
    public static final String KEY_SETTINGS_LANGUAGE = "settings_language";
    public static final String KEY_SETTINGS_CONTROL = "settings_control";
    public static final String KEY_PLAYER_NAME = "player_name";
    public static final String KEY_ID = "id";
    public static final String KEY_SELECTED_COLOR = "selected_color";
    public static final String KEY_SELECTED_IMAGE_INDEX = "selected_image_index";
    public static final String KEY_SELECTED_IMAGE_RES_ID = "selected_image_res_id";
    public static final String KEY_COINS = "coins";
    public static final String KEY_SKINS_USED = "skins_used";
}
