package com.vunke.sharehome.Call;

import java.util.List;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.HomeActivity;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.fragment.AttnFragment;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.sql.ContactsSqlite;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 接电话
 **/
public class CallIn_Activity extends BaseActivity {
	private CallSession callSession = null;
	private Button callins_answer, callouts_end, switch_call;
	private ImageView callins_icon;
	private TextView calllins_phoneName, callins_status, callins_phoneNumber;
	private AlertDialog alertDialog;
	private Intent intent;
	protected String Callnum = "11831726";
	private String calleeNumber;
	private List<ContactSummary> searchContact;
	private ContactsSqlite sqlite;
	private String CallNumbers;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_callins);
		getExtras();
		initViews();
		initCallName();
		registerReceivers();// 发送广播和接受广播

	}

	private void getExtras() {
		// intent=new Intent(mcontext,HomeService.class);
		// intent.putExtra("className", mcontext.getClass().getName());
		// startService(intent);
		long sessionId = getIntent().getLongExtra("session_id",
				CallSession.INVALID_ID);
		callSession = CallApi.getCallSessionById(sessionId);
		if (null == callSession) {
			finish();
			return;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Config.SearchContactApi1:
				if (searchContact != null && searchContact.size() != 0) {
					for (int i = 0; i < searchContact.size(); i++) {
						ContactSummary position = searchContact.get(i);
						// WorkLog.i("CallIn_Activity",
						// "当前名字"+position.getDisplayName());
						if (CallNumbers.substring(1).equals(
								position.getSearchMatchContent())) {
							calllins_phoneName.setText(position
									.getDisplayName());
						}
					}
				} else {
					selectSQL(CallNumbers);
				}
				break;
			case Config.SearchShareHomeContact2:
				if (contact != null && contact.size() != 0) {
					for (int i = 0; i < contact.size(); i++) {
						// WorkLog.i("CallIn_Activity",
						// "CallIn_Activity本地数据查询成功");
						calllins_phoneName.setText(contact.get(i)
								.getContactName());
					}
				}
				break;
			default:
				break;
			}
		}
	};

	private void initCallName() {
		callins_phoneNumber.setText("未知号码");
		if (callSession != null) {
			if (calleeNumber.length() >= 9) {
				if (calleeNumber.startsWith(Config.CALL_BEFORE + "8")
						|| calleeNumber.startsWith(Config.CALL_BEFORE + "9")) {
					CallNumbers = calleeNumber.substring(8,
							calleeNumber.length());
				} else {
					CallNumbers = calleeNumber.substring(8,
							calleeNumber.length());
				}
				// WorkLog.i("CallIn_Activity", "截取号码" + CallNumbers);
				callins_phoneNumber.setText(CallNumbers.substring(1));
				searchContact = ContactApi.searchContact(
						CallNumbers.substring(1), ContactApi.LIST_FILTER_ALL);
				handler.sendEmptyMessage(Config.SearchContactApi1);
			} else {
				selectSQL(calleeNumber);
			}
		}
	}

	public void selectSQL(String callnumber) {
		WorkLog.i("CallIn_Activity", "1:" + callnumber);
		callnumber = UiUtils.isMobileNO(callnumber) ? callnumber : callnumber
				.substring(1);
		WorkLog.i("CallIn_Activity", "2:" + callnumber);
		contact = UiUtils.SearchContact(callnumber);
		handler.sendEmptyMessage(Config.SearchShareHomeContact2);
	}

	private void initViews() {

		callins_icon = (ImageView) findViewById(R.id.callins_icon);

		calllins_phoneName = (TextView) findViewById(R.id.callins_phoneName);
		callins_phoneNumber = (TextView) findViewById(R.id.call_phoneNumber);

		callins_status = (TextView) findViewById(R.id.callins_status);
		callins_answer = (Button) findViewById(R.id.callins_answer);// 接听
		callins_answer.requestFocus();
		callouts_end = (Button) findViewById(R.id.callouts_end);// 拒接
		switch_call = (Button) findViewById(R.id.switch_call);// 切换
		SetOnClickListener(callins_answer, callouts_end, switch_call);
		if (callSession != null) {
			calleeNumber = callSession.getPeer().getNumber();
			// WorkLog.i("CallIn_Activity", "接电话" + calleeNumber);
			if (calleeNumber.length() > 8 && calleeNumber.contains(Callnum)) {
				String string = calleeNumber
						.substring(8, calleeNumber.length());
				calllins_phoneName.setText(string.substring(1));
			} else {
				calllins_phoneName.setText(calleeNumber);
			}
			if (callSession.getType() == callSession.TYPE_AUDIO) {
				boolean isVideoCall = (callSession.getType() == callSession.TYPE_AUDIO);
				switch_call.setVisibility(View.GONE);
				callins_status.setText("语音来电");
			}
			if (callSession.getType() == callSession.TYPE_VIDEO) {
				boolean isVideoCall = (callSession.getType() == callSession.TYPE_VIDEO);
				callins_status.setText("视频来电");
			}
		}
	}

	private void registerReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceive,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callins_answer:
			if (UiUtils.isCameraCanUse()) {
				if (callSession.getType() == CallSession.TYPE_AUDIO) {
					callSession.accept(CallSession.TYPE_AUDIO);
				} else if (callSession.getType() == CallSession.TYPE_VIDEO) {
					callSession.accept(CallSession.TYPE_VIDEO);
				}
			} else {
				NoPermission();
			}

			break;
		case R.id.callouts_end:
			callSession.terminate();
			break;
		case R.id.switch_call:
			if (callSession.getType() == CallSession.TYPE_AUDIO) {
				callSession.accept(CallSession.TYPE_VIDEO);
			} else if (callSession.getType() == CallSession.TYPE_VIDEO) {
				callSession.accept(CallSession.TYPE_AUDIO);
			}
			break;

		default:
			break;
		}
	}

	/* 调用状态改变时调用的事件处理。 */
	private BroadcastReceiver callStatusChangedReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			/* 呼叫会话应该核对. */
			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			switch (newStatus) {
			case CallSession.STATUS_CONNECTED:
				intent = new Intent();
				/* 根据呼叫类型，页面布局将更改 */
				if (session.getType() == CallSession.TYPE_AUDIO) {
					intent.setClass(mcontext, CallAudio_Activity.class);
				} else if (session.getType() == CallSession.TYPE_VIDEO) {
					intent.setClass(mcontext, CallVideo_Activity.class);
				}
				startActivity(intent);
				finish();
				break;
			case CallSession.STATUS_IDLE:
				String type = null;
				if (session.getType() == CallSession.TYPE_AUDIO) {
					type = Config.CALLRECORDER_TYPE_AUDIO_MISSED;
				} else if (session.getType() == CallSession.TYPE_VIDEO) {
					type = Config.CALLRECORDER_TYPE_VIDEO_MISSED;
					AddNotification();
				}
				// WorkLog.i("CallIn_Activity", "通话记录接电话"+calleeNumber);
				UiUtils.InsertCallLog(UiUtils.initCallNumber2(calleeNumber),
						type, "");
				RxBus.getInstance().post(Config.Update_CallLog);
				finish();
				break;
			default:
				break;
			}
		}

	};

	private void AddNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mcontext).setSmallIcon(R.drawable.home_icon)
				.setContentTitle(calllins_phoneName.getText()).setContentText("您有1个未接来电")
				.setAutoCancel(true);
		Intent intentActivity = new Intent();
		ComponentName name = new ComponentName(mcontext, HomeActivity.class);
		intentActivity.setComponent(name);
		intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(mcontext, 0,
				intentActivity, 0);
		builder.setContentIntent(pendingIntent);
		manager.notify(MISSED_FLAG, builder.build());

	}

	private final int MISSED_FLAG = 3;
	private List<Contact> contact;

	public void NoPermission() {
		Builder builder = new AlertDialog.Builder(mcontext);
		builder.setTitle("温馨提示");
		builder.setMessage("想家没有权限打开你的摄像头 \n建议设置如下:\n1、请到“设置 - 权限管理”中打开想家权限\n2、其他应用程序正在占用摄像头,请先将摄像头关闭");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});
		builder.setCancelable(false);
		alertDialog = builder.create();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		/* Unregister broadcast receiver. */
		unRegisterReceivers();
		if (intent != null) {
			stopService(intent);
		}
	}

	public void unRegisterReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceive);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
