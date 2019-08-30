package ir.doorbash.hexy.util;

/**
 * Created by Milad Doorbash on 8/30/2019.
 */
public class I18N {
    private static final int NUM_LANGUAGES = 2;
    private static final int NUM_TEXTS = 10;

    public static final int settings_title = 0;
    public static final int settings_sound_on = 1;
    public static final int settings_sound_off = 2;
    public static final int settings_controls = 3;
    public static final int main_menu_your_name = 4;
    public static final int main_menu_play_online = 5;
    public static final int main_menu_play_against_ai = 6;
    public static final int you_will_respawn_in_9_seconds = 7;
    public static final int connecting = 8;

    private static final int not_supported = -1;
    private static final int english = 0;
    private static final int persian = 1;

    public static int getLangCode(String lang) {
        switch (lang.toLowerCase()) {
            case "english":
                return english;
            case "فارسی":
                return persian;
        }
        return -1;
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
    }

}
