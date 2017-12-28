package com.lm.focusviewgroup;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView=(ListView) findViewById(R.id.list);
        listView.setItemsCanFocus(true);
        String[] data=new String[15];
        for(int i=0;i<data.length;i++){
            data[i]="test"+i;
        }
        listView.setAdapter(new ButtonAdapter(data,this));
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn0:
                Intent intent=new Intent(this,ListActivity.class);
                startActivity(intent);
                break;
        }
    }
}
