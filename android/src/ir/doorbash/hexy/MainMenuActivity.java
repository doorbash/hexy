package ir.doorbash.hexy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import ir.doorbash.hexy.dialogs.HowToPlayDialog;
import ir.doorbash.hexy.dialogs.SettingsDialog;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.Shared;
import ir.doorbash.hexy.util.TextUtil;

/**
 * Created by Milad Doorbash on 8/25/2019.
 */
public class MainMenuActivity extends AppCompatActivity {

    ImageView stroke;
    EditText nameTxt;
    Button playOnline;
    Button playAgainstAi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        stroke = findViewById(R.id.stroke_img);
        nameTxt = findViewById(R.id.name_txt);
        playOnline = findViewById(R.id.play_online);
        playAgainstAi = findViewById(R.id.play_against_ai);

        Drawable drawable = getResources().getDrawable(R.drawable.circle);
        drawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        stroke.setImageDrawable(drawable);

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = TextUtil.validateName(s.toString());
                Shared.getInstance(MainMenuActivity.this).setString(Constants.KEY_PLAYER_NAME, name).commit();
                nameTxt.removeTextChangedListener(this);
                nameTxt.setText(name);
                nameTxt.setSelection(nameTxt.getText().length());
                nameTxt.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void startGame() {
        Intent i = new Intent(MainMenuActivity.this, AndroidLauncher.class);
        MainMenuActivity.this.startActivity(i);
    }

    public void playOnline(View view) {
        startGame();
    }

    public void openSettings(View view) {
        SettingsDialog.showDialog(this);
    }

    public void openHowToPlay(View view) {
        HowToPlayDialog.showDialog(this);
    }

    public void updateUI() {
        System.out.println("updateUI");
        String lang = Shared.getInstance(this).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE);
        int langCode = I18N.getLangCode(lang);

        nameTxt.setHint(I18N.texts[langCode][I18N.main_menu_your_name]);
        playOnline.setHint(I18N.texts[langCode][I18N.main_menu_play_online]);
        playAgainstAi.setHint(I18N.texts[langCode][I18N.main_menu_play_against_ai]);

        nameTxt.setText(Shared.getInstance(this).getString(Constants.KEY_PLAYER_NAME, ""));
    }
}
