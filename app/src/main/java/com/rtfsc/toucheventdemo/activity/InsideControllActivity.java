package com.rtfsc.toucheventdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.rtfsc.toucheventdemo.BaseAdatperEx;
import com.rtfsc.toucheventdemo.HorizontalViewPagerEx;
import com.rtfsc.toucheventdemo.ListViewEx;
import com.rtfsc.toucheventdemo.R;

public class InsideControllActivity extends AppCompatActivity {

	HorizontalViewPagerEx mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		initView();
//		int screenWidth = getResources().getDisplayMetrics().widthPixels;
//		int screenHeight = getResources().getDisplayMetrics().heightPixels;
//		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenWidth, screenHeight);
//		ListViewEx listView = new ListViewEx(InsideControllActivity.this);
//		listView.setAdapter(new BaseAdatperEx());
//		addContentView(listView, layoutParams);
	}

	private void initView() {
		mViewPager = (HorizontalViewPagerEx) findViewById(R.id.viewPager);
		assert mViewPager != null;
		mViewPager.setNeedIntercept(false);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int screenHeight = getResources().getDisplayMetrics().heightPixels;

		for (int i = 0; i < 3; i++) {
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenWidth, screenHeight);
			ListViewEx listView = new ListViewEx(InsideControllActivity.this);
			listView.setAdapter(new BaseAdatperEx());
			mViewPager.addView(listView, layoutParams);
		}
	}


}
