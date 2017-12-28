package com.lm.focusviewgroup;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;

/**
 * Created by lm on 2017/12/17.
 */

public class FocusListView extends ListView {
    public FocusListView(Context context) {
        super(context);
    }

    public FocusListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction) {
        views.add(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        if (getFocusedChild() != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            result = true;
            switch (event.getKeyCode()) {
                case 300:
                    requestNextFocus();
                    break;
                case 301:
                    requestBeforeFocused();
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * @param child ListView的child
     * @param direction FOCUS_LEFT或FOCUS_RIGHT
     * @return child下所有能获取焦点空间集合
     */
    private ArrayList<View> findFocusableInChild(View child,int direction) {
        ArrayList<View> focusableList = new ArrayList<>();
        if (child instanceof ViewGroup) {
            child.addFocusables(focusableList, direction);
        }
        return focusableList;
    }

    private void requestNextFocus() {
        View next;
        View focusedChild = getFocusedChild();
        View focused = focusedChild.findFocus();
        ArrayList<View> focusableList = findFocusableInChild(focusedChild,FOCUS_RIGHT);
        //现在child下查找下一个获取焦点控件
        for (int i = 0; i < focusableList.size(); i++) {
            if (focusableList.get(i) == focused && (i + 1) < focusableList.size()) {
                next = focusableList.get(i + 1);
                next.requestFocus();
                //View不完全显示滚动知道完全显示
                scrollToNext(getPositionForView(focusedChild), next,FOCUS_DOWN);
                return;
            }
        }

        //在child中未找到下一个焦点控件，焦点移动到下一个child
        View nextFocusedChild;
        int position = getPositionForView(focusedChild);
        if (position + 1 >= getCount()) {
            return;
        }
        if ((position + 1 - getFirstVisiblePosition()) < getChildCount()) {
            nextFocusedChild = getChildAt(position + 1 - getFirstVisiblePosition());
            //要处理控件是否在屏幕上情况，如果不在，需要滚动ListView
            nextFocusedChild.requestFocus();
            scrollToNext(position + 1, nextFocusedChild.findFocus(),FOCUS_DOWN);
        } else {
            scrollToNext(position+1,null,FOCUS_DOWN);
        }
    }

    private void requestBeforeFocused() {
        View next;
        View focusedChild = getFocusedChild();
        View focused = focusedChild.findFocus();
        ArrayList<View> focusableList = findFocusableInChild(focusedChild,FOCUS_LEFT);
        //现在child下查找下一个获取焦点控件
        for (int i = 0; i < focusableList.size(); i++) {
            if (focusableList.get(i) == focused && (i - 1) >= 0) {
                next = focusableList.get(i - 1);
                next.requestFocus();

                scrollToNext(getPositionForView(focusedChild), next,FOCUS_UP);
                return;
            }
        }

        //在child中未找到下一个焦点控件，焦点移动到下一个child
        View nextFocusedChild;
        int position = getPositionForView(focusedChild);
        if (position - 1 < 0) {
            return;
        }
        if ((position - 1 - getFirstVisiblePosition()) >= 0) {
            nextFocusedChild = getChildAt(position - 1 - getFirstVisiblePosition());
            focusableList=findFocusableInChild(nextFocusedChild,FOCUS_LEFT);
            //child中最后一个可获取焦点控件获取焦点
            focusableList.get(focusableList.size()-1).requestFocus();
            //要处理控件是否在屏幕上情况，如果不在，需要滚动ListView
            scrollToNext(position-1, nextFocusedChild.findFocus(),FOCUS_UP);
        } else {
            scrollToNext(position-1,null,FOCUS_UP);
        }
    }

    /**
     *
     * @param position ListView中下一个child位置
     * @param nextFocused 下一个获取焦点View
     * @param direction 焦点移动方向，FOCUS_UP或FOCUS_DOWN
     */
    private void scrollToNext(final int position, View nextFocused,int direction) {
        //直接向上或向下滚动ListView一个child
        if(nextFocused==null){
            switch (direction){
                case FOCUS_UP:
                    if(position>=0){
                        smoothScrollToPosition(position);
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View focusedChild=getChildAt(position-getFirstVisiblePosition());
                                if(focusedChild!=null){
                                    ArrayList<View> focusableList=findFocusableInChild(focusedChild,FOCUS_LEFT);
                                    focusableList.get(focusableList.size()-1).requestFocus();
                                }
                            }
                        },200);
                    }
                    break;
                case FOCUS_DOWN:
                    if(position<getCount()){
                        smoothScrollToPosition(position);
                        /*smoothScrollToPosition在UI线程执行，需要时间SCROLL_DURATION / viewTravelCount
                          详情查看AbsListView
                         */
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View focusedChild=getChildAt(position-getFirstVisiblePosition());
                                if(focusedChild!=null){
                                    focusedChild.requestFocus();
                                }
                            }
                        },200);
                    }
                    break;
                default:
                    break;
            }
            return;
        }

        Rect focusedRect = new Rect();
        nextFocused.getFocusedRect(focusedRect);
        //focused平移到ListView坐标系下
        offsetDescendantRectToMyCoords(nextFocused, focusedRect);
        //top是否超过ListView顶部
        switch (direction){
            case FOCUS_UP:
                if(focusedRect.top < 0){
                    smoothScrollBy(focusedRect.top,0);

                }
                break;
            case FOCUS_DOWN:
                //判断focused的bottom是否超过ListView的底部
                if(focusedRect.bottom > getMeasuredHeight() - getListPaddingBottom()){
                    //不能用scrollBy,因为ListView里的View没有更新坐标(left,top,right,bottom),下一次依旧会滚动
                    smoothScrollBy(focusedRect.bottom-getMeasuredHeight()-getListPaddingBottom(),0);

                }
                break;
            default:
                break;
        }
    }

}
