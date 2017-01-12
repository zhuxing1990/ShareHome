package com.vunke.sharehome.base;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.huawei.rcs.RCSApplication;
import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.hme.HmeAudio;
import com.huawei.rcs.hme.HmeVideo;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.stg.NatStgHelper;
import com.huawei.rcs.system.SysApi;
import com.huawei.rcs.tls.DefaultTlsHelper;
import com.huawei.rcs.upgrade.UpgradeApi;
import com.lzy.okhttputils.OkHttpUtils;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.Call.CallIn_Activity;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.receiver.BringToFrontReceiver;
import com.vunke.sharehome.service.GrayService;
import com.vunke.sharehome.service.NetConnectService;
import com.vunke.sharehome.service.ShareHomeService;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 华为SDK RCSApplication 用于初始化SDK
 * */
public class HuaweiSDKApplication extends RCSApplication {
	public static HuaweiSDKApplication application;

	/**
	 * 初始化 UI 需要的组件
	 * */
	@Override
	public void onCreate() {
		super.onCreate();
		application = this;
		DbCore.init(this, "sharehome.db");
		OkHttpUtils.init(this);
		UpgradeApi.init(this);// 初始化更新API
		// 初始化API
		HmeAudio.setup(this);
		HmeVideo.setup(this);
		CallApi.init(this);
		ContactApi.init(this);
		LogApi.init(this);
		CallApi.setConfig(CallApi.CONFIG_MAJOR_TYPE_VIDEO_PREFER_SIZE,
				CallApi.CONFIG_MINOR_TYPE_DEFAULT, "2");
		CallApi.setConfig(CallApi.CONFIG_MAJOR_TYPE_VIDEO_DISPLAY_TYPE,
				CallApi.CONFIG_MINOR_TYPE_DEFAULT, "0");
		CallApi.setCustomCfg(CallApi.CFG_CALLLOG_INSERT_SYS_DB,
				CallApi.CFG_VALUE_NO);
		SysApi.loadTls(new DefaultTlsHelper());
		SysApi.loadStg(new NatStgHelper());
		SysApi.setDMVersion("v1.1.30.8");// 可能需要修改
		/**设置日志加密**/
//		SysApi.setLogEncrypt(false);
		// ConferenceApi.initiateApi(getApplicationContext());//
		// 初始化ConferenceAPI
		setDM();
		// CaasOmp.init();
		// CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_SERVER_IP,
		// "205.177.226.80");
		// CaasOmpCfg.setUint(CaasOmpCfg.EN_OMP_CFG_SERVER_PORT, 8543);
		// 保活的作用，确保应用程序不被停掉
		Intent intent = new Intent(this, ShareHomeService.class);
		intent.setPackage(getPackageName());
		startService(intent);
		
		Intent serviceIntent = new Intent(this,GrayService.class);
		startService(serviceIntent);
		// CrashHandler.getInstance().init(getApplicationContext());
		
		if (Config.intent == null) {
			Config.intent = new Intent(this, NetConnectService.class);
			startService(Config.intent);
		}
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callInvitationReceiver,
						new IntentFilter(CallApi.EVENT_CALL_INVITATION));
		// APPNotification();
	}

	public static HuaweiSDKApplication getApplication() {
		if (application == null) {
			application = new HuaweiSDKApplication();
		}
		return application;
	}

	BroadcastReceiver callInvitationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);

			if (session.getType() == CallSession.TYPE_VIDEO_SHARE) {
				return;
			}
			if (session.getType() == CallSession.TYPE_AUDIO) {
				WorkLog.i("HuaweiSDKApplication", "语音来电");
			} else if (session.getType() == CallSession.TYPE_VIDEO) {
				WorkLog.i("HuaweiSDKApplication", "视频来电");
			}
			Intent newIntent = new Intent(context, CallIn_Activity.class);
			newIntent.putExtra("session_id", session.getSessionId());
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newIntent);
		}
	};

	@Override
	public void onTerminate() {
		super.onTerminate();
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callInvitationReceiver);
	}

	/**
	 * TLS 安全认证 为了保障用户的数据安全，就必须进行安全认证。需要找相应运营商获取根 证书，并保存为 pem 文件。并必须调用
	 * SysApi.setTrustCaFilePath()来加载指 定的根证书文件，如
	 * SysApi.setTrustCaFilePath("/user/safety/rootcert.pem")，需
	 * 要传入准确的绝对路径，否则会导致证书校验失败。 根证书 pem 文件内容示例如下，每个服务器的密钥
	 * 以-----BEGINCERTIFICATE-----开始， 以-----END CERTIFICATE-----结束。
	 * 
	 * -----BEGIN CERTIFICATE-----
	 * Y2VydC5hdC9jZ2ktYmluL2EtY2VydC1hZHZhbmNlZC5jZ2kwDQYJKoZIhvcNAQEFmsC1
	 * +orfKT
	 * ebsg69aMaCx7o6jNONRmR/7TVaPf8/k6g52cHZ9YWjQvup22b5rWxGJ5r5LZ4vCPmF4
	 * +T4lutj UYAa/lGuQTg= -----END CERTIFICATE----- -----BEGIN
	 * CERTIFICATE-----
	 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX -----END CERTIFICATE-----
	 **/
	public void SetTrust() {
		// SysApi.setTrustCaFilePath("/rootcert.pem");
	}

	public void setDM() {
		String sip = "222.246.189.244";
		String sport = "443";
		if (TextUtils.isEmpty(sip) || TextUtils.isEmpty(sport)) {
			//WorkLog.i("HuaweiSDKApplication", "DM地址错误");
		} else {
			LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP,
					LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sip);
			LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT,
					LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sport);
		}
	}

	public void APPNotification() {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher).setContentTitle(
				"欢迎进入想家");
		// .setContentText("您收到了一条新消息");
		Intent intent = new Intent(BringToFrontReceiver.ACTION_BRING_TO_FRONT);
		mBuilder.setContentIntent(PendingIntent.getBroadcast(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT));
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		Notification notification = mBuilder.build();
		mNotificationManager.notify(1, notification);
	}
}
