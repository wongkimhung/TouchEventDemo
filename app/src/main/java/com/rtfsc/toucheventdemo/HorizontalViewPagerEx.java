package com.rtfsc.toucheventdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by wongkimhung on 2016/7/30.
 */
public class HorizontalViewPagerEx extends ViewGroup {
	private Context mContext;
	private boolean mNeedIntercept = true;
	public static final String TAG = "HorizontalViewPager";
	private int mChildrenSize;
	private int mChildWidth;
	private int mChildIndex;
	//  上次滑动的坐标
	private int mLastX = 0;
	private int mLastY = 0;
	//  上次滑动的坐标(onInterceptTouchEvent)
	private int mLastXIntercept = 0;
	private int mLastYIntercept = 0;

	private Scroller mScroller;
	private VelocityTracker mTracker;
	private int mTouchSlop = 8;

	public HorizontalViewPagerEx(Context context) {
		super(context);
		initData(context);
	}

	public HorizontalViewPagerEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	public HorizontalViewPagerEx(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initData(context);
	}

	private void initData(Context context) {
		mContext = context;
		mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
		mScroller = new Scroller(mContext);
		mTracker = VelocityTracker.obtain();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = 0;
		int measuredHeight = 0;
		final int childCount = getChildCount();
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		if (childCount == 0) {
			setMeasuredDimension(0, 0);
		} else if (heightSpecMode == MeasureSpec.AT_MOST) {
			final View childView = getChildAt(0);
			measuredHeight = childView.getMeasuredHeight();
			setMeasuredDimension(widthSpaceSize, childView.getMeasuredHeight());
		} else if (widthSpecMode == MeasureSpec.AT_MOST) {
			final View childView = getChildAt(0);
			measuredWidth = childView.getMeasuredWidth() * childCount;
			setMeasuredDimension(measuredWidth, heightSpaceSize);
		} else {
			final View childView = getChildAt(0);
			measuredWidth = childView.getMeasuredWidth() * childCount;
			measuredHeight = childView.getMeasuredHeight();
			setMeasuredDimension(measuredWidth, measuredHeight);
		}
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();
		mChildrenSize = childCount;

		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				mChildWidth = childWidth;
				childView.layout(childLeft, 0, childLeft + childWidth,
						childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		if (!mNeedIntercept) {
			//  不需要主动进行拦截
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				boolean a = mScroller.isFinished();
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
					return true;
				}
				return false;
			} else {
				return true;
			}
		} else {
			//  需要主动进行外部拦截
			boolean intercept = false;
			switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN:
					intercept = false;
					if (!mScroller.isFinished()) {
						mScroller.abortAnimation();
						intercept = true;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					int deltaX = x - mLastXIntercept;
					int deltaY = y - mLastYIntercept;
					if (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop) {
						if (Math.abs(deltaX) > Math.abs(deltaY)) {
							//  当横向滑动时，拦截事件，不对内部子控件下发移动事件
							intercept = true;
						} else {
							intercept = false;
						}
					} else {
						intercept = false;
					}
					break;
				case MotionEvent.ACTION_UP:
					intercept = false;
					break;
				default:
					break;
			}
			mLastX = x;
			mLastY = y;
			mLastXIntercept = x;
			mLastYIntercept = y;
			return intercept;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mTracker.addMovement(event);
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = x - mLastX;
				int deltaY = y - mLastY;
				scrollBy(-deltaX, 0);
				break;
			case MotionEvent.ACTION_UP:
				int scrollX = getScrollX();
				int scrollToChildIndex = scrollX / mChildWidth;
				mTracker.computeCurrentVelocity(500);
				float xVelocity = mTracker.getXVelocity();
				if (Math.abs(xVelocity) > 100) {
					mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
				} else {
					mChildIndex = (scrollX + mChildWidth / 2) / mChildWidth;
				}
				mChildIndex = Math.max(0, Math.min(mChildIndex, mChildrenSize - 1));
				int dx = mChildIndex * mChildWidth - scrollX;
				smoonthScrollBy(dx, 0);
				mTracker.clear();
				break;
			default:
				break;
		}
		mLastX = x;
		mLastY = y;
		return true;
	}

	private void smoonthScrollBy(int dx, int dy) {
		mScroller.startScroll(getScrollX(), 0, dx, 0, 500);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		mTracker.recycle();
		super.onDetachedFromWindow();
	}

	public void setNeedIntercept(boolean needIntercept) {
		this.mNeedIntercept = needIntercept;
	}
}
