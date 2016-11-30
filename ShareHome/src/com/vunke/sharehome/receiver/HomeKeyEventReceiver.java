package com.vunke.sharehome.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.HomeActivity;
import com.vunke.sharehome.service.GrayService;

/**
 * 监听是否点击了home键将客户端推到后台
 */
public class HomeKeyEventReceiver extends BroadcastReceiver {
	String SYSTEM_REASON = "reason";
	String SYSTEM_HOME_KEY = "homekey";
	String SYSTEM_HOME_KEY_LONG = "recentapps";
	private final int Receiver_FLAG = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
			String reason = intent.getStringExtra(SYSTEM_REASON);
			if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
				// 表示按了home键,程序到了后台
				AddNotification(context);
			} else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {
				// 表示长按home键,显示最近使用的程序列表
			}
		}
	}

	public void AddNotification(Context context) {
		NotificationManager manager = (NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.home_icon)
				.setContentTitle("想家视频电话").setContentText("后台运行中")/* 设置详细文本 */
				.setAutoCancel(true);/* 设置点击后通知消失 */
		Intent intentActivity = new Intent();
		ComponentName name = new ComponentName(context,
				HomeActivity.class);
		intentActivity.setComponent(name);
		intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intentActivity, 0);
		builder.setContentIntent(pendingIntent);
		manager.notify(Receiver_FLAG, builder.build());
	}
}
