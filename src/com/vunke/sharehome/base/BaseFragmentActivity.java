package com.vunke.sharehome.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.vunke.sharehome.utils.AppManager;
import com.vunke.sharehome.utils.UiUtils;

/**
 * Created by Administrator on 2015/12/26.
 */
public abstract class BaseFragmentActivity extends FragmentActivity implements
		OnPageChangeListener, OnClickListener {
	protected BaseFragmentActivity mcontext;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		BaseApplication.getActivities().add(this);
		// 禁止横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mcontext = this;
		AppManager.getAppManager().addActivity(this);
		Activity aty = AppManager.getActivity(BaseFragmentActivity.class);
		if (aty!=null&& !aty.isFinishing()) {
			finish();
		}
//		Crash();
	}

	private void Crash() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				StringBuilder sb = new StringBuilder();
				sb.append("异常出现时间为:");
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy年MM月dd日 HH:mm:ss ");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				sb.append(formatter.format(curDate) + "\n,");
				sb.append("Version Name is ");
				sb.append(UiUtils.getVersionName(getApplicationContext())
						+ "\n,");
				sb.append("Version Code is ");
				sb.append(UiUtils.getVersionCode(getApplicationContext())
						+ "\n,");
				sb.append("Android SDK Version code is ");
				sb.append(Build.VERSION.SDK_INT + "\n");// 设备的Android版本号
				sb.append("Model is ");
				sb.append(Build.MODEL + "\n");// 设备型号
				sb.append(sw.toString());
				Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
				sendIntent.setData(Uri.parse("mailto:897949440@qq.com"));// 发送邮件异常到csdn@csdn.com邮箱
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "崩溃异常");// 邮件主题
				sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());// 堆栈信息
				startActivity(sendIntent);
				finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// BaseApplication.getActivities().remove(this);
		// AppManager.getAppManager().finishActivity(this);
	}

	public void showToast(String string) {
		Toast.makeText(mcontext, string, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View view) {
		view.setOnClickListener(this);
	}

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View... v) {
		for (int i = 0; i < v.length; i++) {
			View view = v[i];
			view.setOnClickListener(this);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		OnPagerSelected(arg0);
	}

	public abstract void OnPagerSelected(int arg0);
}