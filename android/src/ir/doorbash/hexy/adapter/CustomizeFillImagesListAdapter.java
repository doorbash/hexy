package ir.doorbash.hexy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ir.doorbash.hexy.R;
import ir.doorbash.hexy.util.ColorUtil;

public class CustomizeFillImagesListAdapter extends BaseAdapter {

    public Context mContext;
    public int[] data;
    public int selectedColor;

    public CustomizeFillImagesListAdapter(Context mContext, int[] data) {
        try {
            this.mContext = mContext;
            this.data = data;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ImageHolder {
        public ImageView image = null;
        public ImageView stroke_img = null;

        public ImageHolder(View itemView) {
            image = itemView.findViewById(R.id.img);
            stroke_img = itemView.findViewById(R.id.stroke_img);
        }
    }

    @Override
    public int getCount() {
        if (data == null) return 0;
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_customize_fill_images, parent, false);
            ImageHolder holder = new ImageHolder(v);
            v.setTag(holder);
        }
        final ImageHolder vh = (ImageHolder) v.getTag();

        final int imageResource = data[position];
        if(imageResource == 0) {
            vh.image.setImageResource(R.drawable.circle);
            vh.image.setColorFilter(ColorUtil.FILL_COLORS[selectedColor]);
        } else {
            Glide.with(mContext).load(imageResource).into(vh.image);
            vh.image.setColorFilter(0);
        }

        vh.stroke_img.setColorFilter(ColorUtil.STROKE_COLORS[selectedColor]);

        return v;
    }

}
