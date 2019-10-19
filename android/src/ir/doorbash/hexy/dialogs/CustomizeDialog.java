package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import ir.doorbash.hexy.MainMenuActivity;
import ir.doorbash.hexy.R;
import ir.doorbash.hexy.adapter.CustomizeDialogImagesListAdapter;
import ir.doorbash.hexy.adapter.CustomizeDialogImagesListAdapter.Data;
import ir.doorbash.hexy.util.ColorUtil;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.Shared;

public class CustomizeDialog extends DialogFragment {

    private static final String TAG = "CustomizeDialog";
    private static final int NUM_IMAGES = 470;

    public static final Data[] FILL_IMAGES = new Data[]{
            new Data("Recently used"),
//            new Data(),
//            new Data(),
//            new Data(),
//            new Data(),
            new Data(0, 0, 0),
            new Data(R.drawable.e00000, 0, 0),
            new Data(R.drawable.e00001, 1, 0),
            new Data(R.drawable.e00002, 2, 0),
            new Data(R.drawable.e00003, 3, 0),
            new Data(R.drawable.e00004, 4, 0),
            new Data(R.drawable.e00005, 5, 0),
            new Data(R.drawable.e00006, 6, 0),
            new Data(R.drawable.e00007, 7, 0),
//            new Data(),
            new Data("All items"),
//            new Data(),
//            new Data(),
//            new Data(),
//            new Data(),
            new Data(0, 0, 500),
            new Data(R.drawable.e00000, 0, 500),
            new Data(R.drawable.e00001, 1, 500),
            new Data(R.drawable.e00002, 2, 500),
            new Data(R.drawable.e00003, 3, 500),
            new Data(R.drawable.e00004, 4, 500),
            new Data(R.drawable.e00005, 5, 500),
            new Data(R.drawable.e00006, 6, 500),
            new Data(R.drawable.e00007, 7, 500),
            new Data(R.drawable.e00008, 8, 500),
            new Data(R.drawable.e00009, 9, 500),
            new Data(R.drawable.e00010, 10, 500),
            new Data(R.drawable.e00011, 11, 500),
            new Data(R.drawable.e00012, 12, 500),
            new Data(R.drawable.e00013, 13, 500),
            new Data(R.drawable.e00014, 14, 500),
            new Data(R.drawable.e00015, 15, 500),
            new Data(R.drawable.e00016, 16, 500),
            new Data(R.drawable.e00017, 17, 500),
            new Data(R.drawable.e00018, 18, 500),
            new Data(R.drawable.e00019, 19, 500),
            new Data(R.drawable.e00020, 20, 500),
            new Data(R.drawable.e00021, 21, 500),
            new Data(R.drawable.e00022, 22, 500),
            new Data(R.drawable.e00023, 23, 500),
            new Data(R.drawable.e00024, 24, 500),
            new Data(R.drawable.e00025, 25, 500),
            new Data(R.drawable.e00026, 26, 500),
            new Data(R.drawable.e00027, 27, 500),
            new Data(R.drawable.e00028, 28, 500),
            new Data(R.drawable.e00029, 29, 500),
            new Data(R.drawable.e00030, 30, 500),
            new Data(R.drawable.e00031, 31, 500),
            new Data(R.drawable.e00032, 32, 500),
            new Data(R.drawable.e00033, 33, 500),
            new Data(R.drawable.e00034, 34, 500),
            new Data(R.drawable.e00035, 35, 500),
            new Data(R.drawable.e00036, 36, 500),
            new Data(R.drawable.e00037, 37, 500),
            new Data(R.drawable.e00038, 38, 500),
            new Data(R.drawable.e00039, 39, 500),
            new Data(R.drawable.e00040, 40, 500),
            new Data(R.drawable.e00041, 41, 500),
            new Data(R.drawable.e00042, 42, 500),
            new Data(R.drawable.e00043, 43, 500),
            new Data(R.drawable.e00044, 44, 500),
            new Data(R.drawable.e00045, 45, 500),
            new Data(R.drawable.e00046, 46, 500),
            new Data(R.drawable.e00047, 47, 500),
            new Data(R.drawable.e00048, 48, 500),
            new Data(R.drawable.e00049, 49, 500),
            new Data(R.drawable.e00050, 50, 500),
            new Data(R.drawable.e00051, 51, 500),
            new Data(R.drawable.e00052, 52, 500),
            new Data(R.drawable.e00053, 53, 500),
            new Data(R.drawable.e00054, 54, 500),
            new Data(R.drawable.e00055, 55, 500),
            new Data(R.drawable.e00056, 56, 500),
            new Data(R.drawable.e00057, 57, 500),
            new Data(R.drawable.e00058, 58, 500),
            new Data(R.drawable.e00059, 59, 500),
            new Data(R.drawable.e00060, 60, 500),
            new Data(R.drawable.e00061, 61, 500),
            new Data(R.drawable.e00062, 62, 500),
            new Data(R.drawable.e00063, 63, 500),
            new Data(R.drawable.e00064, 64, 500),
            new Data(R.drawable.e00065, 65, 500),
            new Data(R.drawable.e00066, 66, 500),
            new Data(R.drawable.e00067, 67, 500),
            new Data(R.drawable.e00068, 68, 500),
            new Data(R.drawable.e00069, 69, 500),
            new Data(R.drawable.e00070, 70, 500),
            new Data(R.drawable.e00071, 71, 500),
            new Data(R.drawable.e00072, 72, 500),
            new Data(R.drawable.e00073, 73, 500),
            new Data(R.drawable.e00074, 74, 500),
            new Data(R.drawable.e00075, 75, 500),
            new Data(R.drawable.e00076, 76, 500),
            new Data(R.drawable.e00077, 77, 500),
            new Data(R.drawable.e00078, 78, 500),
            new Data(R.drawable.e00079, 79, 500),
            new Data(R.drawable.e00080, 80, 500),
            new Data(R.drawable.e00081, 81, 500),
            new Data(R.drawable.e00082, 82, 500),
            new Data(R.drawable.e00083, 83, 500),
            new Data(R.drawable.e00084, 84, 500),
            new Data(R.drawable.e00085, 85, 500),
            new Data(R.drawable.e00086, 86, 500),
            new Data(R.drawable.e00087, 87, 500),
            new Data(R.drawable.e00088, 88, 500),
            new Data(R.drawable.e00089, 89, 500),
            new Data(R.drawable.e00090, 90, 500),
            new Data(R.drawable.e00091, 91, 500),
            new Data(R.drawable.e00092, 92, 500),
            new Data(R.drawable.e00093, 93, 500),
            new Data(R.drawable.e00094, 94, 500),
            new Data(R.drawable.e00095, 95, 500),
            new Data(R.drawable.e00096, 96, 500),
            new Data(R.drawable.e00097, 97, 500),
            new Data(R.drawable.e00098, 98, 500),
            new Data(R.drawable.e00099, 99, 500),
            new Data(R.drawable.e00100, 100, 500),
            new Data(R.drawable.e00101, 101, 500),
            new Data(R.drawable.e00102, 102, 500),
            new Data(R.drawable.e00103, 103, 500),
            new Data(R.drawable.e00104, 104, 500),
            new Data(R.drawable.e00105, 105, 500),
            new Data(R.drawable.e00106, 106, 500),
            new Data(R.drawable.e00107, 107, 500),
            new Data(R.drawable.e00108, 108, 500),
            new Data(R.drawable.e00109, 109, 500),
            new Data(R.drawable.e00110, 110, 500),
            new Data(R.drawable.e00111, 111, 500),
            new Data(R.drawable.e00112, 112, 500),
            new Data(R.drawable.e00113, 113, 500),
            new Data(R.drawable.e00114, 114, 500),
            new Data(R.drawable.e00115, 115, 500),
            new Data(R.drawable.e00116, 116, 500),
            new Data(R.drawable.e00117, 117, 500),
            new Data(R.drawable.e00118, 118, 500),
            new Data(R.drawable.e00119, 119, 500),
            new Data(R.drawable.e00120, 120, 500),
            new Data(R.drawable.e00121, 121, 500),
            new Data(R.drawable.e00122, 122, 500),
            new Data(R.drawable.e00123, 123, 500),
            new Data(R.drawable.e00124, 124, 500),
            new Data(R.drawable.e00125, 125, 500),
            new Data(R.drawable.e00126, 126, 500),
            new Data(R.drawable.e00127, 127, 500),
            new Data(R.drawable.e00128, 128, 500),
            new Data(R.drawable.e00129, 129, 500),
            new Data(R.drawable.e00130, 130, 500),
            new Data(R.drawable.e00131, 131, 500),
            new Data(R.drawable.e00132, 132, 500),
            new Data(R.drawable.e00133, 133, 500),
            new Data(R.drawable.e00134, 134, 500),
            new Data(R.drawable.e00135, 135, 500),
            new Data(R.drawable.e00136, 136, 500),
            new Data(R.drawable.e00137, 137, 500),
            new Data(R.drawable.e00138, 138, 500),
            new Data(R.drawable.e00139, 139, 500),
            new Data(R.drawable.e00140, 140, 500),
            new Data(R.drawable.e00141, 141, 500),
            new Data(R.drawable.e00142, 142, 500),
            new Data(R.drawable.e00143, 143, 500),
            new Data(R.drawable.e00144, 144, 500),
            new Data(R.drawable.e00145, 145, 500),
            new Data(R.drawable.e00146, 146, 500),
            new Data(R.drawable.e00147, 147, 500),
            new Data(R.drawable.e00148, 148, 500),
            new Data(R.drawable.e00149, 149, 500),
            new Data(R.drawable.e00150, 150, 500),
            new Data(R.drawable.e00151, 151, 500),
            new Data(R.drawable.e00152, 152, 500),
            new Data(R.drawable.e00153, 153, 500),
            new Data(R.drawable.e00154, 154, 500),
            new Data(R.drawable.e00155, 155, 500),
            new Data(R.drawable.e00156, 156, 500),
            new Data(R.drawable.e00157, 157, 500),
            new Data(R.drawable.e00158, 158, 500),
            new Data(R.drawable.e00159, 159, 500),
            new Data(R.drawable.e00160, 160, 500),
            new Data(R.drawable.e00161, 161, 500),
            new Data(R.drawable.e00162, 162, 500),
            new Data(R.drawable.e00163, 163, 500),
            new Data(R.drawable.e00164, 164, 500),
            new Data(R.drawable.e00165, 165, 500),
            new Data(R.drawable.e00166, 166, 500),
            new Data(R.drawable.e00167, 167, 500),
            new Data(R.drawable.e00168, 168, 500),
            new Data(R.drawable.e00169, 169, 500),
            new Data(R.drawable.e00170, 170, 500),
            new Data(R.drawable.e00171, 171, 500),
            new Data(R.drawable.e00172, 172, 500),
            new Data(R.drawable.e00173, 173, 500),
            new Data(R.drawable.e00174, 174, 500),
            new Data(R.drawable.e00175, 175, 500),
            new Data(R.drawable.e00176, 176, 500),
            new Data(R.drawable.e00177, 177, 500),
            new Data(R.drawable.e00178, 178, 500),
            new Data(R.drawable.e00179, 179, 500),
            new Data(R.drawable.e00180, 180, 500),
            new Data(R.drawable.e00181, 181, 500),
            new Data(R.drawable.e00182, 182, 500),
            new Data(R.drawable.e00183, 183, 500),
            new Data(R.drawable.e00184, 184, 500),
            new Data(R.drawable.e00185, 185, 500),
            new Data(R.drawable.e00186, 186, 500),
            new Data(R.drawable.e00187, 187, 500),
            new Data(R.drawable.e00188, 188, 500),
            new Data(R.drawable.e00189, 189, 500),
            new Data(R.drawable.e00190, 190, 500),
            new Data(R.drawable.e00191, 191, 500),
            new Data(R.drawable.e00192, 192, 500),
            new Data(R.drawable.e00193, 193, 500),
            new Data(R.drawable.e00194, 194, 500),
            new Data(R.drawable.e00195, 195, 500),
            new Data(R.drawable.e00196, 196, 500),
            new Data(R.drawable.e00197, 197, 500),
            new Data(R.drawable.e00198, 198, 500),
            new Data(R.drawable.e00199, 199, 500),
            new Data(R.drawable.e00200, 200, 500),
            new Data(R.drawable.e00201, 201, 500),
            new Data(R.drawable.e00202, 202, 500),
            new Data(R.drawable.e00203, 203, 500),
            new Data(R.drawable.e00204, 204, 500),
            new Data(R.drawable.e00205, 205, 500),
            new Data(R.drawable.e00206, 206, 500),
            new Data(R.drawable.e00207, 207, 500),
            new Data(R.drawable.e00208, 208, 500),
            new Data(R.drawable.e00209, 209, 500),
            new Data(R.drawable.e00210, 210, 500),
            new Data(R.drawable.e00211, 211, 500),
            new Data(R.drawable.e00212, 212, 500),
            new Data(R.drawable.e00213, 213, 500),
            new Data(R.drawable.e00214, 214, 500),
            new Data(R.drawable.e00215, 215, 500),
            new Data(R.drawable.e00216, 216, 500),
            new Data(R.drawable.e00217, 217, 500),
            new Data(R.drawable.e00218, 218, 500),
            new Data(R.drawable.e00219, 219, 500),
            new Data(R.drawable.e00220, 220, 500),
            new Data(R.drawable.e00221, 221, 500),
            new Data(R.drawable.e00222, 222, 500),
            new Data(R.drawable.e00223, 223, 500),
            new Data(R.drawable.e00224, 224, 500),
            new Data(R.drawable.e00225, 225, 500),
            new Data(R.drawable.e00226, 226, 500),
            new Data(R.drawable.e00227, 227, 500),
            new Data(R.drawable.e00228, 228, 500),
            new Data(R.drawable.e00229, 229, 500),
            new Data(R.drawable.e00230, 230, 500),
            new Data(R.drawable.e00231, 231, 500),
            new Data(R.drawable.e00232, 232, 500),
            new Data(R.drawable.e00233, 233, 500),
            new Data(R.drawable.e00234, 234, 500),
            new Data(R.drawable.e00235, 235, 500),
            new Data(R.drawable.e00236, 236, 500),
            new Data(R.drawable.e00237, 237, 500),
            new Data(R.drawable.e00238, 238, 500),
            new Data(R.drawable.e00239, 239, 500),
            new Data(R.drawable.e00240, 240, 500),
            new Data(R.drawable.e00241, 241, 500),
            new Data(R.drawable.e00242, 242, 500),
            new Data(R.drawable.e00243, 243, 500),
            new Data(R.drawable.e00244, 244, 500),
            new Data(R.drawable.e00245, 245, 500),
            new Data(R.drawable.e00246, 246, 500),
            new Data(R.drawable.e00247, 247, 500),
            new Data(R.drawable.e00248, 248, 500),
            new Data(R.drawable.e00249, 249, 500),
            new Data(R.drawable.e00250, 250, 500),
            new Data(R.drawable.e00251, 251, 500),
            new Data(R.drawable.e00252, 252, 500),
            new Data(R.drawable.e00253, 253, 500),
            new Data(R.drawable.e00254, 254, 500),
            new Data(R.drawable.e00255, 255, 500),
            new Data(R.drawable.e00256, 256, 500),
            new Data(R.drawable.e00257, 257, 500),
            new Data(R.drawable.e00258, 258, 500),
            new Data(R.drawable.e00259, 259, 500),
            new Data(R.drawable.e00260, 260, 500),
            new Data(R.drawable.e00261, 261, 500),
            new Data(R.drawable.e00262, 262, 500),
            new Data(R.drawable.e00263, 263, 500),
            new Data(R.drawable.e00264, 264, 500),
            new Data(R.drawable.e00265, 265, 500),
            new Data(R.drawable.e00266, 266, 500),
            new Data(R.drawable.e00267, 267, 500),
            new Data(R.drawable.e00268, 268, 500),
            new Data(R.drawable.e00269, 269, 500),
            new Data(R.drawable.e00270, 270, 500),
            new Data(R.drawable.e00271, 271, 500),
            new Data(R.drawable.e00272, 272, 500),
            new Data(R.drawable.e00273, 273, 500),
            new Data(R.drawable.e00274, 274, 500),
            new Data(R.drawable.e00275, 275, 500),
            new Data(R.drawable.e00276, 276, 500),
            new Data(R.drawable.e00277, 277, 500),
            new Data(R.drawable.e00278, 278, 500),
            new Data(R.drawable.e00279, 279, 500),
            new Data(R.drawable.e00280, 280, 500),
            new Data(R.drawable.e00281, 281, 500),
            new Data(R.drawable.e00282, 282, 500),
            new Data(R.drawable.e00283, 283, 500),
            new Data(R.drawable.e00284, 284, 500),
            new Data(R.drawable.e00285, 285, 500),
            new Data(R.drawable.e00286, 286, 500),
            new Data(R.drawable.e00287, 287, 500),
            new Data(R.drawable.e00288, 288, 500),
            new Data(R.drawable.e00289, 289, 500),
            new Data(R.drawable.e00290, 290, 500),
            new Data(R.drawable.e00291, 291, 500),
            new Data(R.drawable.e00292, 292, 500),
            new Data(R.drawable.e00293, 293, 500),
            new Data(R.drawable.e00294, 294, 500),
            new Data(R.drawable.e00295, 295, 500),
            new Data(R.drawable.e00296, 296, 500),
            new Data(R.drawable.e00297, 297, 500),
            new Data(R.drawable.e00298, 298, 500),
            new Data(R.drawable.e00299, 299, 500),
            new Data(R.drawable.e00300, 300, 500),
            new Data(R.drawable.e00301, 301, 500),
            new Data(R.drawable.e00302, 302, 500),
            new Data(R.drawable.e00303, 303, 500),
            new Data(R.drawable.e00304, 304, 500),
            new Data(R.drawable.e00305, 305, 500),
            new Data(R.drawable.e00306, 306, 500),
            new Data(R.drawable.e00307, 307, 500),
            new Data(R.drawable.e00308, 308, 500),
            new Data(R.drawable.e00309, 309, 500),
            new Data(R.drawable.e00310, 310, 500),
            new Data(R.drawable.e00311, 311, 500),
            new Data(R.drawable.e00312, 312, 500),
            new Data(R.drawable.e00313, 313, 500),
            new Data(R.drawable.e00314, 314, 500),
            new Data(R.drawable.e00315, 315, 500),
            new Data(R.drawable.e00316, 316, 500),
            new Data(R.drawable.e00317, 317, 500),
            new Data(R.drawable.e00318, 318, 500),
            new Data(R.drawable.e00319, 319, 500),
            new Data(R.drawable.e00320, 320, 500),
            new Data(R.drawable.e00321, 321, 500),
            new Data(R.drawable.e00322, 322, 500),
            new Data(R.drawable.e00323, 323, 500),
            new Data(R.drawable.e00324, 324, 500),
            new Data(R.drawable.e00325, 325, 500),
            new Data(R.drawable.e00326, 326, 500),
            new Data(R.drawable.e00327, 327, 500),
            new Data(R.drawable.e00328, 328, 500),
            new Data(R.drawable.e00329, 329, 500),
            new Data(R.drawable.e00330, 330, 500),
            new Data(R.drawable.e00331, 331, 500),
            new Data(R.drawable.e00332, 332, 500),
            new Data(R.drawable.e00333, 333, 500),
            new Data(R.drawable.e00334, 334, 500),
            new Data(R.drawable.e00335, 335, 500),
            new Data(R.drawable.e00336, 336, 500),
            new Data(R.drawable.e00337, 337, 500),
            new Data(R.drawable.e00338, 338, 500),
            new Data(R.drawable.e00339, 339, 500),
            new Data(R.drawable.e00340, 340, 500),
            new Data(R.drawable.e00341, 341, 500),
            new Data(R.drawable.e00342, 342, 500),
            new Data(R.drawable.e00343, 343, 500),
            new Data(R.drawable.e00344, 344, 500),
            new Data(R.drawable.e00345, 345, 500),
            new Data(R.drawable.e00346, 346, 500),
            new Data(R.drawable.e00347, 347, 500),
            new Data(R.drawable.e00348, 348, 500),
            new Data(R.drawable.e00349, 349, 500),
            new Data(R.drawable.e00350, 350, 500),
            new Data(R.drawable.e00351, 351, 500),
            new Data(R.drawable.e00352, 352, 500),
            new Data(R.drawable.e00353, 353, 500),
            new Data(R.drawable.e00354, 354, 500),
            new Data(R.drawable.e00355, 355, 500),
            new Data(R.drawable.e00356, 356, 500),
            new Data(R.drawable.e00357, 357, 500),
            new Data(R.drawable.e00358, 358, 500),
            new Data(R.drawable.e00359, 359, 500),
            new Data(R.drawable.e00360, 360, 500),
            new Data(R.drawable.e00361, 361, 500),
            new Data(R.drawable.e00362, 362, 500),
            new Data(R.drawable.e00363, 363, 500),
            new Data(R.drawable.e00364, 364, 500),
            new Data(R.drawable.e00365, 365, 500),
            new Data(R.drawable.e00366, 366, 500),
            new Data(R.drawable.e00367, 367, 500),
            new Data(R.drawable.e00368, 368, 500),
            new Data(R.drawable.e00369, 369, 500),
            new Data(R.drawable.e00370, 370, 500),
            new Data(R.drawable.e00371, 371, 500),
            new Data(R.drawable.e00372, 372, 500),
            new Data(R.drawable.e00373, 373, 500),
            new Data(R.drawable.e00374, 374, 500),
            new Data(R.drawable.e00375, 375, 500),
            new Data(R.drawable.e00376, 376, 500),
            new Data(R.drawable.e00377, 377, 500),
            new Data(R.drawable.e00378, 378, 500),
            new Data(R.drawable.e00379, 379, 500),
            new Data(R.drawable.e00380, 380, 500),
            new Data(R.drawable.e00381, 381, 500),
            new Data(R.drawable.e00382, 382, 500),
            new Data(R.drawable.e00383, 383, 500),
            new Data(R.drawable.e00384, 384, 500),
            new Data(R.drawable.e00385, 385, 500),
            new Data(R.drawable.e00386, 386, 500),
            new Data(R.drawable.e00387, 387, 500),
            new Data(R.drawable.e00388, 388, 500),
            new Data(R.drawable.e00389, 389, 500),
            new Data(R.drawable.e00390, 390, 500),
            new Data(R.drawable.e00391, 391, 500),
            new Data(R.drawable.e00392, 392, 500),
            new Data(R.drawable.e00393, 393, 500),
            new Data(R.drawable.e00394, 394, 500),
            new Data(R.drawable.e00395, 395, 500),
            new Data(R.drawable.e00396, 396, 500),
            new Data(R.drawable.e00397, 397, 500),
            new Data(R.drawable.e00398, 398, 500),
            new Data(R.drawable.e00399, 399, 500),
            new Data(R.drawable.e00400, 400, 500),
            new Data(R.drawable.e00401, 401, 500),
            new Data(R.drawable.e00402, 402, 500),
            new Data(R.drawable.e00403, 403, 500),
            new Data(R.drawable.e00404, 404, 500),
            new Data(R.drawable.e00405, 405, 500),
            new Data(R.drawable.e00406, 406, 500),
            new Data(R.drawable.e00407, 407, 500),
            new Data(R.drawable.e00408, 408, 500),
            new Data(R.drawable.e00409, 409, 500),
            new Data(R.drawable.e00410, 410, 500),
            new Data(R.drawable.e00411, 411, 500),
            new Data(R.drawable.e00412, 412, 500),
            new Data(R.drawable.e00413, 413, 500),
            new Data(R.drawable.e00414, 414, 500),
            new Data(R.drawable.e00415, 415, 500),
            new Data(R.drawable.e00416, 416, 500),
            new Data(R.drawable.e00417, 417, 500),
            new Data(R.drawable.e00418, 418, 500),
            new Data(R.drawable.e00419, 419, 500),
            new Data(R.drawable.e00420, 420, 500),
            new Data(R.drawable.e00421, 421, 500),
            new Data(R.drawable.e00422, 422, 500),
            new Data(R.drawable.e00423, 423, 500),
            new Data(R.drawable.e00424, 424, 500),
            new Data(R.drawable.e00425, 425, 500),
            new Data(R.drawable.e00426, 426, 500),
            new Data(R.drawable.e00427, 427, 500),
            new Data(R.drawable.e00428, 428, 500),
            new Data(R.drawable.e00429, 429, 500),
            new Data(R.drawable.e00430, 430, 500),
            new Data(R.drawable.e00431, 431, 500),
            new Data(R.drawable.e00432, 432, 500),
            new Data(R.drawable.e00433, 433, 500),
            new Data(R.drawable.e00434, 434, 500),
            new Data(R.drawable.e00435, 435, 500),
            new Data(R.drawable.e00436, 436, 500),
            new Data(R.drawable.e00437, 437, 500),
            new Data(R.drawable.e00438, 438, 500),
            new Data(R.drawable.e00439, 439, 500),
            new Data(R.drawable.e00440, 440, 500),
            new Data(R.drawable.e00441, 441, 500),
            new Data(R.drawable.e00442, 442, 500),
            new Data(R.drawable.e00443, 443, 500),
            new Data(R.drawable.e00444, 444, 500),
            new Data(R.drawable.e00445, 445, 500),
            new Data(R.drawable.e00446, 446, 500),
            new Data(R.drawable.e00447, 447, 500),
            new Data(R.drawable.e00448, 448, 500),
            new Data(R.drawable.e00449, 449, 500),
            new Data(R.drawable.e00450, 450, 500),
            new Data(R.drawable.e00451, 451, 500),
            new Data(R.drawable.e00452, 452, 500),
            new Data(R.drawable.e00453, 453, 500),
            new Data(R.drawable.e00454, 454, 500),
            new Data(R.drawable.e00455, 455, 500),
            new Data(R.drawable.e00456, 456, 500),
            new Data(R.drawable.e00457, 457, 500),
            new Data(R.drawable.e00458, 458, 500),
            new Data(R.drawable.e00459, 459, 500),
            new Data(R.drawable.e00460, 460, 500),
            new Data(R.drawable.e00461, 461, 500),
            new Data(R.drawable.e00462, 462, 500),
            new Data(R.drawable.e00463, 463, 500),
            new Data(R.drawable.e00464, 464, 500),
            new Data(R.drawable.e00465, 465, 500),
            new Data(R.drawable.e00466, 466, 500),
            new Data(R.drawable.e00467, 467, 500),
            new Data(R.drawable.e00468, 468, 500),
            new Data(R.drawable.e00469, 469, 500),
    };

