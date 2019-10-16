package ir.doorbash.hexy.util;

/**
 * Created by Milad Doorbash on 8/30/2019.
 */
public class I18N {
    private static final int NUM_LANGUAGES = 2;
    private static final int NUM_TEXTS = 19;

    public static final int settings_title = 0;
    public static final int settings_sound_on = 1;
    public static final int settings_sound_off = 2;
    public static final int settings_controls = 3;
    public static final int main_menu_your_name = 4;
    public static final int main_menu_play_online = 5;
    public static final int main_menu_play_against_ai = 6;
    public static final int you_will_respawn_in_9_seconds = 7;
    public static final int connecting = 8;
    public static final int settings_graphics_high = 9;
    public static final int settings_graphics_low = 10;
    public static final int coming_soon = 11;
    public static final int touch_mode = 12;
    public static final int floating_joystick = 13;
    public static final int fixed_joystick_left_right = 14;
    public static final int device_rotation = 15;
    public static final int not_supported = 16;
    public static final int customize = 17;
    public static final int ok = 18;

    private static final int english = 0;
    private static final int persian = 1;

    public static int getLangCode(String lang) {
        switch (lang.toLowerCase()) {
            case "English":
                return english;
            case "فارسی":
                return persian;
        }
        return english;
    }

    public static final String[][] texts = new String[NUM_LANGUAGES][NUM_TEXTS];

    static {
        texts[english][settings_title] = "Settings";
        texts[persian][settings_title] = "تنظیمات";

        texts[english][settings_sound_on] = "Sound: On";
        texts[persian][settings_sound_on] = "صدا: باز";

        texts[english][settings_sound_off] = "Sound: Off";
        texts[persian][settings_sound_off] = "صدا: بسته";

        texts[english][settings_controls] = "Controls";
        texts[persian][settings_controls] = "کنترل بازی";

        texts[english][settings_graphics_high] = "Graphics: Emoji + Colors";
        texts[persian][settings_graphics_high] = "گرافیک: اموجی + رنگ";

        texts[english][settings_graphics_low] = "Graphics: Only colors";
        texts[persian][settings_graphics_low] = "گرافیک: فقط رنگ";

        texts[english][main_menu_your_name] = "Your name";
        texts[persian][main_menu_your_name] = "نام شما";

        texts[english][main_menu_play_online] = "PLAY ONLINE";
        texts[persian][main_menu_play_online] = "بازی آنلاین";

        texts[english][main_menu_play_against_ai] = "PLAY AGAINST AI";
        texts[persian][main_menu_play_against_ai] = "بازی مقابل هوش مصنوعی";

        texts[english][you_will_respawn_in_9_seconds] = "You will respawn in 9 seconds";
        texts[persian][you_will_respawn_in_9_seconds] = "شما 9 ثانیه دیگر وارد بازی خواهید شد";

        texts[english][connecting] = "Connecting...";
        texts[persian][connecting] = "در حال اتصال...";

        texts[english][coming_soon] = "Coming soon";
        texts[persian][coming_soon] = "به زودی";

        texts[english][touch_mode] = "Touch Mode";
        texts[persian][touch_mode] = "حالت لمسی";

        texts[english][floating_joystick] = "Floating Joystick";
        texts[persian][floating_joystick] = "فرمان شناور";

        texts[english][fixed_joystick_left_right] = "(Left) Fixed Joystick (Right)";
        texts[persian][fixed_joystick_left_right] = "(راست) فرمان ثابت (چپ)";

        texts[english][device_rotation] = "Device Rotation";
        texts[persian][device_rotation] = "چرخش دستگاه";

        texts[english][not_supported] = "Your device does not support this control";
        texts[persian][not_supported] = "دستگاه شما این کنترل را پشتیبانی نمی کند";

        texts[english][customize] = "Customize";
        texts[persian][customize] = "شخصی\u200Cسازی";

        texts[english][ok] = "OK";
        texts[persian][ok] = "خُب";
    }

}
