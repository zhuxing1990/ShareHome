package com.vunke.sharehome.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.vunke.sharehome.R;

public class HomeService extends Service {
	private HomeReceiver homeReceiver;
	private NotificationManager manager;
	private final int Receiver_FLAG = 1;
	private String className = "";
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// homeReceiver = new HomeReceiver();
		// IntentFilter homeFilter = new
		// IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		// registerReceiver(homeReceiver, homeFilter);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(homeReceiver);
		if (manager!=null ) {
			manager.cancel(Receiver_FLAG);
		}
		homeReceiver = null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.hasExtra("className")&& intent!=null) {
			className = intent.getStringExtra("className");
		}
		homeReceiver = new HomeReceiver();
		IntentFilter homeFilter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(homeReceiver, homeFilter);
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 捕获home键
	 * 
	 * @author Administrator
	 * 
	 */
	public class HomeReceiver extends BroadcastReceiver {
		final String SYSTEM_DIALOG_REASON_KEY = "reason";
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reason != null
						&& reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
					manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					NotificationCompat.Builder builder = new NotificationCompat.Builder(
							getApplication())
							.setSmallIcon(R.drawable.home_icon)
							.setContentTitle("想家").setContentText("正在通话中")/* 设置详细文本 */
							.setAutoCancel(true);/* 设置点击后通知消失 */

					if (!TextUtils.isEmpty(className)) {
					Intent intentActivity = new Intent();
					ComponentName name = new ComponentName(getApplicationContext(), className);
					intentActivity.setComponent(name);
					intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					PendingIntent pendingIntent = PendingIntent.getActivity(
							getApplicationContext(), 0, intentActivity, 0);
					builder.setContentIntent(pendingIntent);
					manager.notify(Receiver_FLAG, builder.build());
					}
				}
			}
		}

	}

}
