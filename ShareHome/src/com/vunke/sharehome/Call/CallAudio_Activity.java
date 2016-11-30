package com.vunke.sharehome.Call;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.system.SysApi.PhoneUtils;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.HomeService;
import com.vunke.sharehome.sql.ContactsSqlite;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 语音通话
 **/
public class CallAudio_Activity extends BaseActivity {
	private CallSession videoShareCallsession = null;
	private boolean isVideoShareCaller = false;
	public static String PARAM_SESSION_ID = "PARAM_SESSION_ID";
	public static String PARAM_IS_CALLER = "PARAM_IS_CALLER";
	private AlertDialog alertDialog;
	private CallSession callSession;
	private Drawable drawable;
	private boolean isMute = false;
	private boolean isHandsfree = false;
	private Timer timer;
	private int callTime;
	private TextView call_phoneName, callout_status, callout_time,call_phoneNumber;
	private ImageView callout_icon;
	private Button callout_switch_video, callout_mute,//callout_callKeyboard
			callout_cancel, call_handsfree;
	private Intent intent;
	protected String Callnum = "11831726";
	private String CallNumber;
	private List<ContactSummary> searchContact;
//	private ContactsSqlite sqlite;
	private String CallNumbers;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_callaudio);
		initCallSession();
		initViews();
		initCallName();
		registerReceivers();/* 注册广播. */
		startCallTimeTask();/* 设置通话的时间. */
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
						// WorkLog.e("CallAudio_Activity","当前名字"+position.getDisplayName());
						if (CallNumbers.substring(1).equals(
								position.getSearchMatchContent())) {
							call_phoneNumber.setText(position
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
						// WorkLog.e("CallAudio_Activity",
						// "CallOut_Activity本地数据查询成功");
						call_phoneNumber.setText(contact.get(i)
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
		call_phoneNumber.setText("未知号码");
		if (callSession != null) {
			if (CallNumber.length() >= 9) {
				if (CallNumber.startsWith(Config.CALL_BEFORE + "8")
						|| CallNumber.startsWith(Config.CALL_BEFORE + "9")) {
					CallNumbers = CallNumber.substring(8, CallNumber.length());
				} else {
					CallNumbers = CallNumber.substring(8, CallNumber.length());
				}
				// WorkLog.e("CallAudio_Activity", "截取号码" + CallNumbers);
				call_phoneNumber.setText(CallNumbers.substring(1));
				searchContact = ContactApi.searchContact(
						CallNumbers.substring(1), ContactApi.LIST_FILTER_ALL);
				handler.sendEmptyMessage(Config.SearchContactApi1);
			} else {
				selectSQL(CallNumber);
			}
		}
	}
	public void selectSQL(String callnumber) {
		WorkLog.e("CallAudio_Activity", "1:" + callnumber);
		callnumber = UiUtils.isMobileNO(callnumber) ? callnumber : callnumber
				.substring(1);
		WorkLog.e("CallAudio_Activity", "2:" + callnumber);
		contact = UiUtils.SearchContact(callnumber);
		handler.sendEmptyMessage(Config.SearchShareHomeContact2);
	}

	public void initCallSession() {
		intent = new Intent(mcontext, HomeService.class);
		intent.putExtra("className", mcontext.getClass().getName());
		startService(intent);
		callSession = CallApi.getForegroudCallSession();
		if (null == callSession) {
			LogApi.d("V2OIP",
					"ACT_DemoCallTalking onCreate: no call is talking any more.");
			finish();
			return;
		}
	}

	private void initViews() {
		call_phoneName = (TextView) findViewById(R.id.call_phoneName);
		call_phoneNumber = (TextView) findViewById(R.id.call_phoneNumber);
		callout_status = (TextView) findViewById(R.id.callout_status);
		
		callout_status.setText("通话中");
		callout_time = (TextView) findViewById(R.id.callout_time);
		callout_icon = (ImageView) findViewById(R.id.callout_icon);

		callout_switch_video = (Button) findViewById(R.id.callout_switch_video);
//		callout_callKeyboard = (Button) findViewById(R.id.callout_callKeyboard);
//		callout_callKeyboard.setVisibility(View.VISIBLE);
		callout_mute = (Button) findViewById(R.id.callout_mute);
		callout_cancel = (Button) findViewById(R.id.callout_cancel);
		call_handsfree = (Button) findViewById(R.id.call_handsfree);
		call_handsfree.setText("听筒");
		HandsfreeDrawableTop(false);
		SetOnClickListener(callout_switch_video,callout_mute, callout_cancel, call_handsfree);// callout_callKeyboard,
		if (callSession != null) {
			CallNumber = callSession.getPeer().getNumber();
			if (CallNumber.contains(Callnum) && CallNumber.length() > 8) {
				String number = CallNumber.substring(8, CallNumber.length());
				call_phoneName.setText(number.substring(1));
			} else {
				call_phoneName.setText(CallNumber);
			}
		}

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callout_switch_video:
			if (callSession.isAbleToAddVideo()) {
				callSession.addVideo();
				Toast.makeText(getApplicationContext(), "已经发送邀请,等待对方接受……",
						Toast.LENGTH_LONG).show();
			}
			break;
//		case R.id.callout_callKeyboard:
//
//			break;
		case R.id.callout_mute:
			isMute = !isMute;

			if (isMute) {
				callSession.mute();
				callout_status.setText("已静音");
				MuteDrawableTop(false);
			} else {
				callSession.unMute();
				callout_status.setText("通话中");
				MuteDrawableTop(true);
			}
			break;
		case R.id.callout_cancel:
			callSession.terminate();
			break;
		case R.id.call_handsfree:
			isHandsfree = !isHandsfree;
			AudioManager audioManamger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			boolean speakerState = !audioManamger.isSpeakerphoneOn();
			audioManamger.setSpeakerphoneOn(speakerState);
			if (isHandsfree) {
				HandsfreeDrawableTop(true);
			} else {
				HandsfreeDrawableTop(false);
			}
			break;

		default:
			break;
		}

	}

	public void MuteDrawableTop(boolean b) {
		if (b) {
			drawable = getResources().getDrawable(R.drawable.call_mute);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			callout_mute.setCompoundDrawables(null, drawable, null, null);
		} else {
			drawable = getResources().getDrawable(R.drawable.call_mute2);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			callout_mute.setCompoundDrawables(null, drawable, null, null);
		}
	}

	public void HandsfreeDrawableTop(boolean b) {
		if (b) {
			drawable = getResources().getDrawable(R.drawable.call_handsfree);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			call_handsfree.setCompoundDrawables(null, drawable, null, null);
			call_handsfree.setText("免提");
		} else {
			drawable = getResources().getDrawable(R.drawable.call_handsfree2);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			call_handsfree.setCompoundDrawables(null, drawable, null, null);
			call_handsfree.setText("听筒");
		}
	}

	private void registerReceivers() {
		/*
		 * 注册广播接收器，用于处理呼叫邀请活动。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callInvitationReciever,
						new IntentFilter(CallApi.EVENT_CALL_INVITATION));
		/*
		 * 注册广播接收器，用于检测电话状态。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));
		/*
		 * 注册广播接收器，用于检测呼叫类型被邀请的。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						callTypeChangeInvitationReceiver,
						new IntentFilter(
								CallApi.EVENT_CALL_TYPE_CHANGED_INVITATION));
		/* 注册一个广播接收器，用于检测呼叫类型。 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callTypeChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_TYPE_CHANGED));
		/*
		 * 注册广播接收器，用于检测呼叫类型是否已更改为拒绝。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						callTypeChangeRejectedReceiver,
						new IntentFilter(
								CallApi.EVENT_CALL_TYPE_CHANGED_REJECTED));
		/*
		 * 注册广播接收器，为了应对服务质量报告事件。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callQosReportReceiver,
						new IntentFilter(CallApi.EVENT_CALL_QOS_REPORT));

	}

	private void unRegisterReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callInvitationReciever);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangeInvitationReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangeRejectedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callQosReportReceiver);
	}

	/* 收到一个视频分享的电话 */
	private BroadcastReceiver callInvitationReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			videoShareCallsession = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (null == videoShareCallsession) {
				return;
			}
			/* only received by callee. */
			if (CallSession.TYPE_VIDEO_SHARE == videoShareCallsession.getType()) {
				isVideoShareCaller = false;
				Toast.makeText(mcontext, "A Video Share Invitation Incoming",
						Toast.LENGTH_LONG).show();
			}
		}
	};

	/* 视频共享 */
	private BroadcastReceiver callStatusChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			/* The status of video share call is different from another call. */
			if (session == videoShareCallsession) {
				int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
						CallSession.STATUS_IDLE);

				switch (newStatus) {
				case CallSession.STATUS_ALERTING:
					Toast.makeText(getApplicationContext(), "视频共享会话提醒",
							Toast.LENGTH_LONG).show();
					break;
				case CallSession.STATUS_CONNECTED: // accept by callee
					Intent newIntent = new Intent(mcontext,
							CallVideo_Activity.class);
					newIntent
							.putExtra(PARAM_SESSION_ID, session.getSessionId());
					newIntent.putExtra(PARAM_IS_CALLER, isVideoShareCaller);
					startActivityForResult(newIntent, 0);
					break;
				case CallSession.STATUS_IDLE: // reject by callee
					if (isVideoShareCaller) {
						Toast.makeText(getApplicationContext(), "视频共享会话终止",
								Toast.LENGTH_LONG).show();
					}
					break;
				default:
					break;
				}

				return;
			}

			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			switch (newStatus) {
			case CallSession.STATUS_HOLD:
				break;
			case CallSession.STATUS_HELD:
				break;
			case CallSession.STATUS_CONNECTED:
				break;
			case CallSession.STATUS_IDLE:
				String type = null;
				if (session.getType() == CallSession.TYPE_AUDIO) {
					type = Config.CALLRECORDER_TYPE_AUDIO_RECEIVED;
				} else if (session.getType() == CallSession.TYPE_VIDEO) {
					type = Config.CALLRECORDER_TYPE_VIDEO_RECEIVED;
				}
//				WorkLog.e("CallAudio_Activity", "通话记录语音"+CallNumber);
				UiUtils.InsertCallLog(UiUtils.initCallNumber2(CallNumber), type,
						callout_time.getText().toString());
				RxBus.getInstance().post(Config.Update_CallLog);
				finish();
				break;
			default:
				break;
			}
		}
	};

	/* 收到邀请的视频通话 */
	private BroadcastReceiver callTypeChangeInvitationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			Builder dl = new AlertDialog.Builder(mcontext);
			dl.setTitle("添加视频邀请");
			// dl.setMessage(R.string.fail);
			dl.setMessage("对方邀请视频通话，接受或者不接受?");
			dl.setPositiveButton("接受", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callSession.acceptAddVideo();
					if (alertDialog != null) {
						alertDialog.dismiss();
						alertDialog = null;
					}
				}
			});
			dl.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					callSession.rejectAddVideo();
					if (alertDialog != null) {
						alertDialog.dismiss();
						alertDialog = null;
					}
				}
			});

			alertDialog = dl.create();
			alertDialog.setCancelable(false);
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
		}
	};

	/* 邀请视频通话已被接受 */
	private BroadcastReceiver callTypeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int newType = intent.getIntExtra(CallApi.PARAM_NEW_TYPE, -1);
			if (newType == CallSession.TYPE_VIDEO) {
				Intent newIntent = new Intent(mcontext,
						CallVideo_Activity.class);
				startActivity(newIntent);
				finish();
			}
		}
	};

	/* 邀请视频通话已被拒绝 */
	private BroadcastReceiver callTypeChangeRejectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			Toast.makeText(getApplicationContext(), "你的邀请被拒绝",
					Toast.LENGTH_LONG).show();
		}
	};

	/* 处理服务质量报告事件。 */
	private BroadcastReceiver callQosReportReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int quality = intent.getIntExtra(CallApi.PARAM_CALL_QOS,
					CallApi.QOS_QUALITY_NORMAL);
			switch (quality) {
			case CallApi.QOS_QUALITY_GOOD:
				break;

			case CallApi.QOS_QUALITY_NORMAL:
				break;

			case CallApi.QOS_QUALITY_BAD:
				break;

			default:
				break;
			}
		}
	};
	private List<Contact> contact;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (intent != null) {
			stopService(intent);
		}
		/* unregister broadcast receivers. */
		unRegisterReceivers();
		if (null != alertDialog) {
			alertDialog.dismiss();
			alertDialog = null;
		}
		/*
		 * end call time task.You should stop the time task when this activity
		 * is destroy.
		 */
		stopCallTimeTask();
	}

	private void stopCallTimeTask() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void startCallTimeTask() {
		if (callSession != null) {
			timer = new Timer();
			callTime = (int) ((System.currentTimeMillis() - callSession
					.getOccurDate()) / 1000);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					callTime++;
					handler.post(new Runnable() {
						@Override
						public void run() {
							callout_time.setText(PhoneUtils
									.getCallDurationTime(callTime));
						}
					});
				}
			}, 1000, 1000);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Builder dl = new AlertDialog.Builder(mcontext);
			dl.setTitle("关闭视频并退出");
			// dl.setMessage(R.string.fail);
			dl.setMessage("你确定要关闭视频并退出?");
			dl.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callSession.terminate();
					if (alertDialog != null) {
						alertDialog.dismiss();
						alertDialog = null;
					}
				}
			});
			dl.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (alertDialog != null) {
						alertDialog.dismiss();
						alertDialog = null;
					}
				}
			});

			alertDialog = dl.create();
			alertDialog.show();
			break;
		case KeyEvent.KEYCODE_HOME:
			return false;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
