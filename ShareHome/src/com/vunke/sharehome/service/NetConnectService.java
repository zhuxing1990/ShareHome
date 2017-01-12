package com.vunke.sharehome.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;

import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.LoginCfg;
import com.huawei.rcs.login.UserInfo;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.utils.Encrypt3DES;
import com.vunke.sharehome.utils.WorkLog;

public class NetConnectService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
	private long startTime = 0;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// WorkLog.i("NetConnectService",
		// "---------------onStartCommand-------------");
		if (System.currentTimeMillis() - startTime >2000) {
			initSDK();
			startTime = System.currentTimeMillis();
		}else {
			WorkLog.e("NetConnectService", "Cannot repeat login ");
			
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initSDK() {

		// 来电广播
		// LocalBroadcastManager.getInstance(getApplicationContext())
		// .registerReceiver(callInvitationReceiver,
		// new IntentFilter(CallApi.EVENT_CALL_INVITATION));
		getData();
		Login();

	}

	private void initLoginInfo(String countryCode, String userName) {
		// WorkLog.i("NetConnectService", countryCode+";userName:"+userName);
		SharedPreferences sp = getSharedPreferences(Config.SP_NAME,
				MODE_PRIVATE);
		String login_password = sp.getString(Config.LOGIN_PASSWORD, "");
		if (TextUtils.isEmpty(userName)) {
			userName = sp.getString(Config.LOGIN_USER_NAME, "");
			if (TextUtils.isEmpty(userName)) {
				WorkLog.e("NetConnectService", "username is null");
				return;
			}
		}
		if (!TextUtils.isEmpty(login_password)) {
			UserInfo userInfo = new UserInfo();
			if (countryCode.matches("([+]|[0-9])\\d{0,4}")) {// 保存国家区号
				userInfo.countryCode = countryCode;
			}

			userInfo.username = countryCode + Config.CALL_BEFORE + userName;
			try {
				userInfo.password = Encrypt3DES.getInstance().decrypt(
						login_password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			LoginCfg loginCfg = new LoginCfg();
			loginCfg.isAutoLogin = true;
			loginCfg.isVerified = false;

			LoginApi.login(userInfo, loginCfg);
		}

	}

	private UserInfo mLastUserInfo;

	private void getData() {
		mLastUserInfo = LoginApi.getUserInfo(LoginApi.getLastUserName());
	}

	/*
	 * // 查询用户的配置数据 mLoginCfg = LoginApi.getLoginCfg(mLastUserInfo.username);
	 */
	private void Login() {
		if (mLastUserInfo != null) {
			if (!TextUtils.isEmpty(mLastUserInfo.countryCode)) {// 自动写入帐号
				String countryCode = mLastUserInfo.countryCode;

				if ((null != mLastUserInfo)
						&& (null != mLastUserInfo.countryCode)
						&& (null != mLastUserInfo.username)) {

					if (mLastUserInfo.username.startsWith("+")) {
						int length = mLastUserInfo.countryCode.length();
						if (!mLastUserInfo.countryCode.startsWith("+")) {
							length++;
						}
						String userName = mLastUserInfo.username
								.substring(length);
						if (userName.contains(Config.CALL_BEFORE)) {
							userName = userName.substring(8, userName.length());
						}
						initLoginInfo(countryCode, userName);
					} else {
						if (mLastUserInfo.username.contains(Config.CALL_BEFORE)) {
							mLastUserInfo.username = mLastUserInfo.username
									.substring(8,
											mLastUserInfo.username.length());
						}
						initLoginInfo(countryCode, mLastUserInfo.username);

					}
				}

			}
		} else {
			WorkLog.e("NetConnectService", "get SDK userinfo is null");
			initLoginInfo("", "");
		}

	}

	// BroadcastReceiver callInvitationReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// CallSession session = (CallSession) intent
	// .getSerializableExtra(CallApi.PARAM_CALL_SESSION);
	//
	// if (session.getType() == CallSession.TYPE_VIDEO_SHARE) {
	// return;
	// }
	// if (session.getType() == CallSession.TYPE_AUDIO) {
	// WorkLog.i("NetConnectService", "语音");
	// } else if (session.getType() == CallSession.TYPE_VIDEO) {
	// WorkLog.i("NetConnectService", "视频");
	// }
	// Intent newIntent = new Intent(context, CallIn_Activity.class);
	// newIntent.putExtra("session_id", session.getSessionId());
	// newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// context.startActivity(newIntent);
	// }
	// };

	// public void setDM() {
	// String sip = "222.246.189.244";
	// String sport = "443";
	// if (TextUtils.isEmpty(sip) || TextUtils.isEmpty(sport)) {
	// // WorkLog.i("NetConnectService", "DM地址错误");
	// } else {
	// LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP,
	// LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sip);
	// LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT,
	// LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sport);
	// }
	// }

}
