package ir.doorbash.hexy;

import com.badlogic.gdx.Game;

public class HexyGame extends Game {

    PlayScreen playScreen;

    @Override
    public void create() {
        playScreen = new PlayScreen();
        this.setScreen(playScreen);
    }

}
