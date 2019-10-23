package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import ir.doorbash.hexy.R;
import ir.doorbash.hexy.util.FontManager;

public class MaterialAlertDialog extends Dialog {

    Context mContext;
    TextView titleTxt;
    TextView textTxt;
    Button dismissBtn;

    String title;
    String text;
    Runnable ok;
    Runnable cancel;
    String okText;
    String cancelText;

    public MaterialAlertDialog(Context context, String title, String text, String okText, String cancelText, Runnable ok, Runnable cancel) {
        super(context);
        mContext = context;
        this.title = title;
        this.text = text;
        this.ok = ok;
        this.cancel = cancel;
        this.okText = okText;
        this.cancelText = cancelText;
    }

    //_____________________________________________________ onCreate Function ______________________
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setContentView(R.layout.dialog_change_log);
        setCancelable(false);

        titleTxt = findViewById(R.id.title);
        textTxt = findViewById(R.id.text);
        dismissBtn = findViewById(R.id.dismiss);

        titleTxt.setTypeface(FontManager.getInstance(mContext).getShabnamBold());
        textTxt.setTypeface(FontManager.getInstance(mContext).getYekan());
        dismissBtn.setTypeface(FontManager.getInstance(mContext).getKoodak());

        titleTxt.setText(title);
        textTxt.setText(text);

        dismissBtn.setOnClickListener(v -> ok.run());
    }
}
