package com.kxy.notify;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.kxy.auth.Config;
import com.kxy.base.RxBus;
import com.kxy.notify.NotifyBean2.DataBean;
import com.kxy.notify.NotifyBean2.DataBean.ProductAttr;
import com.kxy.ti.util.Constants;
import com.kxy.ti.util.LogUtil;
import com.kxy.ti.util.UIUtil;
import com.kxy.tl.download.Util;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class NotifyService extends Service {
	private String name;
	private static final String actionName = "com.kxy.tl.notify";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerBoradcastReceiver();
		checkLogin();
		return super.onStartCommand(intent, flags, startId);
	}

	private void checkLogin() {
		LogUtil.i("tv_launcher",
				"send BroadCast to request user info,start time:" + new Date());
		UIUtil.sendBroadCast(this, Constants.REQUEST_USER_INFO_ACTION,
				new Intent());
	}

	public void registerBoradcastReceiver() {
		LogUtil.e("tv_launcher", "registerBoradcast:request user info");
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(Constants.LOAD_USER_INFO_ACTION);
		myIntentFilter.addAction(Constants.REGISTER_STATUS_ACTION);
		myIntentFilter.addAction(Constants.REGISTER_RESULT_ACTION);
		myIntentFilter.addAction(Constants.REGISTER_REBOOT_ACTION);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constants.LOAD_USER_INFO_ACTION)) {
				String userName = intent.getStringExtra("userName");
				String userID = intent.getStringExtra("userID");
				// String password = intent.getStringExtra("password");
				LogUtil.e("tv_launcher", "userName:" + userName);
				LogUtil.e("tv_launcher", "userID:" + userID);
				// LogUtil.e("tv_launcher", "password:" + password);
				boolean initNotfy = TextUtils.isEmpty(userID);
				if (initNotfy) {
					Log.e("tv_launcher", "get UserId is null");
				} else {
					name = userID;
					Log.e("tv_launcher", "get UserId:" + name);
					handler.sendEmptyMessageDelayed(0x1211, 2000);
				}
			}
		}
	};
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x1211:
				if (Util.isNetConnected(getApplicationContext()) == true) {
					initData();
				} else {
					Log.e("tv_launcher", "network not connect");
					handler.sendEmptyMessageDelayed(0x1211, 2000);
				}
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 请求获取推送消息
	 */
	private void initData() {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date(System.currentTimeMillis());
			String format = dateFormat.format(date);
			// String version_code = UiUtils
			// .getVersionCode(getApplicationContext())
			// + Build.MODEL
			// + Build.VERSION.RELEASE;

			LogUtil.e("tv_launcher", "get notification ");
			String getVersion_vode = "420161208";
			OkHttpUtils.post(Config.Notify_Url + Config.Push).tag(this)
					.params("name", name)
					.params("version_code", getVersion_vode)
					.params("business_type", "2").execute(new StringCallback() {

						@Override
						public void onResponse(boolean isFromCache, String t,
								Request request, @Nullable Response response) {
							// LogUtil.e(TAG, "Data:" + t);
							try {
								String data = "{\"data\":";
								String data2 = "}";
								StringBuffer buffer = new StringBuffer();
								buffer.append(data);
								buffer.append(t);
								buffer.append(data2);
								Gson gson = new Gson();
								LogUtil.e("tv_launcher", buffer.toString());
								NotifyBean2 bean = gson.fromJson(
										buffer.toString(), NotifyBean2.class);
								bean.setUserId(name);
								bean.setRxBuscode(20161207);
								RxBus.getInstance().post(bean);

								// initUrl(bean);
							} catch (Exception e) {
								LogUtil.e("tv_launcher", "解析推送数据失败");
								e.printStackTrace();
							}
						}

						@Override
						public void onError(boolean isFromCache, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onError(isFromCache, call, response, e);
							LogUtil.e("tv_launcher", "onError");
						}
					});
		} catch (Exception e) {
			LogUtil.e("tv_launcher", "initData():get path onError");
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtil.e("tv_launcher", "注销广播:request user info");
		try {
			if (mBroadcastReceiver != null) {
				unregisterReceiver(mBroadcastReceiver);
			}
		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
			LogUtil.e("tv_launcher", "不需要注销");
		}
	}
}
