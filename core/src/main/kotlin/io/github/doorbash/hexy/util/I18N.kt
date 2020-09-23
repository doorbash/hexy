package io.github.doorbash.hexy.util

/**
 * Created by Milad Doorbash on 8/30/2019.
 */
object I18N {
    private const val NUM_LANGUAGES = 2
    private const val NUM_TEXTS = 19
    const val settings_title = 0
    const val settings_sound_on = 1
    const val settings_sound_off = 2
    const val settings_controls = 3
    const val main_menu_your_name = 4
    const val main_menu_play_online = 5
    const val main_menu_play_against_ai = 6
    const val you_will_respawn_in_9_seconds = 7
    const val connecting = 8
    const val settings_graphics_high = 9
    const val settings_graphics_low = 10
    const val coming_soon = 11
    const val touch_mode = 12
    const val floating_joystick = 13
    const val fixed_joystick_left_right = 14
    const val device_rotation = 15
    const val not_supported = 16
    const val customize = 17
    const val ok = 18
    private const val english = 0
    private const val persian = 1
    fun getLangCode(lang: String): Int {
        when (lang.toLowerCase()) {
            "English" -> return english
            "فارسی" -> return persian
        }
        return english
    }

    val texts = Array(NUM_LANGUAGES) { arrayOfNulls<String>(NUM_TEXTS) }

    init {
        texts[english][settings_title] = "Settings"
        texts[persian][settings_title] = "تنظیمات"
        texts[english][settings_sound_on] = "Sound: On"
        texts[persian][settings_sound_on] = "صدا: باز"
        texts[english][settings_sound_off] = "Sound: Off"
        texts[persian][settings_sound_off] = "صدا: بسته"
        texts[english][settings_controls] = "Controls"
        texts[persian][settings_controls] = "کنترل بازی"
        texts[english][settings_graphics_high] = "Graphics: Emoji + Colors"
        texts[persian][settings_graphics_high] = "گرافیک: اموجی + رنگ"
        texts[english][settings_graphics_low] = "Graphics: Only colors"
        texts[persian][settings_graphics_low] = "گرافیک: فقط رنگ"
        texts[english][main_menu_your_name] = "Your name"
        texts[persian][main_menu_your_name] = "نام شما"
        texts[english][main_menu_play_online] = "PLAY ONLINE"
        texts[persian][main_menu_play_online] = "بازی آنلاین"
        texts[english][main_menu_play_against_ai] = "PLAY AGAINST AI"
        texts[persian][main_menu_play_against_ai] = "بازی مقابل هوش مصنوعی"
        texts[english][you_will_respawn_in_9_seconds] = "You will respawn in 9 seconds"
        texts[persian][you_will_respawn_in_9_seconds] = "شما 9 ثانیه دیگر وارد بازی خواهید شد"
        texts[english][connecting] = "Connecting..."
        texts[persian][connecting] = "در حال اتصال..."
        texts[english][coming_soon] = "Coming soon"
        texts[persian][coming_soon] = "به زودی"
        texts[english][touch_mode] = "Touch Mode"
        texts[persian][touch_mode] = "حالت لمسی"
        texts[english][floating_joystick] = "Floating Joystick"
        texts[persian][floating_joystick] = "فرمان شناور"
        texts[english][fixed_joystick_left_right] = "(Left) Fixed Joystick (Right)"
        texts[persian][fixed_joystick_left_right] = "(راست) فرمان ثابت (چپ)"
        texts[english][device_rotation] = "Device Rotation"
        texts[persian][device_rotation] = "چرخش دستگاه"
        texts[english][not_supported] = "Your device does not support this control"
        texts[persian][not_supported] = "دستگاه شما این کنترل را پشتیبانی نمی کند"
        texts[english][customize] = "Customize"
        texts[persian][customize] = "شخصی\u200Cسازی"
        texts[english][ok] = "OK"
        texts[persian][ok] = "خُب"
    }
}