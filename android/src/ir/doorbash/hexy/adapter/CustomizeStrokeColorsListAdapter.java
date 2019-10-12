package ir.doorbash.hexy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import ir.doorbash.hexy.R;

public class CustomizeStrokeColorsListAdapter extends BaseAdapter {

    public Context mContext;
    int[] data;

    public CustomizeStrokeColorsListAdapter(Context mContext, int[] data) {
        try {
            this.mContext = mContext;
            this.data = data;
        } catch (Exception e) {
            e.printStackTrace();
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
            v = inflater.inflate(R.layout.item_customize_stroke_colors, parent, false);
        }
        final int color = data[position];
        v.setBackgroundColor(color);
        return v;
    }

}
