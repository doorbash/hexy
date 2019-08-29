package ir.doorbash.hexy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import ir.doorbash.hexy.dialogs.HowToPlayDialog;
import ir.doorbash.hexy.dialogs.SettingsDialog;

/**
 * Created by Milad Doorbash on 8/25/2019.
 */
public class MainMenuActivity extends AppCompatActivity {

    ImageView stroke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        stroke = findViewById(R.id.stroke_img);

        Drawable drawable = getResources().getDrawable(R.drawable.circle);
        drawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        stroke.setImageDrawable(drawable);
    }

    private void startGame() {
        Intent i = new Intent(MainMenuActivity.this, AndroidLauncher.class);
        MainMenuActivity.this.startActivity(i);
    }

    public void playOnline(View view) {
        startGame();
    }

    public void openSettings(View view) {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.show();
    }

    public void openHowToPlay(View view) {
       HowToPlayDialog.showDialog(this, () -> {
           System.out.println("Did i fuckin ask?");
       });
    }
}
