package com.lm.focusviewgroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

/**
 * Created by lm on 2017/12/17.
 */

public class ListActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ListView listView=(ListView) findViewById(R.id.list);
        listView.setItemsCanFocus(true);
        String[] data=new String[15];
        for(int i=0;i<data.length;i++){
            data[i]="test"+i;
        }
        listView.setAdapter(new ButtonAdapter(data,this));

    }
}
