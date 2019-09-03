package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ir.doorbash.hexy.R;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.SensorUtil;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.Shared;

public class SelectInputDialog extends DialogFragment {

    private static final String TAG = "SelectInputDialog";

    ImageView touch;
    ImageView floating;
    ImageView fixedLeft;
    ImageView fixedRight;
    ImageView rotation;
    TextView touchTxt;
    TextView floatingTxt;
    TextView fixedTxt;
    TextView rotationTxt;

    int langCode;
    int isDeviceRotationAvailable = -1;

    public static SelectInputDialog newInstance() {
        SelectInputDialog fragment = new SelectInputDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showDialog(Fragment fragment) {
        FragmentManager fm = fragment.getChildFragmentManager();
        SelectInputDialog myDialogFragment = SelectInputDialog.newInstance();
        myDialogFragment.show(fm, TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int width = (int) (screenWidth * 0.95f);
        int height = (int) (width * 0.52f);
        getDialog().getWindow().setLayout(width, height);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_input_select, container, false);
        touch = contentView.findViewById(R.id.touch);
        floating = contentView.findViewById(R.id.floating_joystick);
        fixedLeft = contentView.findViewById(R.id.fixed_left);
        fixedRight = contentView.findViewById(R.id.fixed_right);
        rotation = contentView.findViewById(R.id.rotation);
        touchTxt = contentView.findViewById(R.id.touch_txt);
        floatingTxt = contentView.findViewById(R.id.floating_joystick_txt);
        fixedTxt = contentView.findViewById(R.id.fixed_txt);
        rotationTxt = contentView.findViewById(R.id.rotation_txt);

        langCode = I18N.getLangCode(Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE));

        touch.setOnClickListener(view -> {
            Shared.getInstance(getContext()).setInt(Constants.KEY_SETTINGS_CONTROL, Constants.CONTROL_TOUCH).commit();
            dismiss();
        });

        floating.setOnClickListener(view -> {
            Shared.getInstance(getContext()).setInt(Constants.KEY_SETTINGS_CONTROL, Constants.CONTROL_FLOATING).commit();
            dismiss();
        });

        fixedLeft.setOnClickListener(view -> {
            Shared.getInstance(getContext()).setInt(Constants.KEY_SETTINGS_CONTROL, Constants.CONTROL_FIXED_LEFT).commit();
            dismiss();
        });

        fixedRight.setOnClickListener(view -> {
            Shared.getInstance(getContext()).setInt(Constants.KEY_SETTINGS_CONTROL, Constants.CONTROL_FIXED_RIGHT).commit();
            dismiss();
        });

        rotation.setOnClickListener(view -> {
            if (isDeviceRotationAvailable == 1) {
                Shared.getInstance(getContext()).setInt(Constants.KEY_SETTINGS_CONTROL, Constants.CONTROL_DEVICE_ROTATION).commit();
                dismiss();
            } else if (isDeviceRotationAvailable == 0) {
                Toast.makeText(getContext(), I18N.texts[langCode][I18N.not_supported], Toast.LENGTH_SHORT).show();
            }
        });

//        fixedLeft.post(() -> {
//            ViewGroup.LayoutParams params = fixedLeft.getLayoutParams();
//            params.height = params.width = fixedLeft.getWidth();
//            fixedLeft.setLayoutParams(params);
//        });
//
//        fixedRight.post(() -> {
//            ViewGroup.LayoutParams params = fixedRight.getLayoutParams();
//            params.height = params.width = fixedRight.getWidth();
//            fixedRight.setLayoutParams(params);
//        });

        touchTxt.setText(I18N.texts[langCode][I18N.touch_mode]);
        floatingTxt.setText(I18N.texts[langCode][I18N.floating_joystick]);
        fixedTxt.setText(I18N.texts[langCode][I18N.fixed_joystick_left_right]);
        rotationTxt.setText(I18N.texts[langCode][I18N.device_rotation]);

        if (isDeviceRotationAvailable == -1)
            isDeviceRotationAvailable = SensorUtil.isDeviceRotationAvailable(getContext()) ? 1 : 0;

        if (isDeviceRotationAvailable == 0) {
            rotation.setAlpha(0.5f);
            rotationTxt.setAlpha(0.5f);
        } else {
            rotation.setAlpha(1.0f);
            rotationTxt.setAlpha(1.0f);
        }

        return contentView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
