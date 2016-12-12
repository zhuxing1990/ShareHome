package com.kxy.auth;

import com.kxy.ti.util.LogUtil;
import com.kxy.tl.activity.TupianActivity;
import com.kxy.tl.download.DownLoadService;
import com.kxy.tl.download.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectionChangeReceiver extends BroadcastReceiver {
	private static final String TAG = ConnectionChangeReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Util.isNetworkAvailable(context)) {
			LogUtil.e("tv_launcher", "网络未连接 ----------");
		}

		if (!Util.isNetworkConnected(context)) {
			LogUtil.e("tv_launcher", "网络还未连接----------");
		}
		if (Util.isNetConnected(context)) {
			LogUtil.e("tv_launcher", "网络已连接----------");
			Intent it = new Intent(context,DownLoadService.class);
			context.startService(it);
		}
	}

}










