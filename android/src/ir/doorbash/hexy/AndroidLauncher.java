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
        if (Shared.getInstance(this).getInt(Constants.KEY_SETTINGS_CONTROL, Constants.DEFAULT_SETTINGS_CONTROL) == Constants.CONTROL_DEVICE_ROTATION) {
            config.useRotationVectorSensor = true;
            config.useAccelerometer = true;
            config.useCompass = true;
        } else {
            config.useRotationVectorSensor = false;
            config.useAccelerometer = false;
            config.useCompass = false;
        }
        initialize(new HexyGame(), config);
    }

    @Override
    public void onBackPressed() {

    }
}
