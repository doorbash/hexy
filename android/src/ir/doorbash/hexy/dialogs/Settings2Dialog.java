package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import ir.doorbash.hexy.R;
import ir.doorbash.hexy.adapter.LanguageSpinnerAdapter2;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.Shared;
import ir.doorbash.hexy.util.I18N;

/**
 * Created by Milad Doorbash on 8/28/2019.
 */
public class Settings2Dialog extends Dialog {

    private Context context;
    private Button sound;
    private Button controls;
    private Spinner languageSpinner;
    private LanguageSpinnerAdapter2 languageAdapter;
    private TextView title;

    public Settings2Dialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        setContentView(R.layout.dialog_settings2);

//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.height = WindowManager.LayoutParams.FILL_PARENT;
//        getWindow().setAttributes(params);


        languageSpinner = findViewById(R.id.language_spinner);
        sound = findViewById(R.id.sound_txt);
        controls = findViewById(R.id.input_txt);
        title = findViewById(R.id.title_txt);

        languageAdapter = new LanguageSpinnerAdapter2(context);
        languageSpinner.setAdapter(languageAdapter);

        updateUI();

        sound.setOnClickListener(view -> {
            boolean sfx = Shared.getInstance(context).getBoolean(Constants.KEY_SETTINGS_SOUND, true);
            Shared.getInstance(context).setBoolean(Constants.KEY_SETTINGS_SOUND, !sfx).commit();
            updateUI();
        });

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("languageSpinner.setOnItemSelectedListener");
                String lang = languageAdapter.array[position];
                Shared.getInstance(context).setString(Constants.KEY_SETTINGS_LANGUAGE, lang).commit();
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateUI() {
        String lang = Shared.getInstance(context).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE);
        int langCode = I18N.getLangCode(lang);

        title.setText(I18N.texts[langCode][I18N.settings_title]);

        int index = 0;
        for (int i = 0; i < languageAdapter.array.length; i++) {
            if (languageAdapter.array[i].equals(lang)) {
                index = i;
                break;
            }
        }
        languageSpinner.setSelection(index);

        if (Shared.getInstance(context).getBoolean(Constants.KEY_SETTINGS_SOUND, true)) {
            sound.setText(I18N.texts[langCode][I18N.settings_sound_on]);
        } else {
            sound.setText(I18N.texts[langCode][I18N.settings_sound_off]);
        }

        controls.setText(I18N.texts[langCode][I18N.settings_controls]);
    }
}
