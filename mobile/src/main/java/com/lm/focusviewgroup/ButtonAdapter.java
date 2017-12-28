package com.lm.focusviewgroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by lm on 2017/12/17.
 */

public class ButtonAdapter extends BaseAdapter {
    private String[] mData;
    private LayoutInflater mInflater;

    public ButtonAdapter(String[] data, Context context) {
        mData=data;
        mInflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public Object getItem(int position) {
        return mData[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView=mInflater.inflate(R.layout.item,null);
        }
        TextView textView=(TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(mData[position]);
        return convertView;
    }
}
