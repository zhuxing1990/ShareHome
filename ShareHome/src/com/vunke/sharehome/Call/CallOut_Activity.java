package com.vunke.sharehome.Call;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.huawei.sci.CsfNty;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.model.Rx_ReLogin;
import com.vunke.sharehome.receiver.CallStatusChangedReceive;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.NetConnectService;
import com.vunke.sharehome.sql.ContactsSqlite;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 拨电话
 **/
@SuppressLint("NewApi") public class CallOut_Activity extends BaseActivity {
	/**
	 * 电话号码， 通话状态 ，时间
	 */
	private TextView call_phoneName, callout_status, callout_time, call_phoneNumber;

	/**
	 * 切换视频开关 ,静音 ， 挂断 拨号键盘
	 */
	private Button callout_switch_video, callout_mute, callout_cancel, call_handsfree;

	/**
	 * 头像
	 */
	private ImageView callout_icon;

	private Drawable drawable;

	/**
	 * 通话号码
	 */
	private String CallNumber;

	/**
	 * 判断是视频还是语音
	 */
	private boolean isVideoCall;

	/**
	 * 通话服务
	 */
	public CallSession callSession = null;

	/**
	 * 是否静音
	 */
	private boolean isMute = false;

	/**
	 * 判断拨号键盘
	 */
	private boolean isHandsfree = false;

	private Intent intent;

	/**
	 * 前缀
	 */
	protected String Callnum = "11831726";

	/**
	 * 联系人集合
	 */
	private List<ContactSummary> searchContact;

	// private ContactsSqlite sqlite;

	/**
	 * 播出的号码
	 */
	private String CallNumbers;

