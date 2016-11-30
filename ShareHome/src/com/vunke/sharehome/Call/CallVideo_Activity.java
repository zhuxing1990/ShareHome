package com.vunke.sharehome.Call;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.call_recording.CallSessionRecording;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.system.SysApi.PhoneUtils;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.CaaSSdkService;
import com.vunke.sharehome.service.HomeService;
import com.vunke.sharehome.sql.ContactsSqlite;
import com.vunke.sharehome.utils.MyOnTouch;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 视频通话
 **/
public class CallVideo_Activity extends BaseActivity implements OnTouchListener {
	private CallSession callSession;
	private Drawable drawable;
	private boolean isMute = false;
	private boolean isHandsfree = false;
	// private boolean isCamera = false;
	private Timer timer;
	private int callTime;
	private AlertDialog alertDialog;
	private SurfaceView localVideoView;
	private SurfaceView remoteVideoView;
	private ViewGroup videoFrame;
	protected String Callnum = "11831726";
	private Intent intent;
	private String CallNumber;
	/**
	 * 如果重力感应变化角度过小，则不处理.
	 */
	private static final int ORIENTATION_SENSITIVITY = 45;

	private int lastOrientation = 270;

	private int lastDisplayRotation = Surface.ROTATION_0;

	private boolean hasStoped = false;
	private TextView callvideo_phoneName, callvideo_status, callvideo_time,
			callvideo_PhoneNumber;
	private Button callvideo_switch_audio, callvideo_callKeyboard,
			swtich_Camera, callvideo_mute, callvideo_cancel,
			callvideo_handsfree;
	private LinearLayout callvideo_title, callvideo_linear1;
	private Observable<Long> observable;
	private Subscriber<Long> subscriber;
	private Subscription subscribe;
	private AlphaAnimation alphaAnimation0To1;
	private AlphaAnimation alphaAnimation1To0;
	private boolean isOnTouch = false;
	private List<ContactSummary> searchContact;
	private TelephonyManager manager;
	private ContactsSqlite sqlite;
	private String CallNumbers;

