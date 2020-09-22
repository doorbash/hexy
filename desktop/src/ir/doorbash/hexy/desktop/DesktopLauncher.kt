package ir.doorbash.hexy.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import ir.doorbash.hexy.HexyGame

object DesktopLauncher {
    @JvmStatic
    fun main(vararg args: String) {
        val config = LwjglApplicationConfiguration()
        config.width = 800
        config.height = 480
        LwjglApplication(HexyGame(), config)
    }
}