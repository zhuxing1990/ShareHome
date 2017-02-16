package com.vunke.sharehome.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.updata.AppTVStoreUpdateManager;
import com.vunke.sharehome.utils.UiUtils;

/**
 * 关于Activity
 * */
public class AboutActivity extends BaseActivity {
	/**
	 * 返回键
	 */
	private Button about_back;
	/**
	 * 反馈，更新，服务条款
	 */
	private RelativeLayout about_feedback, about_check_update, about_tos;
	/**
	 * APP版本
	 */
	private TextView app_version;
	/**
	 * 更新管理器
	 */
	private AppTVStoreUpdateManager updateManager;
	/**
	 * 点击更新次数
	 */
	private int num = 0;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_about);
		init();
		initVersion();
	}

	/**
	 *设置版本号 
	 */
	private void initVersion() {
		app_version.setText("版本号:"+UiUtils.getVersionName(mcontext));
	}

	/**
	 *初始化 
	 */
	private void init() {
		about_back = (Button) findViewById(R.id.about_back);// 返回键
		about_feedback = (RelativeLayout) findViewById(R.id.about_feedback);// 反馈，
		about_check_update = (RelativeLayout) findViewById(R.id.about_check_update);// 更新
		about_tos = (RelativeLayout) findViewById(R.id.about_tos);// 服务条款
		app_version = (TextView) findViewById(R.id.app_version);
		SetOnClickListener(about_back, about_feedback, about_check_update,
				about_tos);// 设置点击事件
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.about_back:// 返回键
			finish();
			break;
		case R.id.about_feedback:// 反馈，
			Config.intent = new Intent(mcontext, FeedbackActivity.class);
			startActivity(Config.intent);
			break;
			
		case R.id.about_check_update:// 更新
			num++;
			if (num <= 2) {
				showToast("正在后台检测更新中...");
			} else if (num == 3) {
				showToast("哎呀，别点了,在检测更新啦!");
				return;
			} else if (num >= 4) {
				showToast("求你别点了,有更新会通知你的啦！");
				return;
			}
			updateManager = new AppTVStoreUpdateManager(mcontext);
			updateManager.GetAppTVStoreUpdateInfo();
			break;
		case R.id.about_tos:// 服务条款
//			Config.intent = new Intent(mcontext, ServiceTermsActivity.class);
//			startActivity(Config.intent);
			break;
		default:
			break;
		}

	}

}
