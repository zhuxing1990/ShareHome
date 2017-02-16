package com.vunke.sharehome.receiver;

import org.json.JSONObject;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.Call.CallAudio_Activity;
import com.vunke.sharehome.Call.CallVideo_Activity;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("NewApi") public class CallStatusChangedReceive extends BroadcastReceiver {
	/**
	 * 记录STATUS_ALERTING次数据 如果些状态出现一次则发送短信
	 * 
	 */
	private int alerting_count = 0;
	private long current_time = 0;
	private long dy_time = 0;
	private long dy_status_time = 0;
	private CallSession callSession = null;// 通话服务
	private Activity mcontext;
	private String CallNumber;// 通话号码
	private TextView call_phoneName;

	public CallStatusChangedReceive(Activity mcontext, CallSession callSession, String CallNumber,
			TextView call_phoneName) {
		this.mcontext = mcontext;
		this.callSession = callSession;
		this.CallNumber = CallNumber;
		this.call_phoneName = call_phoneName;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() == CallApi.EVENT_CALL_STATUS_CHANGED) {
			CallSession session = (CallSession) intent.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				WorkLog.i("CallOut_Activity", "!callSession.equals(session)");
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS, CallSession.STATUS_IDLE);
			switch (newStatus) {
			case CallSession.STATUS_CONNECTED:// 连接成功
				WorkLog.i("CallOut_Activity", "STATUS:STATUS_CONNECTED");
				intent = new Intent();
				if (session.getType() == CallSession.TYPE_AUDIO) {
					intent.setClass(mcontext, CallAudio_Activity.class);
				} else if (session.getType() == CallSession.TYPE_VIDEO) {
					intent.setClass(mcontext, CallVideo_Activity.class);
				}
				mcontext.startActivity(intent);
				mcontext.finish();
				break;
			case CallSession.STATUS_IDLE:// 空闲
				WorkLog.i("CallOut_Activity", "STATUS:STATUS_IDLE");
				long errorCode = intent.getLongExtra(CallApi.PARAM_SIP_STATUS_CODE, 0);

				if (errorCode == 408) {
					dy_status_time = System.currentTimeMillis() - current_time;

					if (alerting_count == 1 && dy_time < 2000) {
						// 对方正忙,主叫不挂断
						if (dy_status_time > 90000) {
							WorkLog.i("CallOut_Activity", "only_status对方无法接通: 对方正忙,主叫不挂断");
							sendSms();
						}
						// 对方不在线,不挂断
						else if (dy_status_time > 10000) {
							sendSms();
							WorkLog.i("CallOut_Activity", "only_status对方无法接通:对方不在线,不挂断");
						}

					}
					// 在线没网络,不挂断
					else if (alerting_count == 1 && dy_time >= 2000) {
						if (dy_status_time > 30000) {
							sendSms();
							WorkLog.i("CallOut_Activity", "only_status对方无法接通:在线没网络,不挂断");
						}
					}
					// 被叫拒接,不挂断
					else if (alerting_count == 3) {
						WorkLog.i("CallOut_Activity", "only_status对方无法接通:被叫拒接,不挂断");
					}
				} else {
					// 对方正忙或不在线,主叫挂断
					if (alerting_count == 1 && dy_time < 2000) {
						sendSms();
						WorkLog.i("CallOut_Activity", "only_status对方无法接通: 对方正忙或不在线,主叫挂断");
					}
					// 在线没网络,挂断
					else if (alerting_count == 1 && dy_time >= 2000) {
						sendSms();
						WorkLog.i("CallOut_Activity", "only_status对方无法接通:在线没网络,挂断");
					}
					// 被叫拒接,挂断
					else if (alerting_count == 3) {
						WorkLog.i("CallOut_Activity", "only_status对方无法接通:被叫拒接,挂断");
					}
				}
				String type = null;
				if (session.getType() == CallSession.TYPE_AUDIO) {
					type = Config.CALLRECORDER_TYPE_AUDIO_DIAL;
				} else if (session.getType() == CallSession.TYPE_VIDEO) {
					type = Config.CALLRECORDER_TYPE_VIDEO_DIAL;
				}
				 WorkLog.i("CallOut_Activity","通话记录拨电话"+CallNumber);
				UiUtils.InsertCallLog(UiUtils.initCallNumber2(CallNumber), type, "");
				RxBus.getInstance().post(Config.Update_CallLog);
				 WorkLog.i("CallOutActivity", "结束通话");
				 LocalBroadcastManager.getInstance(mcontext).unregisterReceiver(this);
				 if (!mcontext.isDestroyed()) {
					 mcontext.finish();
				 }
				break;
			case CallSession.STATUS_OUTGOING:// 呼出
				WorkLog.i("CallOut_Activity", "STATUS:STATUS_OUTGOING");
				alerting_count = 0;
				dy_time = 0;
				current_time = System.currentTimeMillis();
				break;
			case CallSession.STATUS_ALERTING:// 接听
				WorkLog.i("CallOut_Activity", "STATUS:STATUS_ALERTING");
				alerting_count++;
				if (alerting_count == 1) {
					dy_time = System.currentTimeMillis() - current_time;
					WorkLog.i("CallOut_Activity", "拨打电话1至3之间的时间隔" + dy_time + "");
				}

				break;

			default:
				WorkLog.i("CallOut_Activity", "未知的异常");
				break;
			}
		}

	}

	private void sendSms() {
		// callTime,current_time
		try {
			String getCalledName = UiUtils.initCallNumber2(CallNumber);
			String getUserName = UiUtils.GetUserName(mcontext);
			JSONObject params = new JSONObject();
			params.put("callTime", current_time);// 通话时间
			params.put("calledName", call_phoneName.getText());// 被叫电话
			params.put("calledPhone", getCalledName.substring(1));// 被叫在想家联系人中的姓名
			if (getCalledName.startsWith("8")) {
				params.put("calledType", "8");
			} else if (getCalledName.startsWith("9")) {
				params.put("calledType", "9");
			}
			params.put("callingPhone", getUserName.substring(1));
			if (getUserName.startsWith("8")) {
				params.put("callingType", "8");
			}
			if (getUserName.startsWith("9")) {
				params.put("callingType", "9");
			}
			// WorkLog.i("CallOut_Activity", "sendJSONData:" +
			// params.toString());
			if (params.length() == 0 || params == null) {
				return;
			}
			OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.MissedCall).tag(this).params("json", params.toString())
					.execute(new StringCallback() {

						@Override
						public void onResponse(boolean b, String s, Request request, @Nullable Response response) {
							WorkLog.i("CallOut_Activity", "data:"+s);

						}

						@Override
						public void onError(boolean isFromCache, Call call, @Nullable Response response,
								@Nullable Exception e) {
							super.onError(isFromCache, call, response, e);
							WorkLog.i("CallOut_Activity", "请求错误,网络发送异常");
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
