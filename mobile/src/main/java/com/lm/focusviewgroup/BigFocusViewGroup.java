package com.lm.focusviewgroup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by lm on 2017/12/13.
 */

public class BigFocusViewGroup extends FrameLayout {
    private ViewGroup mFocused;

    public BigFocusViewGroup(@NonNull Context context) {
        super(context);
    }

    public BigFocusViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;
        View next;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    next = FocusViewGroupFinder.getInstance().findNextFocus(this, mFocused, FOCUS_UP);
                    if (next != null) {
                        next.requestFocus();
                    }
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    next = FocusViewGroupFinder.getInstance().findNextFocus(this, mFocused, FOCUS_DOWN);
                    if (next != null) {
                        next.requestFocus();
                    }
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    next = FocusViewGroupFinder.getInstance().findNextFocus(this, mFocused, FOCUS_LEFT);
                    if (next != null) {
                        next.requestFocus();
                    }
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    next = FocusViewGroupFinder.getInstance().findNextFocus(this, mFocused, FOCUS_RIGHT);
                    if(next!=null) {
                        next.requestFocus();
                    }
                    handled = true;
                    break;
                default:
                    handled = super.dispatchKeyEvent(event);
                    break;
            }
        } else {
            handled = super.dispatchKeyEvent(event);
        }
        System.out.println("handle:" + handled);
        return handled;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);

        ViewParent parent = focused.getParent();
        while (parent != null) {
            if (parent instanceof FocusViewGroup) {
                mFocused = (ViewGroup) parent;
                break;
            }
            parent = parent.getParent();
        }
    }
}
