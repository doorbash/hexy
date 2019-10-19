package ir.doorbash.hexy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;

import ir.doorbash.hexy.R;
import ir.doorbash.hexy.util.ColorUtil;

public class CustomizeDialogImagesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Data[] data;
    private Context context;
    public int selectedColor;
    public Listener listener;

    public interface Listener {
        void onClick(Data data);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView stroke_img;
        public ImageView image;
        public LinearLayout priceLayout;
        public TextView price;

        public ImageViewHolder(View v) {
            super(v);
            priceLayout = v.findViewById(R.id.price_layout);
            price = v.findViewById(R.id.price);
            stroke_img = v.findViewById(R.id.stroke_img);
            image = v.findViewById(R.id.img);
        }
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {

        public TextView text;

        public TextViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.text);
        }
    }

    public CustomizeDialogImagesListAdapter(Context context, Data[] data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return data[position].type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        RecyclerView.ViewHolder vh = null;
        switch (viewType) {
            case 0:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customize_fill_images, parent, false);
                vh = new ImageViewHolder(itemView);
                break;
            case 1:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customize_images_list_titles, parent, false);
                vh = new TextViewHolder(itemView);
        }
        if (itemView == null) return null;
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Data item = data[position];
        if (item.type == 0) {
            ImageViewHolder vh = (ImageViewHolder) holder;
            if (item.resId == 0) {
                vh.image.setImageResource(R.drawable.circle);
                vh.image.setColorFilter(ColorUtil.FILL_COLORS[selectedColor]);
                vh.priceLayout.setVisibility(View.INVISIBLE);
            } else {
                Glide.with(context).load(item.resId).into(vh.image);
                vh.image.setColorFilter(0);
                if (item.price == 0) {
                    vh.priceLayout.setVisibility(View.INVISIBLE);
                } else {
                    vh.priceLayout.setVisibility(View.VISIBLE);
                    vh.price.setText(String.valueOf(item.price));
                }
            }
            vh.stroke_img.setColorFilter(ColorUtil.STROKE_COLORS[selectedColor]);
            vh.itemView.setOnClickListener(v -> listener.onClick(item));

        } else if (item.type == 1) {
            TextViewHolder vh = (TextViewHolder) holder;
            if (item.text == null) {
                vh.text.setVisibility(View.GONE);
            } else {
                vh.text.setVisibility(View.VISIBLE);
                vh.text.setText(item.text);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public static class Data {
        public int type; // 0 = image, 1 = text
        public String text = null;
        public int resId = 0;
        public int price = 500;
        public int imageCode;
        public boolean owned = false;

        public Data(int resId, int code, int price) {
            this.type = 0;
            this.resId = resId;
            this.imageCode = code;
            this.price = price;
        }

        public Data(String text) {
            this.type = 1;
            this.text = text;
        }

        public Data() {
            this.type = 1;
        }
    }

    public void setOnItemClickListener(Listener listener) {
        this.listener = listener;
    }
}
