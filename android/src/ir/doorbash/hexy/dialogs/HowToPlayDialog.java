package ir.doorbash.hexy.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import ir.doorbash.hexy.R;
import ir.doorbash.hexy.fragments.HowToPlayImageFragment;

public class HowToPlayDialog extends DialogFragment {

    private static final String TAG = "HowToPlayDialog";

    ViewPager viewPager;
    AppCompatImageView next;
    AppCompatImageView prev;
    AppCompatImageView close;

    public static HowToPlayDialog newInstance() {
        HowToPlayDialog fragment = new HowToPlayDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showDialog(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        HowToPlayDialog myDialogFragment = HowToPlayDialog.newInstance();
        myDialogFragment.show(fm, TAG);
    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes(lp);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_how, container, false);

        viewPager = contentView.findViewById(R.id.viewpager);
        next = contentView.findViewById(R.id.next);
        prev = contentView.findViewById(R.id.prev);
        close = contentView.findViewById(R.id.close);

        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());

        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) prev.setVisibility(View.INVISIBLE);
                else prev.setVisibility(View.VISIBLE);
                if (i == 2) next.setVisibility(View.INVISIBLE);
                else next.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        viewPager.post(() -> {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) viewPager.getLayoutParams();
            layoutParams.width = (int) (screenWidth * 0.9f);
            layoutParams.height = (int) (layoutParams.width * 0.56f);
            viewPager.setLayoutParams(layoutParams);
            prev.setVisibility(View.INVISIBLE);
        });

        next.setOnClickListener(view -> {
            int nextItem = viewPager.getCurrentItem() + 1;
            if (nextItem > 2) return;
            viewPager.setCurrentItem(nextItem, true);
        });

        prev.setOnClickListener(view -> {
            int prevItem = viewPager.getCurrentItem() - 1;
            if (prevItem < 0) return;
            viewPager.setCurrentItem(prevItem, true);
        });

        close.setOnClickListener(view -> {
            dismiss();
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

    public class PagerAdapter extends FragmentPagerAdapter {

        String[] titles;
        Fragment[] fragments = new Fragment[3];

        PagerAdapter(FragmentManager fm) {
            super(fm);
            fragments[0] = HowToPlayImageFragment.newInstance(R.drawable.ui_h_1);
            fragments[1] = HowToPlayImageFragment.newInstance(R.drawable.ui_h_2);
            fragments[2] = HowToPlayImageFragment.newInstance(R.drawable.ui_h_3);
        }

        @Override
        public Fragment getItem(int num) {
            return fragments[num];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        void setTitles(String[] titles) {
            this.titles = titles;
        }
    }
}
