package com.kxy.base;

import com.lzy.okhttputils.OkHttpUtils;

import android.app.Application;

public class BaseApplication2 extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		OkHttpUtils.init(this);
	}
}