	@Override
	public void OnCreate() {
		CallApi.setPauseMode(1);
		setContentView(R.layout.activity_callvideo);
		initCallSession();
		initViews();
		initCallName();
		OnListener();
		initAlphaAnimation1();
		initTimerOut();
		/* In order to properly display video, Camera rotate should be set. */
		setCameraRotate();
		/* creat local video view and remote video view. */
		createVideoView();
		if (callSession == null) {

		} else {
			callSession.showVideoWindow();
//			CaaSSdkService.setRemoteRenderPos(getFullScreenRect(),
//					CallApi.VIDEO_LAYER_BOTTOM);
			/* register broadcast receivers. */
			registerReceivers();
			startCallTimeTask();
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
						// WorkLog.e("CallVideo_Activity","当前名字"+position.getDisplayName());
						if (CallNumbers.substring(1).equals(
								position.getSearchMatchContent())) {
							callvideo_PhoneNumber.setText(position
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
						// WorkLog.e("CallVideo_Activity",
						// "CallOut_Activity本地数据查询成功");
						callvideo_PhoneNumber.setText(contact.get(i)
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
		callvideo_PhoneNumber.setText("未知号码");
		if (callSession != null) {
			if (CallNumber.length() >= 9) {
				if (CallNumber.startsWith(Config.CALL_BEFORE + "8")
						|| CallNumber.startsWith(Config.CALL_BEFORE + "9")) {
					CallNumbers = CallNumber.substring(8, CallNumber.length());
				} else {
					CallNumbers = CallNumber.substring(8, CallNumber.length());
				}
				// WorkLog.e("CallVideo_Activity", "截取号码" + CallNumbers);
				callvideo_PhoneNumber.setText(CallNumbers.substring(1));
				searchContact = ContactApi.searchContact(
						CallNumbers.substring(1), ContactApi.LIST_FILTER_ALL);
				handler.sendEmptyMessage(Config.SearchContactApi1);
			} else {
				selectSQL(CallNumber);
			}
		}
	}

	public void selectSQL(String callnumber) {
		WorkLog.e("CallVideo_Activity", "1:" + callnumber);
		callnumber = UiUtils.isMobileNO(callnumber) ? callnumber : callnumber
				.substring(1);
		WorkLog.e("CallVideo_Activity", "2:" + callnumber);
		contact = UiUtils.SearchContact(callnumber);
		handler.sendEmptyMessage(Config.SearchShareHomeContact2);
	}

	/**
	 * 得到全屏幕矩形
	 * @return
	 */
	private Rect getFullScreenRect() {
		Rect rect = new Rect();
		rect.left = 0;
		rect.top = 0;
		rect.right = 1280;
		rect.bottom = 720;
		return rect;
	}

	private void initViews() {
		callvideo_phoneName = (TextView) findViewById(R.id.callvideo_PhoneName);
		callvideo_PhoneNumber = (TextView) findViewById(R.id.callvideo_phoneNumber);
		// WorkLog.e("CallVideo_Activity", "视频通话:" +
		// callSession.getPeer().getNumber());

		callvideo_status = (TextView) findViewById(R.id.callvideo_status);
		callvideo_status.setText("通话中");
		callvideo_time = (TextView) findViewById(R.id.callvideo_time);

		callvideo_switch_audio = (Button) findViewById(R.id.callvideo_switch_audio);
		callvideo_callKeyboard = (Button) findViewById(R.id.callvideo_callKeyboard);
		callvideo_mute = (Button) findViewById(R.id.callvideo_mute);
		callvideo_cancel = (Button) findViewById(R.id.callvideo_cancel);
		callvideo_handsfree = (Button) findViewById(R.id.callvideo_handsfree);
		swtich_Camera = (Button) findViewById(R.id.swtich_video1);

		callvideo_title = (LinearLayout) findViewById(R.id.callvideo_title);
		callvideo_linear1 = (LinearLayout) findViewById(R.id.callvideo_linear1);

		videoFrame = (ViewGroup) findViewById(R.id.callvideo_frame);
		videoFrame.setOnTouchListener(this);
		SetOnClickListener(callvideo_switch_audio, callvideo_callKeyboard,
				callvideo_mute, callvideo_cancel, callvideo_handsfree,
				swtich_Camera);
		if (callSession != null) {
			CallNumber = callSession.getPeer().getNumber();
			String number;
			if (CallNumber.contains(Callnum) && CallNumber.length() > 8) {
				number = CallNumber.substring(8, CallNumber.length());
				callvideo_phoneName.setText(number.substring(1));
			} else {
				callvideo_phoneName.setText(CallNumber);
			}
		}
		/* only you can switch camera when device has more than one camera. */
    	if (CallApi.getCameraCount() < 2) {
    		swtich_Camera.setEnabled(false);
    	}
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callvideo_switch_audio:
			callSession.removeVideo();
			break;
		case R.id.callvideo_callKeyboard:

			break;
		case R.id.callvideo_mute:
			isMute = !isMute;

			if (isMute) {
				callSession.mute();
				callvideo_status.setText("静音");
				MuteDrawableTop(false);
			} else {
				callSession.unMute();
				callvideo_status.setText("通话中");
				MuteDrawableTop(true);
			}
			break;
		case R.id.callvideo_cancel:
			callSession.terminate();
			break;
		case R.id.callvideo_handsfree:
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
		case R.id.swtich_video1:
			if (CallApi.getCameraCount() < 2) {
				return;
			}
			CallApi.switchCamera();
			localVideoView.setVisibility(View.GONE);
			LogApi.d("V2OIP", "onClick_CameraSwitch displayRotation"
					+ lastDisplayRotation);
			int cameraRotate = getCameraOrientation(lastDisplayRotation);
			CallApi.setCameraRotate(cameraRotate);
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
			callvideo_mute.setCompoundDrawables(null, drawable, null, null);

		} else {
			drawable = getResources().getDrawable(R.drawable.call_mute2);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			callvideo_mute.setCompoundDrawables(null, drawable, null, null);

		}
	}

	public void HandsfreeDrawableTop(boolean b) {
		if (b) {
			drawable = getResources().getDrawable(R.drawable.call_handsfree);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			callvideo_handsfree
					.setCompoundDrawables(null, drawable, null, null);
			callvideo_handsfree.setText("听筒");
		} else {
			drawable = getResources().getDrawable(R.drawable.call_handsfree2);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			callvideo_handsfree
					.setCompoundDrawables(null, drawable, null, null);
			callvideo_handsfree.setText("免提");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		showVideo();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// IntentFilter intent = new IntentFilter();
		// intent.addAction(Const.CAMERA_PLUG);
		// registerReceiver(mCameraPlugReciver, intent);
	}

	// private BroadcastReceiver mCameraPlugReciver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// Rect rectLocal = new Rect();
	//
	// int iState = intent.getIntExtra("state", -1);
	// LogApi.d(Const.TAG_UI, "camera stat change:" + iState);
	//
	// Toast.makeText(getApplicationContext(), "mCameraPlugReciver", 0)
	// .show();
	//
	// if (1 == iState) {
	// Toast.makeText(getApplicationContext(), "打开摄像头", 0)
	// .show();
	// rectLocal.left = 0;
	// rectLocal.top = 0;
	// rectLocal.right = 320;
	// rectLocal.bottom = 180;
	// // CaaSSdkService.openCamera();
	// CaaSSdkService.setLocalRenderPos(rectLocal,
	// CallApi.VIDEO_LAYER_TOP);
	// CaaSSdkService.openLocalView();
	// // CaaSSdkService.showLocalVideoRender(true);
	// } else {
	// CaaSSdkService.closeLocalView();
	// }
	//
	// }
	// };

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if ((null != localVideoView || null != remoteVideoView)
				&& CallSession.INVALID_ID != callSession.getSessionId()) {
			callSession.hideVideoWindow();
			videoFrame.removeAllViews();
			hasStoped = true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (intent != null) {
			stopService(intent);
		}
		if (subscribe != null) {
			subscribe.unsubscribe();
		}
		alphaAnimation0To1 = null;
		alphaAnimation1To0 = null;
		unRegisterReceivers();
		stopCallTimeTask();
		destroyVideoView();
	}

	private void unRegisterReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(remoteVideoStreamArrivedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(remoteNetStatusChangeReciverr);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(cameraSwitchedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(cameraStartedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callQosReportReceiver);

	}

	private void registerReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						remoteVideoStreamArrivedReceiver,
						new IntentFilter(
								CallApi.EVENT_CALL_VIDEO_STREAM_ARRIVED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						remoteNetStatusChangeReciverr,
						new IntentFilter(
								CallApi.EVENT_CALL_VIDEO_NET_STATUS_CHANGE));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callTypeChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_TYPE_CHANGED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(cameraSwitchedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_CAMERA_SWITCHED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(cameraStartedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_CAMERA_STARTED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callQosReportReceiver,
						new IntentFilter(CallApi.EVENT_CALL_QOS_REPORT));
	}

	/* 显示来自远程的视频流。 */
	private BroadcastReceiver remoteNetStatusChangeReciverr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			Bundle bundle = intent.getExtras();
			int status = bundle.getInt(CallApi.PARAM_CALL_NET_STATUS);
			if (status == 1) {
				Rect rectLocal = new Rect();
				rectLocal.left = 0;
				rectLocal.top = 0;
				rectLocal.right = 320;
				rectLocal.bottom = 180;
				CaaSSdkService.setLocalRenderPos(rectLocal,
						CallApi.VIDEO_LAYER_TOP);
				CaaSSdkService.showRemoteVideoRender(true);
			}
		}
	};
	/* 如果通话状态为闲置状态，结束通话。 */
	private BroadcastReceiver callStatusChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (null != session
					&& session.getSessionId() != callSession.getSessionId()) {
				LogApi.d("V2OIP",
						"CallVideo_Activity callStatusChangedReceiver invalid callsession");
				WorkLog.d("CallVideo_Activity",
						"callStatusChangedReceiver invalid callsession");
				return;
			}
			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			LogApi.d(
					"V2OIP",
					"CallVideo_Activity callStatusChangedReceiver video share status changed newStatus:"
							+ newStatus);
			WorkLog.d("CallVideo_Activity",
					"callStatusChangedReceiver video share status changed newStatus:"
							+ newStatus);
			switch (newStatus) {
			case CallSession.STATUS_IDLE:
				String type = null;
				if (session.getType() == CallSession.TYPE_AUDIO) {
					type = Config.CALLRECORDER_TYPE_AUDIO_RECEIVED;
				} else if (session.getType() == CallSession.TYPE_VIDEO) {
					type = Config.CALLRECORDER_TYPE_VIDEO_RECEIVED;
				}
				// WorkLog.e("CallVideo_Activity", "通话记录视频"+CallNumber);
				UiUtils.InsertCallLog(UiUtils.initCallNumber2(CallNumber),
						type, callvideo_time.getText().toString());
				RxBus.getInstance().post(Config.Update_CallLog);
				finish();
				break;
			default:
				break;
			}
		}
	};

	/* 显示来自远程的视频流。 */
	private BroadcastReceiver remoteVideoStreamArrivedReceiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * CallSession session = (CallSession) intent
			 * .getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			 * 
			 * if (!callSession.equals(session)) {
			 * showToast("callSession!=session"); return; }
			 * 
			 * if (remoteVideoView == null) {
			 * 
			 * remoteVideoView = CallApi
			 * .createRemoteVideoView(getApplicationContext()); }
			 * remoteVideoView.setVisibility(View.VISIBLE);
			 */
			/*
			 * videoFrame .updateViewLayout(remoteVideoView,
			 * getRemoteViewMetrics());
			 * videoFrame.updateViewLayout(localVideoView,
			 * getLocalViewMetrics());
			 * videoFrame.bringChildToFront(localVideoView);
			 */

			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);