    LinearLayout colorsLayout;
    private CustomizeDialogImagesListAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager portraitGridLayoutManager;
    private GridLayoutManager landscapeGridLayoutManager;
    AppCompatImageView stroke;
    AppCompatImageView fill;
    TextView title;
    Button okButton;

    int selectedImageIndex;
    int selectedImageResId;

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
        recyclerView = contentView.findViewById(R.id.images_list);
        stroke = contentView.findViewById(R.id.stroke);
        fill = contentView.findViewById(R.id.fill);
        title = contentView.findViewById(R.id.title_txt);
        okButton = contentView.findViewById(R.id.ok_btn);

        recyclerView.setHasFixedSize(true);
        portraitGridLayoutManager = new GridLayoutManager(getContext(), 5);
        landscapeGridLayoutManager = new GridLayoutManager(getContext(), 10);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        portraitGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0 || position == 10) {
                    return dpWidth < 680 ? 5 : 10;
                }
                return 1;
            }
        });

        adapter = new CustomizeDialogImagesListAdapter(getContext(), FILL_IMAGES);
        recyclerView.setAdapter(adapter);

        String lang = Shared.getInstance(getContext()).getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE);
        int langCode = I18N.getLangCode(lang);
        title.setText(I18N.texts[langCode][I18N.customize]);
        okButton.setText(I18N.texts[langCode][I18N.ok]);

        //int orientation = getActivity().getResources().getConfiguration().orientation;


        if (/*orientation == Configuration.ORIENTATION_PORTRAIT*/dpWidth < 680) {
            recyclerView.setLayoutManager(portraitGridLayoutManager);
        } else {
            recyclerView.setLayoutManager(landscapeGridLayoutManager);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.invalidateItemDecorations();
                }
            }
        });

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

        selectedImageIndex = Shared.getInstance(getContext()).getInt(Constants.KEY_SELECTED_IMAGE_INDEX, 0);

        if (selectedImageResId == 0) {
            fill.setImageResource(R.drawable.circle);
            fill.setColorFilter(ColorUtil.FILL_COLORS[adapter.selectedColor]);
        } else {
            if (selectedImageIndex >= NUM_IMAGES) selectedImageIndex = 1;
            Glide.with(getContext()).load(selectedImageResId).into(fill);
            fill.setColorFilter(0);
        }

        adapter.setOnItemClickListener(data -> {
            selectedImageIndex = data.imageCode;
            selectedImageResId = data.resId;
            Shared.getInstance(getContext())
                    .setInt(Constants.KEY_SELECTED_IMAGE_INDEX, selectedImageIndex)
                    .setInt(Constants.KEY_SELECTED_IMAGE_RES_ID, selectedImageResId)
                    .commit();
            if (selectedImageResId == 0) {
                fill.setImageResource(R.drawable.circle);
                fill.setColorFilter(ColorUtil.FILL_COLORS[adapter.selectedColor]);
            } else {
                Glide.with(getContext()).load(data.resId).into(fill);
                fill.setColorFilter(0);
            }
        });

        okButton.setOnClickListener(v -> dismiss());

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

    private View.OnClickListener colorSelectOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int color = (int) v.getTag();
            Shared.getInstance(getContext()).setInt(Constants.KEY_SELECTED_COLOR, color).commit();
            adapter.selectedColor = color;
            stroke.setColorFilter(ColorUtil.STROKE_COLORS[adapter.selectedColor]);
            if (selectedImageResId == 0) {
                fill.setImageResource(R.drawable.circle);
                fill.setColorFilter(ColorUtil.FILL_COLORS[adapter.selectedColor]);
            }
            adapter.notifyDataSetChanged();
        }
    };
}