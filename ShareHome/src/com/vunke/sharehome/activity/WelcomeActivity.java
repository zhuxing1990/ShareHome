package com.vunke.sharehome.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.updata.AppTVStoreUpdateManager;

public class WelcomeActivity extends BaseActivity {
	
	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_welcome);
		Timer timer = new Timer();
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				SharedPreferences sp = getSharedPreferences(Config.WELCOME, MODE_PRIVATE);
				boolean isFirst = sp.getBoolean(Config.IS_First, true);
				if (isFirst) {
					Intent intent = new Intent(mcontext,GuideActivity.class);
					startActivity(intent);
					finish();
					
				}else {
					Intent intent = new Intent(mcontext,LoginActivity2.class);
					startActivity(intent);
					finish();
				}
			}
		};
	
		timer.schedule(task, 2000);
	}

	@Override
	public void OnClick(View v) {
		
	}
}
