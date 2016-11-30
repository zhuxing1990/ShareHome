package com.vunke.sharehome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vunke.sharehome.activity.LoginActivity;
import com.vunke.sharehome.service.NetConnectService;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 开机广播
 *
 */
public class AppBroadcastReceiver extends BroadcastReceiver {
	 static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent arg1) {
		WorkLog.e("AppBroadcastReceiver", "开机启动");
		Intent intent = new Intent(context, NetConnectService.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}