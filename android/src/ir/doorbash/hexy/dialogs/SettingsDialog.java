package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Spinner;

import ir.doorbash.hexy.LanguageSpinnerAdapter;
import ir.doorbash.hexy.R;

/**
 * Created by Milad Doorbash on 8/28/2019.
 */
public class SettingsDialog extends Dialog {
    Context context;

    Spinner languageSpinner;
    LanguageSpinnerAdapter languageAdapter;

    public SettingsDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        setContentView(R.layout.dialog_settings);

//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.height = WindowManager.LayoutParams.FILL_PARENT;
//        getWindow().setAttributes(params);

        languageAdapter = new LanguageSpinnerAdapter(context);
        languageSpinner = findViewById(R.id.language_spinner);
        languageSpinner.setAdapter(languageAdapter);
    }
}
