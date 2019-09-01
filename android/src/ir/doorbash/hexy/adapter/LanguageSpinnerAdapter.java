package ir.doorbash.hexy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ir.doorbash.hexy.FontManager;
import ir.doorbash.hexy.R;

/**
 * Created by Milad Doorbash on 8/29/2019.
 */
public class LanguageSpinnerAdapter extends BaseAdapter {

    private Context context;
    public String[] array;

    public LanguageSpinnerAdapter(Context context) {
        this.context = context;
        array = context.getResources().getStringArray(R.array.languages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.row_dialog_settings, null);
            ViewHolder vh = new ViewHolder();
            vh.text = v.findViewById(R.id.text);
            v.setTag(vh);
        }
        ViewHolder vh = (ViewHolder) v.getTag();

//        String text = "Language";
        String text = array[position];

        vh.text.setText(text);
//        vh.text.setTypeface(FontManager.getInstance(context).noto);
//        vh.text.setTextColor(Color.BLACK);

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.row_spinner_language, null);
            ViewHolder vh = new ViewHolder();
            vh.text = v.findViewById(R.id.text);
            v.setTag(vh);
        }
        ViewHolder vh = (ViewHolder) v.getTag();

        String text = getItem(position).toString();

        vh.text.setText(text);
        vh.text.setTypeface(FontManager.getInstance(context).noto);
        vh.text.setTextColor(Color.BLACK);

        return v;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return array[position];
    }

    @Override
    public int getCount() {
        return array.length;
    }

    class ViewHolder {
        TextView text;
    }
}
