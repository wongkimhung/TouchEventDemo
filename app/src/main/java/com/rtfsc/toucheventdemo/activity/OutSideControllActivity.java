package com.rtfsc.toucheventdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ListView;

import com.rtfsc.toucheventdemo.HorizontalViewPagerEx;
import com.rtfsc.toucheventdemo.BaseAdatperEx;
import com.rtfsc.toucheventdemo.R;

public class OutSideControllActivity extends AppCompatActivity {

	HorizontalViewPagerEx mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		initView();
	}

	private void initView() {
		mViewPager = (HorizontalViewPagerEx) findViewById(R.id.viewPager);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int screenHeight = getResources().getDisplayMetrics().heightPixels;

		for (int i = 0; i < 3; i++) {
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenWidth, screenHeight);
			ListView listView = new ListView(OutSideControllActivity.this);
			listView.setAdapter(new BaseAdatperEx());
			mViewPager.addView(listView, layoutParams);
		}
	}

}
