package com.vunke.sharehome.receiver;

import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.model.Rx_ReLogin;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.utils.WorkLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LoginStatusChangedReceiver extends BroadcastReceiver {
	/**
	 *广播的次数 
	 */
	private int longTime = 0;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(LoginApi.EVENT_LOGIN_STATUS_CHANGED)) {
			longTime++;
			WorkLog.e("LoginStatusChangedReceiver", "检测登录状态次数:" + longTime);
			int new_status = intent.getIntExtra(LoginApi.PARAM_NEW_STATUS, -1);
			LogApi.d("tag", "the status is " + new_status);
			int reason = intent.getIntExtra(LoginApi.PARAM_REASON, -1);
			WorkLog.e("HomeActivity", "reason:" + reason + "\n" + "new_status:"
					+ new_status);
			switch (new_status) {
			case LoginApi.STATUS_DISCONNECTED:
				WorkLog.e("reason",
						mapReasonStringtoReasonCode(reason, context));
				break;
			case LoginApi.STATUS_CONNECTED:
				Rx_ReLogin reLogin = new Rx_ReLogin();
				reLogin.code = Config.RESTART_RELOGIN;
				reLogin.isConnect = true;
				RxBus.getInstance().post(reLogin);
				break;

			default:
				break;
			}

		}

	}

	/**
	 * 下线的原因
	 * */
	private String mapReasonStringtoReasonCode(int reason, Context context) {
		String reasonStr = null;
		Rx_ReLogin reLogin = new Rx_ReLogin();
		reLogin.code = Config.RESTART_RELOGIN;
		reLogin.isConnect = false;
		switch (reason) {
		case LoginApi.REASON_AUTH_FAILED:// 鉴权失败，用户名或密码错误
			reasonStr = "auth failed";
//			showToast("登录失败，用户名或密码错误");
//			showAlderDialog("登录失败，用户名或密码错误");
			break;
		case LoginApi.REASON_CONNCET_ERR:// 连接错误
			reasonStr = "connect error";
//			showToast("连接错误");
//			showAlderDialog("咦，好像网络出了点问题");
			RxBus.getInstance().post(reLogin);
			break;
		case LoginApi.REASON_NET_UNAVAILABLE:// 没有网络
			reasonStr = "no network";
//			showToast("当前网络不可用，请检查网络设置");
//			showAlderDialog2("当前网络不可用，请检查网络设置");
			RxBus.getInstance().post(reLogin);
			break;
		case LoginApi.REASON_NULL:// 空
//			showToast("未知的异常！");
//			showAlderDialog("未知的异常！");
			reasonStr = "none";
			break;
		case LoginApi.REASON_SERVER_BUSY:// 服务器繁忙
			reasonStr = "server busy";
//			showToast("服务器繁忙！");
//			showAlderDialog2("服务器繁忙！");
			RxBus.getInstance().post(reLogin);
			break;
		case LoginApi.REASON_SRV_FORCE_LOGOUT:// 强行注销
			reasonStr = "force logout";
//			showToast("账号异地登录，被服务器强制下线");
//			showAlderDialog("账号异地登录，被服务器强制下线");
			break;
		case LoginApi.REASON_USER_CANCEL:// 用户取消了
			reasonStr = "user canceled";
//			showToast(" 用户取消了！");
			break;
		case LoginApi.REASON_WRONG_LOCAL_TIME:// 当地时间错了
			reasonStr = "wrong local time";
//			showToast("当地时间错误");
//			showAlderDialog("客户端时间错误");
			break;
		case LoginApi.REASON_ACCESSTOKEN_INVALID:// 无效的访问令牌
			reasonStr = "invalid access token";
//			showToast("无效的访问令牌");
//			showAlderDialog("无效的访问令牌");
			break;
		case LoginApi.REASON_ACCESSTOKEN_EXPIRED:// 访问令牌过期
			reasonStr = "access token expired";
//			showToast(" 访问令牌过期");
//			showAlderDialog("访问令牌过期");
			break;
		case LoginApi.REASON_APPKEY_INVALID:// 无效的application 密钥
			reasonStr = "invalid application key";
//			showToast("无效的application 密钥");
//			showAlderDialog("无效的application 密钥");
			break;
		case LoginApi.REASON_UNKNOWN:// 未知的
		default:
//			showToast("未知的异常！");
			RxBus.getInstance().post(reLogin);
			reasonStr = "unknown";
			break;
		}
		return reasonStr;
	}
}
