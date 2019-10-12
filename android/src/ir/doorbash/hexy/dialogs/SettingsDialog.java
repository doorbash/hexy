package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import ir.doorbash.hexy.MainMenuActivity;
import ir.doorbash.hexy.R;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.Shared;

public class SettingsDialog extends DialogFragment {

    private static final String TAG = "SettingsDialog";

    private Button sound;
    private Button graphics;
    private Button language;
    private Button controls;
    //    private Spinner languageSpinner;
//    private LanguageSpinnerAdapter2 languageAdapter;
    private TextView title;

    public static SettingsDialog newInstance() {
        SettingsDialog fragment = new SettingsDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showDialog(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        SettingsDialog myDialogFragment = SettingsDialog.newInstance();
        myDialogFragment.show(fm, TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_settings_height);
        int width = height;
        getDialog().getWindow().setLayout(width, height);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_settings, container, false);

        //        languageSpinner = findViewById(R.id.language_spinner);
        sound = contentView.findViewById(R.id.sound_txt);
        graphics = contentView.findViewById(R.id.graphics_mode);
        language = contentView.findViewById(R.id.language_txt);
        controls = contentView.findViewById(R.id.input_txt);
        title = contentView.findViewById(R.id.title_txt);

//        languageAdapter = new LanguageSpinnerAdapter2(context);
//        languageSpinner.setAdapter(languageAdapter);

        updateUI();

//        Shared.getInstance(context).setBoolean(Constants.KEY_SETTINGS_SOUND, true).commit();

        sound.setOnClickListener(view -> {
            boolean sfx = Shared.getInstance(getContext()).getBoolean(Constants.KEY_SETTINGS_SOUND, true);
            Shared.getInstance(getContext()).setBoolean(Constants.KEY_SETTINGS_SOUND, !sfx).commit();
            updateUI();
        });

        graphics.setOnClickListener(view -> {
            String gfx = Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_GRAPHICS, Constants.DEFAULT_SETTINGS_GRAPHICS);
            Shared.getInstance(getContext()).setString(Constants.KEY_SETTINGS_GRAPHICS, gfx.equals("high") ? "low" : "high").commit();
            updateUI();
        });

        language.setOnClickListener(view -> {
            String gfx = Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE);
            Shared.getInstance(getContext()).setString(Constants.KEY_SETTINGS_LANGUAGE, gfx.equals("English") ? "فارسی" : "English").commit();
            updateUI();
        });

        controls.setOnClickListener(view -> SelectInputDialog.showDialog(this));

//        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                System.out.println("languageSpinner.setOnItemSelectedListener");
//                String lang = languageAdapter.array[position];
//                Shared.getInstance(context).setString(Constants.KEY_SETTINGS_LANGUAGE, lang).commit();
//                updateUI();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        return contentView;
    }

    private void updateUI() {
        String lang = Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE);
        int langCode = I18N.getLangCode(lang);

        title.setText(I18N.texts[langCode][I18N.settings_title]);

//        int index = 0;
//        for (int i = 0; i < languageAdapter.array.length; i++) {
//            if (languageAdapter.array[i].equals(lang)) {
//                index = i;
//                break;
//            }
//        }
//        languageSpinner.setSelection(index);

        if (Shared.getInstance(getContext()).getBoolean(Constants.KEY_SETTINGS_SOUND, true)) {
            sound.setText(I18N.texts[langCode][I18N.settings_sound_on]);
        } else {
            sound.setText(I18N.texts[langCode][I18N.settings_sound_off]);
        }

        if (Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_GRAPHICS, Constants.DEFAULT_SETTINGS_GRAPHICS).equals("high")) {
            graphics.setText(I18N.texts[langCode][I18N.settings_graphics_high]);
        } else {
            graphics.setText(I18N.texts[langCode][I18N.settings_graphics_low]);
        }

        language.setText(Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE));

        controls.setText(I18N.texts[langCode][I18N.settings_controls]);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        MainMenuActivity activity = ((MainMenuActivity) getActivity());
        if (activity != null) activity.updateUI();
        super.onDismiss(dialog);
    }
}