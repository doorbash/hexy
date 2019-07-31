package ir.doorbash.hexy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class HexyGame extends Game {

    private static final boolean SHOW_SPLASH = false;

    private SplashScreen splashScreen;
    private PlayScreen playScreen;

    @Override
    public void create() {
        TrailGraphic.init();
        ProgressBar.init();

        splashScreen = new SplashScreen();
        playScreen = new PlayScreen();
        if (SHOW_SPLASH) {
            new Thread(() -> {
                Gdx.app.postRunnable(() -> setScreen(splashScreen));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Gdx.app.postRunnable(() -> setScreen(playScreen));
                }
            }).start();
        } else {
            setScreen(playScreen);
        }
    }

}