			if (null != session
					&& session.getSessionId() != callSession.getSessionId()) {
				LogApi.d("V2OIP",
						"ACT_DemoCallVideoSharing callStatusChangedReceiver invalid callsession");
				WorkLog.d("CallVideo_Activity",
						"callStatusChangedReceiver invalid callsession");
				return;
			}
			session.showVideoWindow();
			if (remoteVideoView == null) {

				remoteVideoView = CallApi
						.createRemoteVideoView(getApplicationContext());
			}
			if (remoteVideoView != null) {
				remoteVideoView.setVisibility(View.VISIBLE);
			}

			videoFrame
					.updateViewLayout(remoteVideoView, getRemoteViewMetrics());
			videoFrame.updateViewLayout(localVideoView, getLocalViewMetrics());
			videoFrame.bringChildToFront(localVideoView);
			new MyOnTouch(localVideoView, mcontext);
		}
	};
	/**
	 * 通话类型发生改变
	 * */
	private BroadcastReceiver callTypeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int newType = intent.getIntExtra(CallApi.PARAM_NEW_TYPE, -1);
			if (newType == CallSession.TYPE_AUDIO) {
				Toast.makeText(getApplicationContext(), "视频通话切换到音频电话",
						Toast.LENGTH_LONG).show();
				Intent newIntent = new Intent(mcontext,
						CallAudio_Activity.class);
				startActivity(newIntent);
				finish();
			}
		}
	};
	/**
	 * 摄像头启动
	 * */
	private BroadcastReceiver cameraStartedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			LogApi.d("V2OIP", "receive cameraStarted broadcast");
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}

			int cameraRotate = getCameraOrientation(lastDisplayRotation);
			CallApi.setCameraRotate(cameraRotate);

			localVideoView.setVisibility(View.VISIBLE);
			videoFrame.bringChildToFront(localVideoView);
		}
	};
	/**
	 * 摄像头切换开关
	 * */
	private BroadcastReceiver cameraSwitchedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			LogApi.d("V2OIP", "receive cameraSwitched broadcast");
			WorkLog.d("CallVideo_Activity", "receive cameraSwitched broadcast");
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			localVideoView.setVisibility(View.VISIBLE);
		}
	};
	/**
	 * 服务质量报告
	 * */
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

	private void OnListener() {
		OrientationEventListener listener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {

			@Override
			public void onOrientationChanged(int orientation) {
				if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
					orientationChanged(orientation);
				}
			}
		};
		if (listener.canDetectOrientation()) {
			listener.enable();
		} else {
			WorkLog.e("CallVideo", "定位事件监听器启用失败! !");
			LogApi.e("V2OIP", "OrientationEventListener enable failed!!");
		}
	}

	private void orientationChanged(int orientation) {
		int cameraRotate = 270;
		int displayRotation = Surface.ROTATION_0;
		if (Math.abs(orientation - lastOrientation) < ORIENTATION_SENSITIVITY
				|| Math.abs(orientation - lastOrientation - 360) < ORIENTATION_SENSITIVITY) {
			return;
		}

		lastOrientation = orientation;

		if (orientation < ORIENTATION_SENSITIVITY
				|| 360 - orientation < ORIENTATION_SENSITIVITY) {
			displayRotation = Surface.ROTATION_0;
		} else if (Math.abs(orientation - 90) <= ORIENTATION_SENSITIVITY) {
			displayRotation = Surface.ROTATION_90;
		} else if (Math.abs(orientation - 180) <= ORIENTATION_SENSITIVITY) {
			displayRotation = Surface.ROTATION_180;
		} else if (Math.abs(orientation - 270) <= ORIENTATION_SENSITIVITY) {
			displayRotation = Surface.ROTATION_270;
		} else {
			LogApi.e("V2OIP", "orientationChanged get wrong orientation:"
					+ orientation
					+ ", getCameraOrientation with default displayRotation "
					+ Surface.ROTATION_0);
			WorkLog.e(
					"CallVideo_Activity",
					"orientationChanged get wrong orientation:"
							+ orientation
							+ ", getCameraOrientation with default displayRotation "
							+ Surface.ROTATION_0);
			lastDisplayRotation = Surface.ROTATION_0;
		}

		if (lastDisplayRotation == displayRotation) {
			return;
		}

		lastDisplayRotation = displayRotation;
		cameraRotate = getCameraOrientation(lastDisplayRotation);
		CallApi.setCameraRotate(cameraRotate);
	}

	private void initCallSession() {
		intent = new Intent(mcontext, HomeService.class);
		intent.putExtra("className", mcontext.getClass().getName());
		startService(intent);
		callSession = CallApi.getForegroudCallSession();
		if (null == callSession) {
			LogApi.d("V2OIP", "视频onCreate:没有电话交谈.");
			finish();
			return;
		}
	}

	/**
	 * get orientation to set camera orientation in this situation
	 * 
	 * @param displayRotation
	 *            get the displayRotation by Gravity sensor(onOrientationChanged
	 *            event).
	 * */
	private int getCameraOrientation(int displayRotation) {

		boolean isFrontCamera = true;
		int cameraOrientation = 0;
		int degrees = 0;
		int result = 0;

		if (CallApi.getCameraCount() < 2) {
			isFrontCamera = false;
			LogApi.d(
					"V2OIP",
					"getCameraOrientation getCameraCount "
							+ CallApi.getCameraCount());
			WorkLog.d(
					"CallVideo_Activity",
					"getCameraOrientation getCameraCount "
							+ CallApi.getCameraCount());
		} else {
			if (CallApi.getCamera() == CallApi.CAMERA_TYPE_FRONT) {
				isFrontCamera = true;
			} else {
				isFrontCamera = false;
			}
		}

		cameraOrientation = CallApi.getCameraRotate();

		switch (displayRotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 270;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 90;
			break;
		default:
			LogApi.e("V2OIP", "getCameraOrientation wrong displayRotation "
					+ displayRotation);
			WorkLog.e("CallVideo_Activity",
					"getCameraOrientation wrong displayRotation "
							+ displayRotation);
			break;
		}

		CallApi.setVideoRenderRotate(degrees);

		if (isFrontCamera) {
			result = (cameraOrientation + degrees) % 360;
		} else {
			result = (cameraOrientation - degrees + 360) % 360;
		}

		return result;
	}

	private void setCameraRotate() {
		Display display = this.getWindowManager().getDefaultDisplay();
		int displayRotation = display.getRotation();
		int cameraRotate = getCameraOrientation(displayRotation);
		CallApi.setCameraRotate(cameraRotate);
	}

	private void createVideoView() {
		if (remoteVideoView == null) {
			remoteVideoView = CallApi
					.createRemoteVideoView(getApplicationContext());
			videoFrame.addView(remoteVideoView, getRemoteViewMetrics());
			remoteVideoView.setVisibility(View.GONE);
		}

		if (localVideoView == null) {
			localVideoView = CallApi
					.createLocalVideoView(getApplicationContext());

			videoFrame.addView(localVideoView, getRemoteViewMetrics());
			localVideoView.setVisibility(View.GONE);
		}
	}

	// 显示通话视频
	private void showVideo() {
		if (remoteVideoView == null) {
			// WorkLog.e("CallVideo_Activity", "remoteVideoView==null");
		}
		if ((null != localVideoView || null != remoteVideoView)
				&& CallSession.INVALID_ID != callSession.getSessionId()) {
			if (hasStoped) {
				callSession.showVideoWindow();
				videoFrame.addView(remoteVideoView, getRemoteViewMetrics());
				callSession.showVideoWindow();
				videoFrame.addView(localVideoView, getLocalViewMetrics());
				videoFrame.bringChildToFront(localVideoView);
			}
		} else {
			// WorkLog.e("CallVideo_Activity", "aaaa");
		}

	}

	private void destroyVideoView() {
		if (localVideoView != null) {
			localVideoView.setVisibility(View.GONE);
			videoFrame.removeView(localVideoView);
			CallApi.deleteLocalVideoView(localVideoView);
			localVideoView = null;
		}

		if (remoteVideoView != null) {
			remoteVideoView.setVisibility(View.GONE);
			videoFrame.removeView(remoteVideoView);
			CallApi.deleteRemoteVideoView(remoteVideoView);
			remoteVideoView = null;
		}

//		CaaSSdkService.setLocalRenderPos(getFullScreenRect(),
//				CallApi.VIDEO_LAYER_BOTTOM);// HmeVideoTV.VEDIO_RENDER_LAYER_BOTTOM
	}

	private RelativeLayout.LayoutParams getRemoteViewMetrics() {
		int[] metrics = new int[2];
		int rmtHeight = 0;
		getDisplayMetrics(this, metrics);
		RelativeLayout.LayoutParams rp;
		if (metrics[0] > metrics[1]) {
			/*
			 * image's pixel's proportion is 4:3, so you'd better make
			 * proportion to the screen of device is 4:3.
			 */
			rmtHeight = metrics[1] * 4 / 3;
			rp = new RelativeLayout.LayoutParams((int) metrics[1], rmtHeight);
		} else {
			rmtHeight = metrics[0] * 4 / 3;
			rp = new RelativeLayout.LayoutParams((int) metrics[0], rmtHeight);
		}
		rp.addRule(RelativeLayout.CENTER_VERTICAL);
		return rp;
	}

	private RelativeLayout.LayoutParams getLocalViewMetrics() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(180,
				240);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		return lp;
	}

	private void getDisplayMetrics(Context context, int metrics[]) {
		if (null == metrics) {
			metrics = new int[2];
		}

		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		if (screenHeight > screenWidth) {
			int temp = screenWidth;
			screenWidth = screenHeight;
			screenHeight = temp;
		}
		metrics[0] = screenWidth;
		metrics[1] = screenHeight;
	}

	private void stopCallTimeTask() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	/* this task will be started when user enters video talking. */
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
							callvideo_time.setText(PhoneUtils
									.getCallDurationTime(callTime));
						}
					});
				}
			}, 1000, 1000);
		}
	}

	public void initTimerOut() {
		observable = Observable.timer(8, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
		subscriber = new Subscriber<Long>() {

			@Override
			public void onCompleted() {
				callvideo_linear1.clearAnimation();
				callvideo_title.clearAnimation();
				callvideo_title.startAnimation(alphaAnimation1To0);
				callvideo_linear1.startAnimation(alphaAnimation1To0);
				isOnTouch = false;
				setCallButton(isOnTouch);
				this.unsubscribe();
			}

			@Override
			public void onNext(Long arg0) {
			}

			@Override
			public void onError(Throwable arg0) {
				this.unsubscribe();
			}
		};
		subscribe = observable.subscribe(subscriber);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.callvideo_frame:
			isOnTouch = !isOnTouch;
			if (subscribe != null) {
				// rl_call_type_layout.clearAnimation();
				Animation animation = callvideo_linear1.getAnimation();
				if (alphaAnimation1To0.equals(animation)) {
					callvideo_linear1.clearAnimation();
					callvideo_linear1.startAnimation(alphaAnimation0To1);
					callvideo_title.clearAnimation();
					callvideo_title.startAnimation(alphaAnimation0To1);
					setCallButton(isOnTouch);
				}
				subscribe.unsubscribe();
				subscribe = null;
				observable = null;
				subscriber = null;
				initTimerOut();
			}
			break;
		default:
			break;
		}
		return false;
	}

	private void initAlphaAnimation1() {
		alphaAnimation0To1 = new AlphaAnimation(0f, 1f);
		alphaAnimation0To1.setDuration(1000);
		alphaAnimation0To1.setFillAfter(true);
		alphaAnimation1To0 = new AlphaAnimation(1f, 0f);
		alphaAnimation1To0.setDuration(1000);
		alphaAnimation1To0.setFillAfter(true);
	}

	public void setCallButton(boolean b) {
		callvideo_switch_audio.setEnabled(b);
		callvideo_callKeyboard.setEnabled(b);
		callvideo_mute.setEnabled(b);
		callvideo_cancel.setEnabled(b);
		callvideo_handsfree.setEnabled(b);
		callvideo_switch_audio.setEnabled(b);
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
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
