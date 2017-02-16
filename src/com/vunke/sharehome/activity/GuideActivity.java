package com.vunke.sharehome.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.adapter.MypagerAdapter;
import com.vunke.sharehome.base.BaseActivity;

/**
 * 引导页
 * */
public class GuideActivity extends BaseActivity implements OnPageChangeListener {
	private ViewPager pager;
	private List<View> list;
	private MypagerAdapter ar;
	private ImageView[] points;
	private static final int[] pics = { R.drawable.guide1, R.drawable.guide2,
			R.drawable.guide3 };
	private int currentIndex;// 记录当前位置
	protected Intent intentActivity;
	private Button finish_activity;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_guide);
		SharedPreferences sp = getSharedPreferences(Config.WELCOME,
				MODE_PRIVATE);
		boolean isFirst = sp.getBoolean(Config.IS_First, true);
		if (isFirst) {
			Editor editor = sp.edit();
			editor.putBoolean(Config.IS_First, false);
			editor.commit();
		}
		init();
		initDate();
	}

	public void init() {
		pager = (ViewPager) findViewById(R.id.guide_viewpager);
		list = new ArrayList<View>();
		ar = new MypagerAdapter(list);
		finish_activity = (Button) findViewById(R.id.finish_activity);
	}

	public void initDate() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		for (int i = 0; i < pics.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setLayoutParams(params);
			imageView.setBackgroundResource(pics[i]);
			list.add(imageView);
		}
		pager.setAdapter(ar);
		pager.setOnPageChangeListener(this);
		initPoint();
	}

	public void initPoint() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.linear1);
		points = new ImageView[pics.length];
		for (int i = 0; i < points.length; i++) {
			// 得到一个LinearLayout 下面的每一个子元素
			points[i] = (ImageView) layout.getChildAt(i);
			// 默认的图片为
			points[i].setEnabled(true);
			// 没一个小点设置监听事件
			points[i].setOnClickListener(this);
			// 设置位置tag，方便取出与当前位置对应
			points[i].setTag(i);
		}
		currentIndex = 0;
		points[currentIndex].setEnabled(false);
	}

	// 在滑动状态时调用
	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	// 在当前页面被滑动时被调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	// 在新的页面被选中时调用
	@Override
	public void onPageSelected(int position) {
		setCurDot(position);
	}

	public void setCurView(int position) {
		if (position < 0 || position > pics.length - 1
				|| currentIndex == position) {
			return;
		}
		pager.setCurrentItem(position);
	}

	// 设置小点位置
	public void setCurDot(int position) {
		if (position < 0 || position > pics.length - 1
				|| currentIndex == position) {
			return;
		}
		points[position].setEnabled(false);
		points[currentIndex].setEnabled(true);
		currentIndex = position;
		if (position == 2 && currentIndex == 2) {
			finish_activity.setVisibility(View.VISIBLE);
		} else {
			finish_activity.setVisibility(View.GONE);
		}
	}

	// 当点击是调用
	@Override
	public void OnClick(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
		setCurDot(position);
	}

	public void finishActivity(View v) {
		Finish();
	}

	public void Finish() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				intentActivity = new Intent(mcontext, LoginActivity2.class);
				startActivity(intentActivity);
				finish();
			}
		};
		timer.schedule(task, 100);
	}

}
