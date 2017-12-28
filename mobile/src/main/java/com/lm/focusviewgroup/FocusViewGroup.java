package com.lm.focusviewgroup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by lm on 2017/12/10.
 */

public class FocusViewGroup extends FrameLayout {
    private final static String TAG = "FocusViewGroup";

    public FocusViewGroup(@NonNull Context context) {
        super(context);
    }

    public FocusViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled=super.dispatchKeyEvent(event);
        if(!handled){
            View next;
            View directFocused=findFocus();
            if(directFocused !=null&&event.getAction()==KeyEvent.ACTION_DOWN){
                switch (event.getKeyCode()) {
                    case 300:
                        next=findNextFocused(directFocused);
                        if(next!=null){
                            handled=next.requestFocus();
                        }
                        break;
                    case 301:
                        next=findBeforeFocused(directFocused);
                        if(next!=null){
                            handled=next.requestFocus();
                        }
                        break;
                    default:
                        handled=super.dispatchKeyEvent(event);
                        break;
                }
            }else {
                handled=super.dispatchKeyEvent(event);
            }
        }

        return handled;
    }

    /**
     *
     * @param focused 当前焦点View
     * @return 下一个获取焦点View
     */
    private View findNextFocused(View focused) {
        View next=null;
        ArrayList<View> views=new ArrayList<>();
        addFocusables(views,FOCUS_RIGHT);
        for(int i=0;i<views.size();i++){
            if(views.get(i)==focused){
                next=views.get((i+1)%views.size());
                break;
            }
        }
        return next;
    }

    /**
     ** @param focused 当前焦点View
     * @return 前一个获取焦点View
     */
    private View findBeforeFocused(View focused){
        View next=null;
        ArrayList<View> views=new ArrayList<>();
        addFocusables(views,FOCUS_LEFT);
        for(int i=0;i<views.size();i++){
            if(views.get(i)==focused){
                next=views.get((i-1+views.size())%views.size());
                break;
            }
        }
        return next;
    }
}
