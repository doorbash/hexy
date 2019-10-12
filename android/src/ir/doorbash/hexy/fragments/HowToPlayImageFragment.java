package ir.doorbash.hexy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import ir.doorbash.hexy.R;

public class HowToPlayImageFragment extends Fragment {

    public static HowToPlayImageFragment newInstance(int imageId) {
        HowToPlayImageFragment fragment = new HowToPlayImageFragment();
        Bundle args = new Bundle();
        args.putInt("imageId", imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_how_image, container, false);
        int imageId = getArguments().getInt("imageId", R.drawable.ui_h_1);
        AppCompatImageView image = contentView.findViewById(R.id.image);
        image.setImageResource(imageId); // TODO: change this to Glide
        return contentView;
    }

}
