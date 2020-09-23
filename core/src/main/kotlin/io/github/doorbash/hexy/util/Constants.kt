package io.github.doorbash.hexy.util

/**
 * Created by Milad Doorbash on 8/30/2019.
 */
object Constants {
    const val GAME_ENDPOINT = "ws://localhost:2222"
//    const val API_ENDPOINT = "http://localhost:3232"
    const val CONNECT_TIMEOUT = 10000
    const val READ_TIMEOUT = 10000
    const val CONTROL_TOUCH = 1
    const val CONTROL_FLOATING = 2
    const val CONTROL_FIXED_LEFT = 3
    const val CONTROL_FIXED_RIGHT = 4
    const val CONTROL_DEVICE_ROTATION = 5
    const val PREFS_NAME = "settings"
    const val DEFAULT_SETTINGS_LANGUAGE = "English"
    const val DEFAULT_SETTINGS_GRAPHICS = "high"
    const val DEFAULT_SETTINGS_CONTROL = CONTROL_TOUCH
    const val KEY_SETTINGS_SOUND = "settings_sound"
    const val KEY_SETTINGS_GRAPHICS = "settings_graphics"
    const val KEY_SETTINGS_LANGUAGE = "settings_language"
    const val KEY_SETTINGS_CONTROL = "settings_control"
    const val KEY_PLAYER_NAME = "player_name"
    const val KEY_ID = "id"
    const val KEY_SELECTED_COLOR = "selected_color"
    const val KEY_SELECTED_IMAGE_INDEX = "selected_image_index"
    const val KEY_SELECTED_IMAGE_RES_ID = "selected_image_res_id"
    const val KEY_COINS = "coins"
    const val KEY_SKINS_USED = "skins_used"
}