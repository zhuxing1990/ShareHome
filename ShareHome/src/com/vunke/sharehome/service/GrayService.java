package com.vunke.sharehome.service;

import com.vunke.sharehome.receiver.WakeReceiver;
import com.vunke.sharehome.utils.WorkLog;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * 灰色保活手法创建的Service进程
 * 
 * @author Clock
 * @since 2016-04-12
 */
public class GrayService extends Service {

	private final static String TAG = GrayService.class.getSimpleName();
	/**
	 * 定时唤醒的时间间隔，5分钟
	 */
	private final static int ALARM_INTERVAL = 5 * 60 * 1000;
	private final static int WAKE_REQUEST_CODE = 6666;

	private final static int GRAY_SERVICE_ID = -1001;
	@Override
	public void onCreate() {
//		WorkLog.e(TAG, "GrayService->onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT < 18) {
			startForeground(GRAY_SERVICE_ID, new Notification());// API < 18
																	// ，此方法能有效隐藏Notification上的图标
		} else {
			Intent innerIntent = new Intent(this, GrayInnerService.class);
			startService(innerIntent);
			startForeground(GRAY_SERVICE_ID, new Notification());
		}

		// 发送唤醒广播来促使挂掉的UI进程重新启动起来
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent();
		alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
		PendingIntent operation = PendingIntent.getBroadcast(this,
				WAKE_REQUEST_CODE, alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), ALARM_INTERVAL, operation);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onDestroy() {
//		WorkLog.e(TAG, "GrayService->onDestroy");
		super.onDestroy();
	}

	/**
	 * 给 API >= 18 的平台上用的灰色保活手段
	 */
	public static class GrayInnerService extends Service {

		@Override
		public void onCreate() {
//			WorkLog.e(TAG, "InnerService -> onCreate");
			super.onCreate();
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
//			WorkLog.e(TAG, "InnerService -> onStartCommand");
			startForeground(GRAY_SERVICE_ID, new Notification());
			// stopForeground(true);
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}

		@Override
		public IBinder onBind(Intent intent) {
			// TODO: Return the communication channel to the service.
			throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override
		public void onDestroy() {
//			WorkLog.e(TAG, "InnerService -> onDestroy");
			super.onDestroy();
		}
	}
}