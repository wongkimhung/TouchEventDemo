package com.rtfsc.toucheventdemo;

import android.content.Context;
import android.support.v7.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by wongkimhung on 2016/7/30.
 */
public class ListViewEx extends ListView {
	//  上次滑动的坐标
	private int mLastX = 0;
	private int mLastY = 0;

	private VelocityTracker mTracker;
	private int mTouchSlop = 8;
	private Context mContext;

	public ListViewEx(Context context) {
		super(context);
		initData(context);
	}

	public ListViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	public ListViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initData(context);
	}

	private void initData(Context context) {
		mContext = context.getApplicationContext();
		mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				((ViewGroup) getParent()).requestDisallowInterceptTouchEvent(true); //  通知父容器，接下来的事件不要拦截
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = x - mLastX;
				int deltaY = y - mLastY;
				if (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop) {
					if (Math.abs(deltaX) > Math.abs(deltaY)) {
						//  当横向滑动时，通知父容器，接下来的事件进行拦截
						((ViewGroup) getParent()).requestDisallowInterceptTouchEvent(false);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				break;
			default:
				break;
		}
		mLastX = x;
		mLastY = y;
		return super.dispatchTouchEvent(ev);
	}
}
