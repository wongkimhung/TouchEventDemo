package com.rtfsc.toucheventdemo;

import java.util.NoSuchElementException;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * 自定义LinearLayout * @author 转自：http://blog.csdn.net/singwhatiwanna/article/details/25546871 *
 */
public class StickyLayout extends LinearLayout {
	private static final String TAG = "StickyLayout";

	public interface OnGiveUpTouchEnventListener {
		boolean giveUpTouchEvent(MotionEvent event);
	}

	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};
	private View mHeader;   //上面部分，下面成为Header
	private View mContent;  //下面部分
	private OnGiveUpTouchEnventListener mGiveUpTouchEventListener;
	private int mTouchSlop;  //移动的距离
	// header的高度   单位：px
	private int mOriginalHeaderHeight;//Header部分的原始高度
	private int mHeaderHeight;//Header部分现在的实际高度（随着手势滑动会变化）
	private int mStatus = STATUS_EXPANDED;      //当前的状态
	public static final int STATUS_EXPANDED = 1;    //展开状态
	public static final int STATUS_COLLAPSED = 2;   //闭合状态
	// 分别记录上次滑动的坐标
	private int mLastX = 0;
	private int mLastY = 0;
	//分别记录上次滑动的坐标（onInterceptTouchEvent)
	private int mLastXIntercept = 0;
	private int mLastYIntercept = 0;

	/*   * 构造函数1     */
	public StickyLayout(Context context) {
		super(context);
	}

	/*   * 构造函数2     */
	public StickyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/* 构造函数3     * TargetApi 标签的作用是使高版本的api代码在低版本sdk不报错  */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public StickyLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * onWindowFocusChanged方法用于监听一个activity是否加载完毕，Activity生命周期中，
	 * onStart, onResume, onCreate都不是真正visible的时间点，真正的visible时间点是onWindowFocusChanged()函数被执行时。
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		//如果是activity加载完毕，mHeader和mContent未被初始化，则执行初始化方法。
		if (hasWindowFocus && (mHeader == null || mContent == null)) {
			initData();
		}
	}

	private void initData() {
		// 使用getIdentifier()方法可以方便的获各应用包下的指定资源ID。
		// 详细请看：http://blog.sina.com.cn/s/blog_5da93c8f0100zlrx.html
		int headerId = getResources().getIdentifier("header", "id", getContext().getPackageName());
		int contentId = getResources().getIdentifier("content", "id", getContext().getPackageName());
		if (headerId != 0 && contentId != 0) {
			mHeader = findViewById(headerId);
			mContent = findViewById(contentId);
			mOriginalHeaderHeight = mHeader.getMeasuredHeight();
			mHeaderHeight = mOriginalHeaderHeight;          //是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。
			mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
			Log.d(TAG, "mTouchSlop = " + mTouchSlop);
		} else {
			throw new NoSuchElementException("Did your view with \"header\" or \"content\" exist?");
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int intercepted = 0;
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastXIntercept = x;
				mLastYIntercept = y;
				mLastX = x;
				mLastY = y;
				intercepted = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = x - mLastXIntercept;
				int deltaY = y - mLastYIntercept;

				if (mStatus == STATUS_EXPANDED) {
					intercepted = 1;
				} else {
					boolean directTop = deltaY <= 0;
					boolean canScroll = isContentCanBeScroll(directTop);
					System.out.println("canScroll = " + canScroll);
					System.out.println("directTop = " + directTop);
					intercepted = canScroll ? 0 : 1;
				}


				if (mGiveUpTouchEventListener != null) {
					if (mGiveUpTouchEventListener.giveUpTouchEvent(event) && deltaY >= mTouchSlop) {
						intercepted = 1;
					}
				}
				break;
			case MotionEvent.ACTION_UP: {
				intercepted = 0;
				mLastXIntercept = mLastYIntercept = 0;
				break;
			}
			default:
				break;
		}
		Log.d(TAG, "intercepted = " + intercepted);
		//如果为1则返回true,传递给当前的onTouchEvent。如果为0则返回false,传递给子控件
		return intercepted != 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = x - mLastX;
				int deltaY = y - mLastY;
				setHeaderHeight(mHeaderHeight + deltaY);
				break;
			case MotionEvent.ACTION_UP:
				int destHeight = 0;
				if (mHeaderHeight <= mOriginalHeaderHeight * 0.5) {
					destHeight = 0;
					mStatus = STATUS_COLLAPSED;
				} else {
					destHeight = mOriginalHeaderHeight;
					mStatus = STATUS_EXPANDED;
				}
				//慢慢滑向终点
				this.smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
				break;
			default:
				break;
		}
		mLastX = x;
		mLastY = y;
		return true;
	}

	public void smoothSetHeaderHeight(final int from, final int to, long duration) {
		final int frameCount = (int) (duration / 1000f * 30) + 1;
		final float partation = (to - from) / (float) frameCount;
		new Thread("Thread#smoothSetHeaderHeight") {
			public void run() {
				for (int i = 0; i < frameCount; i++) {
					final int height;
					if (i == frameCount - 1) {
						height = to;
					} else {
						height = (int) (from + partation * i);
					}
					post(new Runnable() {
						@Override
						public void run() {
							setHeaderHeight(height);
						}
					});
					try {
						sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/*   改变header的高度   */
	private void setHeaderHeight(int height) {

		if (height < 0) {
			height = 0;
		} else if (height > mOriginalHeaderHeight) {
			height = mOriginalHeaderHeight;
		}
		if (mHeaderHeight != height) {
			mHeaderHeight = height;
			mHeader.getLayoutParams().height = mHeaderHeight;
			mHeader.requestLayout();
		}
	}

	public boolean isViewInBottom(View view) {
		boolean isButtom = false;
		if (view == null) {
			return false;
		}
		if (view instanceof AbsListView) {
			AbsListView content = (AbsListView) mContent;
			View lastView = content.getChildAt(content.getChildCount() - 1);
			isButtom = lastView != null && lastView.getBottom() == content.getHeight();
		}
		return isButtom;
	}

	public boolean isViewInTop(View view) {
		boolean isTop = false;
		if (view == null) {
			return false;
		}
		if (view instanceof AbsListView) {
			AbsListView content = (AbsListView) mContent;
			isTop = content.getFirstVisiblePosition() == 0 && content.getChildAt(0).getTop() == 0;
		}
		return isTop;
	}

	public boolean isContentCanBeScroll(boolean directTop) {
		if (isViewInTop(mContent) && !directTop) {
			return false;
		}
		if (isViewInBottom(mContent) && directTop) {
			return false;
		}
		return true;
	}
}