	/**
	 * 判断是不是在线状态
	 */
	private boolean iSRestartCall = false;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_callaudio);
		// WorkLog.e("CallOutActivity", "OnCreate" + UiUtils.isCameraCanUse());
		getExtras();
		initViews();
		
		initCall();
		initCallName();
		registerReceivers();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// unregisterReceivers();
		if (intent != null) {
			stopService(intent);
		}
		WorkLog.e("CallOut_Activity", "onDestroy");
		// if (!subscribe.isUnsubscribed()) {
		// subscribe.unsubscribe();
		// }
	}

	/**
	 * 初始化呼叫 是否呼叫成功
	 */
	private void initCall() {
		if (isVideoCall) {
			callSession = CallApi.initiateVideoCall(CallNumber);
		} else {
			callSession = CallApi.initiateAudioCall(CallNumber);
		}

		/* You had to finish the program when you received an error code. */
		switch (callSession.getErrCode()) {
		case CallSession.ERRCODE_OK:// 没问题
			WorkLog.e("CallOut_Activity", "CallSession.ERRCODE_OK");
			break;
		case CallSession.ERRCODE_FAILED:
			WorkLog.e("CallOut_Activity", "CallSession.ERRCODE_FAILED");
			showToast("发起呼叫失败。");
			break;
		case CallSession.ERRCODE_SERVER_DISCONNECTED:
			WorkLog.e("CallOut_Activity", "CallSession.ERRCODE_SERVER_DISCONNECTED");
			showToast("连接服务器失败，请检查网络。");
			Config.intent = new Intent(mcontext, NetConnectService.class);
			startService(Config.intent);
			Rx_ReLogin reLogin = new Rx_ReLogin();
			reLogin.code = Config.RESTART_RELOGIN;
			reLogin.isConnect = false;
			RxBus.getInstance().post(reLogin);
			break;
		case CallSession.ERRCODE_EXIST_CS_CALL:
			WorkLog.e("CallOut_Activity", "CallSession.ERRCODE_EXIST_CS_CALL");
			showToast("通话已存在。");
			break;

		default:
			WorkLog.e("CallOut_Activity", "未知的错误");
			showToast("未知的错误");
			break;
		}
		if (callSession.getErrCode() != CallSession.ERRCODE_OK) {

			if (callSession.getErrCode() == CallSession.ERRCODE_SERVER_DISCONNECTED) {
				if (iSRestartCall == false) {
					RestartCall();
				}
			}
			finishActivity();
		}
	}

	private void RestartCall() {
		Observable.timer(3000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<Long>() {

					@Override
					public void onCompleted() {
						this.unsubscribe();
					}

					@Override
					public void onError(Throwable throwable) {
						this.unsubscribe();
						WorkLog.e("CallOut_Activity", "错误" + throwable.getMessage());
					}

					@Override
					public void onNext(Long arg0) {
						iSRestartCall = true;
						initCall();
					}
				});
	}

	/**
	 * 关闭并销毁该页面
	 * 
	 **/
	public void finishActivity() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				WorkLog.e("CallOutActivity", "结束通话");
				finish();
			}
		}, 3000);
	}

	private List<Contact> contact;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Config.SearchContactApi1:
				if (searchContact != null && searchContact.size() != 0) {
					for (int i = 0; i < searchContact.size(); i++) {
						ContactSummary position = searchContact.get(i);
						// WorkLog.e("CallOut_Activity","当前名字"+position.getDisplayName());
						if (CallNumbers.substring(1).equals(position.getSearchMatchContent())) {
							call_phoneName.setText(position.getDisplayName());
						}
					}
				} else {
					selectSQL(CallNumbers);
				}
				break;
			case Config.SearchShareHomeContact2:
				if (contact != null && contact.size() != 0) {
					for (int i = 0; i < contact.size(); i++) {
						// WorkLog.e("CallOut_Activity",
						// "CallOut_Activity本地数据查询成功");
						call_phoneName.setText(contact.get(i).getContactName());
					}
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 初始化呼叫对方的姓名
	 */
	private void initCallName() {
		call_phoneName.setText("未知号码");
		if (callSession != null) {
			if (CallNumber.length() >= 9) {
				if (CallNumber.startsWith(Config.CALL_BEFORE + "8")
						|| CallNumber.startsWith(Config.CALL_BEFORE + "9")) {
					CallNumbers = CallNumber.substring(8, CallNumber.length());
				} else {
					CallNumbers = CallNumber.substring(8, CallNumber.length());
				}
				// WorkLog.e("CallOut_Activity", "截取号码" + CallNumbers);
				call_phoneNumber.setText(CallNumbers.substring(1));
				searchContact = ContactApi.searchContact(CallNumbers.substring(1), ContactApi.LIST_FILTER_ALL);
				handler.sendEmptyMessage(Config.SearchContactApi1);
			} else {
				selectSQL(CallNumber);
			}
		}
	}

	public void selectSQL(String callnumber) {
		// WorkLog.e("CallOut_Activity", "1:" + callnumber);
		callnumber = UiUtils.isMobileNO(callnumber) ? callnumber : callnumber.substring(1);
		// WorkLog.e("CallOut_Activity", "2:" + callnumber);
		contact = UiUtils.SearchContact(callnumber);
		handler.sendEmptyMessage(Config.SearchShareHomeContact2);
	}

	/**
	 * 获取意图
	 */
	private void getExtras() {
		// intent =new Intent(mcontext,HomeService.class);
		// intent.putExtra("className", mcontext.getClass().getName());
		// startService(intent);
		CallNumber = getIntent().getStringExtra("PhoneNumber");
		isVideoCall = getIntent().getBooleanExtra("is_video_call", false);

	}

	/**
	 * 初始化控件
	 */
	private void initViews() {
		callout_icon = (ImageView) findViewById(R.id.callout_icon);

		call_phoneName = (TextView) findViewById(R.id.call_phoneName);
		call_phoneNumber = (TextView) findViewById(R.id.call_phoneNumber);

		if (CallNumber.length() > 8 && CallNumber.contains(Callnum)) {
			String string = CallNumber.substring(8, CallNumber.length());
			call_phoneName.setText(string.substring(1));
		} else {
			call_phoneName.setText(CallNumber);
		}

		callout_status = (TextView) findViewById(R.id.callout_status);
		callout_status.setText("正在拨号...");
		callout_time = (TextView) findViewById(R.id.callout_time);
		callout_switch_video = (Button) findViewById(R.id.callout_switch_video);
		callout_switch_video.setVisibility(View.GONE);
		callout_mute = (Button) findViewById(R.id.callout_mute);
		callout_cancel = (Button) findViewById(R.id.callout_cancel);
		call_handsfree = (Button) findViewById(R.id.call_handsfree);
		SetOnClickListener(callout_switch_video, callout_mute, callout_cancel, call_handsfree);
	}

	private CallStatusChangedReceive callStatusChangedReceive2;

	/**
	 * 发送和注册广播
	 */
	private void registerReceivers() {
		callStatusChangedReceive2 = new CallStatusChangedReceive(mcontext, callSession, CallNumber, call_phoneName);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(callStatusChangedReceive2,
				new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));
	}

	/**
	 * 取消注册广播
	 */
	private void unregisterReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(callStatusChangedReceive2);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callout_switch_video:

			break;
		case R.id.callout_mute:
			isMute = !isMute;
			if (isMute) {
				callSession.mute();
				callout_status.setText("已静音");
				MuteDrawableTop(false);
			} else {
				callSession.unMute();
				callout_status.setText("正在拨号...");
				MuteDrawableTop(true);
			}
			break;
		case R.id.callout_cancel:
			if (callSession.getErrCode() != CallSession.ERRCODE_OK) {
				finish();
				return;
			}
			WorkLog.e("CallOut_Activity", "点击结束通话");
			 if (!mcontext.isDestroyed()) {
				 mcontext.finish();
			 }
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					callSession.terminate();
					
				}
			}).start();
			break;
		case R.id.call_handsfree:
			isHandsfree = !isHandsfree;
			AudioManager audioManamger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			boolean speakerState = !audioManamger.isSpeakerphoneOn();
			audioManamger.setSpeakerphoneOn(speakerState);
			if (isHandsfree) {
				HandsfreeDrawableTop(true);
				call_handsfree.setText("听筒");
			} else {
				HandsfreeDrawableTop(false);
				call_handsfree.setText("免提");
			}
			break;
		/*
		 * case value: break;
		 */

		default:
			break;
		}
	}

	public void MuteDrawableTop(boolean b) {
		if (b) {
			drawable = getResources().getDrawable(R.drawable.call_mute);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			callout_mute.setCompoundDrawables(null, drawable, null, null);
		} else {
			drawable = getResources().getDrawable(R.drawable.call_mute2);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			callout_mute.setCompoundDrawables(null, drawable, null, null);
		}
	}

	public void HandsfreeDrawableTop(boolean b) {
		if (b) {
			drawable = getResources().getDrawable(R.drawable.call_mute);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			call_handsfree.setCompoundDrawables(null, drawable, null, null);

		} else {
			drawable = getResources().getDrawable(R.drawable.call_mute2);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			call_handsfree.setCompoundDrawables(null, drawable, null, null);

		}
	}

	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
