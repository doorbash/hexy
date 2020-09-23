package io.github.doorbash.hexy

import com.badlogic.gdx.Game

class HexyGame : Game() {
    //    private SplashScreen splashScreen;
    private var playScreen: PlayScreen? = null
    override fun create() {
        TrailGraphic.init()
        Bar.init()

//        splashScreen = new SplashScreen();
        if (playScreen == null) playScreen = PlayScreen()
        //        if (SHOW_SPLASH) {
//            new Thread(() -> {
//                Gdx.app.postRunnable(() -> setScreen(splashScreen));
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } finally {
//                    Gdx.app.postRunnable(() -> setScreen(playScreen));
//                }
//            }).start();
//        } else {
        setScreen(playScreen)
        //        }
    }

    companion object {
        private const val SHOW_SPLASH = false
    }
}