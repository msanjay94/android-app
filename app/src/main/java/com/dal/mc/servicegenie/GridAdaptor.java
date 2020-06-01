package com.dal.mc.servicegenie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdaptor extends BaseAdapter {

    Context context;
    private final int [] images;
    private  final String [] values;
    View view;
    LayoutInflater layoutInflater;

    public GridAdaptor(Context context, int[] images, String[] values) {
        this.context = context;
        this.images = images;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            view = new View(context);
            view = layoutInflater.inflate(R.layout.service_list, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            imageView.setImageResource(images[position]);
            textView.setText(values[position]);
        }
        return view;
    }


}
