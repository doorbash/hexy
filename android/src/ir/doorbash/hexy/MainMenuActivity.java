package ir.doorbash.hexy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import ir.doorbash.hexy.dialogs.CustomizeDialog;
import ir.doorbash.hexy.dialogs.HowToPlayDialog;
import ir.doorbash.hexy.dialogs.SettingsDialog;
import ir.doorbash.hexy.util.ColorUtil;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.Shared;
import ir.doorbash.hexy.util.TextUtil;

/**
 * Created by Milad Doorbash on 8/25/2019.
 */
public class MainMenuActivity extends AppCompatActivity {

    ImageView stroke;
    ImageView fill;
    EditText nameTxt;
    Button playOnline;
    Button playAgainstAi;
    TextView log;
    TextView coinTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        stroke = findViewById(R.id.stroke_img);
        fill = findViewById(R.id.fill_img);
        nameTxt = findViewById(R.id.name_txt);
        coinTxt = findViewById(R.id.coin_txt);
        playOnline = findViewById(R.id.play_online);
        playAgainstAi = findViewById(R.id.play_against_ai);
        log = findViewById(R.id.log);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float width = displayMetrics.widthPixels;
        float height = displayMetrics.heightPixels;
        float dpWidth = width / displayMetrics.density;
        float dpHeight = height / displayMetrics.density;
        log.setText(new StringBuilder().append("screen size (px): ").append(width).append("x").append(height).append("\n").append("screen size (dp): ").append(dpWidth).append("x").append(dpHeight).append("\n").append("density: ").append(displayMetrics.density).append("\n").append("dpi: ").append(displayMetrics.density * 160).toString());

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

        int selectedColor = Shared.getInstance(this).getInt(Constants.KEY_SELECTED_COLOR, 0);
        Drawable drawable = getResources().getDrawable(R.drawable.circle);
        drawable.setColorFilter(ColorUtil.STROKE_COLORS[selectedColor], PorterDuff.Mode.MULTIPLY);
        stroke.setImageDrawable(drawable);

        int selectedFill = Shared.getInstance(this).getInt(Constants.KEY_SELECTED_FILL, 0);

        if (selectedFill == 0) {
            fill.setImageResource(R.drawable.circle);
            fill.setColorFilter(ColorUtil.FILL_COLORS[selectedColor]);
        } else {
            Glide.with(this).load(CustomizeDialog.FILL_IMAGES[selectedFill]).into(fill);
            fill.setColorFilter(0);
        }

        coinTxt.setText(Shared.getInstance(this).getInt(Constants.KEY_COINS, 0) + "");
    }

    public void openCustomize(View view) {
        // Toast.makeText(this, "open customize dialog", Toast.LENGTH_SHORT).show();
        CustomizeDialog.showDialog(this);
    }

    public void openFreeCoinDialog(View view) {
        Toast.makeText(this, "open free coin dialog", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
