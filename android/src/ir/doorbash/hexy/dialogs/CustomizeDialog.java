package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;

import ir.doorbash.hexy.MainMenuActivity;
import ir.doorbash.hexy.R;
import ir.doorbash.hexy.adapter.CustomizeFillImagesListAdapter;
import ir.doorbash.hexy.util.ColorUtil;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.Shared;
import ir.doorbash.hexy.util.TextUtil;

public class CustomizeDialog extends DialogFragment {

    private static final String TAG = "CustomizeDialog";

    public static final int[] FILL_IMAGES = new int[]{
            0,
            R.drawable.e00000,
            R.drawable.e00001,
            R.drawable.e00002,
            R.drawable.e00003,
            R.drawable.e00004,
            R.drawable.e00005,
            R.drawable.e00006,
            R.drawable.e00007,
            R.drawable.e00008,
            R.drawable.e00009,
            R.drawable.e00010,
            R.drawable.e00011,
            R.drawable.e00012,
            R.drawable.e00013,
            R.drawable.e00014,
            R.drawable.e00015,
            R.drawable.e00016,
            R.drawable.e00017,
            R.drawable.e00018,
            R.drawable.e00019,
            R.drawable.e00020,
            R.drawable.e00021,
            R.drawable.e00022,
            R.drawable.e00023,
            R.drawable.e00024,
            R.drawable.e00025,
            R.drawable.e00026,
            R.drawable.e00027,
            R.drawable.e00028,
            R.drawable.e00029,
            R.drawable.e00030,
            R.drawable.e00031,
            R.drawable.e00032,
            R.drawable.e00033,
            R.drawable.e00034,
            R.drawable.e00035,
            R.drawable.e00036,
            R.drawable.e00037,
            R.drawable.e00038,
            R.drawable.e00039,
            R.drawable.e00040,
            R.drawable.e00041,
            R.drawable.e00042,
            R.drawable.e00043,
            R.drawable.e00044,
            R.drawable.e00045,
            R.drawable.e00046,
            R.drawable.e00047,
            R.drawable.e00048,
            R.drawable.e00049,
            R.drawable.e00050,
            R.drawable.e00051,
            R.drawable.e00052,
            R.drawable.e00053,
            R.drawable.e00054,
            R.drawable.e00055,
            R.drawable.e00056,
            R.drawable.e00057,
            R.drawable.e00058,
            R.drawable.e00059,
            R.drawable.e00060,
            R.drawable.e00061,
            R.drawable.e00062,
            R.drawable.e00063,
            R.drawable.e00064,
            R.drawable.e00065,
            R.drawable.e00066,
            R.drawable.e00067,
            R.drawable.e00068,
            R.drawable.e00069,
            R.drawable.e00070,
            R.drawable.e00071,
            R.drawable.e00072,
            R.drawable.e00073,
            R.drawable.e00074,
            R.drawable.e00075,
            R.drawable.e00076,
            R.drawable.e00077,
            R.drawable.e00078,
            R.drawable.e00079,
            R.drawable.e00080,
            R.drawable.e00081,
            R.drawable.e00082,
            R.drawable.e00083,
            R.drawable.e00084,
            R.drawable.e00085,
            R.drawable.e00086,
            R.drawable.e00087,
            R.drawable.e00088,
            R.drawable.e00089,
            R.drawable.e00090,
            R.drawable.e00091,
            R.drawable.e00092,
            R.drawable.e00093,
            R.drawable.e00094,
            R.drawable.e00095,
            R.drawable.e00096,
            R.drawable.e00097,
            R.drawable.e00098,
            R.drawable.e00099,
            R.drawable.e00100,
            R.drawable.e00101,
            R.drawable.e00102,
            R.drawable.e00103,
            R.drawable.e00104,
            R.drawable.e00105,
            R.drawable.e00106,
            R.drawable.e00107,
            R.drawable.e00108,
            R.drawable.e00109,
            R.drawable.e00110,
            R.drawable.e00111,
            R.drawable.e00112,
            R.drawable.e00113,
            R.drawable.e00114,
            R.drawable.e00115,
            R.drawable.e00116,
            R.drawable.e00117,
            R.drawable.e00118,
            R.drawable.e00119,
            R.drawable.e00120,
            R.drawable.e00121,
            R.drawable.e00122,
            R.drawable.e00123,
            R.drawable.e00124,
            R.drawable.e00125,
            R.drawable.e00126,
            R.drawable.e00127,
            R.drawable.e00128,
            R.drawable.e00129,
            R.drawable.e00130,
            R.drawable.e00131,
            R.drawable.e00132,
            R.drawable.e00133,
            R.drawable.e00134,
            R.drawable.e00135,
            R.drawable.e00136,
            R.drawable.e00137,
            R.drawable.e00138,
            R.drawable.e00139,
            R.drawable.e00140,
            R.drawable.e00141,
            R.drawable.e00142,
            R.drawable.e00143,
            R.drawable.e00144,
            R.drawable.e00145,
            R.drawable.e00146,
            R.drawable.e00147,
            R.drawable.e00148,
            R.drawable.e00149,
            R.drawable.e00150,
            R.drawable.e00151,
            R.drawable.e00152,
            R.drawable.e00153,
            R.drawable.e00154,
            R.drawable.e00155,
            R.drawable.e00156,
            R.drawable.e00157,
            R.drawable.e00158,
            R.drawable.e00159,
            R.drawable.e00160,
            R.drawable.e00161,
            R.drawable.e00162,
            R.drawable.e00163,
            R.drawable.e00164,
            R.drawable.e00165,
            R.drawable.e00166,
            R.drawable.e00167,
            R.drawable.e00168,
            R.drawable.e00169,
            R.drawable.e00170,
            R.drawable.e00171,
            R.drawable.e00172,
            R.drawable.e00173,
            R.drawable.e00174,
            R.drawable.e00175,
            R.drawable.e00176,
            R.drawable.e00177,
            R.drawable.e00178,
            R.drawable.e00179,
            R.drawable.e00180,
            R.drawable.e00181,
            R.drawable.e00182,
            R.drawable.e00183,
            R.drawable.e00184,
            R.drawable.e00185,
            R.drawable.e00186,
            R.drawable.e00187,
            R.drawable.e00188,
            R.drawable.e00189,
            R.drawable.e00190,
            R.drawable.e00191,
            R.drawable.e00192,
            R.drawable.e00193,
            R.drawable.e00194,
            R.drawable.e00195,
            R.drawable.e00196,
            R.drawable.e00197,
            R.drawable.e00198,
            R.drawable.e00199,
            R.drawable.e00200,
            R.drawable.e00201,
            R.drawable.e00202,
            R.drawable.e00203,
            R.drawable.e00204,
            R.drawable.e00205,
            R.drawable.e00206,
            R.drawable.e00207,
            R.drawable.e00208,
            R.drawable.e00209,
            R.drawable.e00210,
            R.drawable.e00211,
            R.drawable.e00212,
            R.drawable.e00213,
            R.drawable.e00214,
            R.drawable.e00215,
            R.drawable.e00216,
            R.drawable.e00217,
            R.drawable.e00218,
            R.drawable.e00219,
            R.drawable.e00220,
            R.drawable.e00221,
            R.drawable.e00222,
            R.drawable.e00223,
            R.drawable.e00224,
            R.drawable.e00225,
            R.drawable.e00226,
            R.drawable.e00227,
            R.drawable.e00228,
            R.drawable.e00229,
            R.drawable.e00230,
            R.drawable.e00231,
            R.drawable.e00232,
            R.drawable.e00233,
            R.drawable.e00234,
            R.drawable.e00235,
            R.drawable.e00236,
            R.drawable.e00237,
            R.drawable.e00238,
            R.drawable.e00239,
            R.drawable.e00240,
            R.drawable.e00241,
            R.drawable.e00242,
            R.drawable.e00243,
            R.drawable.e00244,
            R.drawable.e00245,
            R.drawable.e00246,
            R.drawable.e00247,
            R.drawable.e00248,
            R.drawable.e00249,
            R.drawable.e00250,
            R.drawable.e00251,
            R.drawable.e00252,
            R.drawable.e00253,
            R.drawable.e00254,
            R.drawable.e00255,
            R.drawable.e00256,
            R.drawable.e00257,
            R.drawable.e00258,
            R.drawable.e00259,
            R.drawable.e00260,
            R.drawable.e00261,
            R.drawable.e00262,
            R.drawable.e00263,
            R.drawable.e00264,
            R.drawable.e00265,
            R.drawable.e00266,
            R.drawable.e00267,
            R.drawable.e00268,
            R.drawable.e00269,
            R.drawable.e00270,
            R.drawable.e00271,
            R.drawable.e00272,
            R.drawable.e00273,
            R.drawable.e00274,
            R.drawable.e00275,
            R.drawable.e00276,
            R.drawable.e00277,
            R.drawable.e00278,
            R.drawable.e00279,
            R.drawable.e00280,
            R.drawable.e00281,
            R.drawable.e00282,
            R.drawable.e00283,
            R.drawable.e00284,
            R.drawable.e00285,
            R.drawable.e00286,
            R.drawable.e00287,
            R.drawable.e00288,
            R.drawable.e00289,
            R.drawable.e00290,
            R.drawable.e00291,
            R.drawable.e00292,
            R.drawable.e00293,
            R.drawable.e00294,
            R.drawable.e00295,
            R.drawable.e00296,
            R.drawable.e00297,
            R.drawable.e00298,
            R.drawable.e00299,
            R.drawable.e00300,
            R.drawable.e00301,
            R.drawable.e00302,
            R.drawable.e00303,
            R.drawable.e00304,
            R.drawable.e00305,
            R.drawable.e00306,
            R.drawable.e00307,
            R.drawable.e00308,
            R.drawable.e00309,
            R.drawable.e00310,
            R.drawable.e00311,
            R.drawable.e00312,
            R.drawable.e00313,
            R.drawable.e00314,
            R.drawable.e00315,
            R.drawable.e00316,
            R.drawable.e00317,
            R.drawable.e00318,
            R.drawable.e00319,
            R.drawable.e00320,
            R.drawable.e00321,
            R.drawable.e00322,
            R.drawable.e00323,
            R.drawable.e00324,
            R.drawable.e00325,
            R.drawable.e00326,
            R.drawable.e00327,
            R.drawable.e00328,
            R.drawable.e00329,
            R.drawable.e00330,
            R.drawable.e00331,
            R.drawable.e00332,
            R.drawable.e00333,
            R.drawable.e00334,
            R.drawable.e00335,
            R.drawable.e00336,
            R.drawable.e00337,
            R.drawable.e00338,
            R.drawable.e00339,
            R.drawable.e00340,
            R.drawable.e00341,
            R.drawable.e00342,
            R.drawable.e00343,
            R.drawable.e00344,
            R.drawable.e00345,
            R.drawable.e00346,
            R.drawable.e00347,
            R.drawable.e00348,
            R.drawable.e00349,
            R.drawable.e00350,
            R.drawable.e00351,
            R.drawable.e00352,
            R.drawable.e00353,
            R.drawable.e00354,
            R.drawable.e00355,
            R.drawable.e00356,
            R.drawable.e00357,
            R.drawable.e00358,
            R.drawable.e00359,
            R.drawable.e00360,
            R.drawable.e00361,
            R.drawable.e00362,
            R.drawable.e00363,
            R.drawable.e00364,
            R.drawable.e00365,
            R.drawable.e00366,
            R.drawable.e00367,
            R.drawable.e00368,
            R.drawable.e00369,
            R.drawable.e00370,
            R.drawable.e00371,
            R.drawable.e00372,
            R.drawable.e00373,
            R.drawable.e00374,
            R.drawable.e00375,
            R.drawable.e00376,
            R.drawable.e00377,
            R.drawable.e00378,
            R.drawable.e00379,
            R.drawable.e00380,
            R.drawable.e00381,
            R.drawable.e00382,
            R.drawable.e00383,
            R.drawable.e00384,
            R.drawable.e00385,
            R.drawable.e00386,
            R.drawable.e00387,
            R.drawable.e00388,
            R.drawable.e00389,
            R.drawable.e00390,
            R.drawable.e00391,
            R.drawable.e00392,
            R.drawable.e00393,
            R.drawable.e00394,
            R.drawable.e00395,
            R.drawable.e00396,
            R.drawable.e00397,
            R.drawable.e00398,
            R.drawable.e00399,
            R.drawable.e00400,
            R.drawable.e00401,
            R.drawable.e00402,
            R.drawable.e00403,
            R.drawable.e00404,
            R.drawable.e00405,
            R.drawable.e00406,
            R.drawable.e00407,
            R.drawable.e00408,
            R.drawable.e00409,
            R.drawable.e00410,
            R.drawable.e00411,
            R.drawable.e00412,
            R.drawable.e00413,
            R.drawable.e00414,
            R.drawable.e00415,
            R.drawable.e00416,
            R.drawable.e00417,
            R.drawable.e00418,
            R.drawable.e00419,
            R.drawable.e00420,
            R.drawable.e00421,
            R.drawable.e00422,
            R.drawable.e00423,
            R.drawable.e00424,
            R.drawable.e00425,
            R.drawable.e00426,
            R.drawable.e00427,
            R.drawable.e00428,
            R.drawable.e00429,
            R.drawable.e00430,
            R.drawable.e00431,
            R.drawable.e00432,
            R.drawable.e00433,
            R.drawable.e00434,
            R.drawable.e00435,
            R.drawable.e00436,
            R.drawable.e00437,
            R.drawable.e00438,
            R.drawable.e00439,
            R.drawable.e00440,
            R.drawable.e00441,
            R.drawable.e00442,
            R.drawable.e00443,
            R.drawable.e00444,
            R.drawable.e00445,
            R.drawable.e00446,
            R.drawable.e00447,
            R.drawable.e00448,
            R.drawable.e00449,
            R.drawable.e00450,
            R.drawable.e00451,
            R.drawable.e00452,
            R.drawable.e00453,
            R.drawable.e00454,
            R.drawable.e00455,
            R.drawable.e00456,
            R.drawable.e00457,
            R.drawable.e00458,
            R.drawable.e00459,
            R.drawable.e00460,
            R.drawable.e00461,
            R.drawable.e00462,
            R.drawable.e00463,
            R.drawable.e00464,
            R.drawable.e00465,
            R.drawable.e00466,
            R.drawable.e00467,
            R.drawable.e00468,
            R.drawable.e00469,

    };

