package com.vunke.sharehome.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class MyListView extends ListView {

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setParentScrollAble(false);//当手指触到listview的时候，让父ScrollView交出ontouch权限，也就是让父scrollview 停住不能滚动
//                LogManager.d("onInterceptTouchEvent down");
            case MotionEvent.ACTION_MOVE:
//                LogManager.d("onInterceptTouchEvent move");
                break;
            case MotionEvent.ACTION_UP:
//                LogManager.d("onInterceptTouchEvent up");
            case MotionEvent.ACTION_CANCEL:
//                LogManager.d("onInterceptTouchEvent cancel");
                setParentScrollAble(true);//当手指松开时，让父ScrollView重新拿到onTouch权限
                break;
            default:
                break;
        }
//        return super.onInterceptTouchEvent(ev);
          return false;        // return true;
    }


 /**
     * 是否把滚动事件交给父scrollview
     * 
     * @param flag
     */
    private void setParentScrollAble(boolean flag) {
    	getParent().requestDisallowInterceptTouchEvent(!flag);
       //这里的parentScrollView就是listview外面的那个scrollview
    }

}
