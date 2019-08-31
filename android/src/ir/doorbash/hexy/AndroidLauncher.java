package ir.doorbash.hexy;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.Shared;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useWakelock = true;
        config.disableAudio = !Shared.getInstance(this).getBoolean(Constants.KEY_SETTINGS_SOUND, true);
        config.hideStatusBar = true;
        initialize(new HexyGame(), config);
    }

    @Override
    public void onBackPressed() {

    }
}