    LinearLayout colorsLayout;
    CustomizeFillImagesListAdapter adapter;
    GridView fillImages;
    AppCompatImageView stroke;
    AppCompatImageView fill;
    TextView title;

    int selectedFill;

    public static CustomizeDialog newInstance() {
        CustomizeDialog fragment = new CustomizeDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showDialog(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        CustomizeDialog myDialogFragment = CustomizeDialog.newInstance();
        myDialogFragment.show(fm, TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        getDialog().getWindow().setLayout((int) (displayMetrics.widthPixels * 0.85f), (int) (displayMetrics.heightPixels * 0.85f));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_customize, container, false);

        colorsLayout = contentView.findViewById(R.id.colors);
        fillImages = contentView.findViewById(R.id.fill_images);
        stroke = contentView.findViewById(R.id.stroke);
        fill = contentView.findViewById(R.id.fill);
        title = contentView.findViewById(R.id.title_txt);

        String lang = Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE);
        int langCode = I18N.getLangCode(lang);
        title.setText(I18N.texts[langCode][I18N.customize]);

        int orientation = getActivity().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            fillImages.setNumColumns(5);
        } else {
            fillImages.setNumColumns(10);
        }

        adapter = new CustomizeFillImagesListAdapter(getContext(), FILL_IMAGES);
        fillImages.setAdapter(adapter);


        int i = 0;
        for (int color : ColorUtil.STROKE_COLORS) {
            View view = new View(getContext());
            view.setBackgroundColor(color);
            float colorsSize = getResources().getDimension(R.dimen.dialog_customize_colors_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) colorsSize, (int) colorsSize);
            view.setLayoutParams(params);
            view.setTag(i);
            view.setOnClickListener(colorSelectOnClick);
            colorsLayout.addView(view);
            i++;
        }

        adapter.selectedColor = Shared.getInstance(getContext()).getInt(Constants.KEY_SELECTED_COLOR, 0);

        stroke.setColorFilter(ColorUtil.STROKE_COLORS[adapter.selectedColor]);

        selectedFill = Shared.getInstance(getContext()).getInt(Constants.KEY_SELECTED_FILL, 0);

        if (selectedFill == 0) {
            fill.setImageResource(R.drawable.circle);
            fill.setColorFilter(ColorUtil.FILL_COLORS[adapter.selectedColor]);
        } else {
            if (selectedFill >= adapter.data.length) selectedFill = 1;
            Glide.with(getContext()).load(adapter.data[selectedFill]).into(fill);
            fill.setColorFilter(0);
        }

        fillImages.setOnItemClickListener((parent, view, position, id) -> {
            selectedFill = position;
            Shared.getInstance(getContext()).setInt(Constants.KEY_SELECTED_FILL, selectedFill).commit();
            if (selectedFill == 0) {
                fill.setImageResource(R.drawable.circle);
                fill.setColorFilter(ColorUtil.FILL_COLORS[adapter.selectedColor]);
            } else {
                Glide.with(getContext()).load(adapter.data[selectedFill]).into(fill);
                fill.setColorFilter(0);
            }
        });

        return contentView;
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

    View.OnClickListener colorSelectOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int color = (int) v.getTag();
            Shared.getInstance(getContext()).setInt(Constants.KEY_SELECTED_COLOR, color).commit();
            adapter.selectedColor = color;
            stroke.setColorFilter(ColorUtil.STROKE_COLORS[adapter.selectedColor]);
            if (selectedFill == 0) {
                fill.setImageResource(R.drawable.circle);
                fill.setColorFilter(ColorUtil.FILL_COLORS[adapter.selectedColor]);
            }
            adapter.notifyDataSetChanged();
        }
    };
}