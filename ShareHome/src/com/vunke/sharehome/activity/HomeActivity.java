package com.vunke.sharehome.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.LoginCfg;
import com.lzy.okhttputils.OkHttpUtils;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.adapter.MyFragmentPagerAdapter;
import com.vunke.sharehome.base.BaseFragmentActivity;
import com.vunke.sharehome.crop.Crop;
import com.vunke.sharehome.fragment.AttnFragment;
import com.vunke.sharehome.fragment.ContactsFragment;
import com.vunke.sharehome.fragment.MoreFragment;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.model.Rx_ReLogin;
import com.vunke.sharehome.receiver.HomeKeyEventReceiver;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.MyJobService;
import com.vunke.sharehome.service.NetConnectService;
import com.vunke.sharehome.service.UpdateContactService;
import com.vunke.sharehome.updata.AppTVStoreUpdateManager;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.FileUtils;
import com.vunke.sharehome.utils.ImageUtils;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.SharedPreferencesUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 首页
 * 
 * @date 2016年2月
 */
public class HomeActivity extends BaseFragmentActivity {
	
	
	private ViewPager viewPager;

	/**
	 * Fragment集合
	 */
	private List<Fragment> fragmentlist;

	/**
	 * 联系人界面
	 */
	private ContactsFragment fragment1;

	/**
	 * 通话记录和拨号界面
	 */
	private AttnFragment fragment2;

	/**
	 * 设置界面
	 */
	private MoreFragment fragment3;

	/**
	 * viewpager设配器
	 */
	private MyFragmentPagerAdapter adapter;

	/**
	 * attn 联系人按钮 dial 通话记录和拨号按钮 more 设置按钮
	 */
	private Button attn, dial, more;

	/**
	 * 意图
	 */
	private Intent intent;

	private Drawable drawable;

	/**
	 * 弹窗
	 */
	private ProgressDialog mProgressDialog;

	/**
	 * 通过 mHandler 发送消息 注销登录
	 */
//	private Handler mHandler;

	/**
	 * 登录的前缀
	 */
	protected String Callnum = "11831726";

	/**
	 * 弹窗
	 */
	private AlertDialog alertDialog;

	/**
	 * Home键监听广播
	 */
//	private HomeKeyEventReceiver homeKeyEventReceiver;

	/**
	 * 接收消息开始注销
	 */
	private Runnable mLogoutRunnable = new Runnable() {

		@Override
		public void run() {
			finishSelf();

		}
	};
	/**
	 * App更新管理器
	 */
	private AppTVStoreUpdateManager updateManager;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Config.login_count = 1;
		setContentView(R.layout.activity_home);
		// int a[] ={1,2};
		// System.out.println(a[2]);
		initData();
		registerReceivers();
		init();
		initView();
		UiUtils.UpdateAPK(mcontext);
//			String getProvidersName = NetUtils.getProvidersName(mcontext);
//			WorkLog.e("HomeActivity", "当前SIM卡为:"+getProvidersName);
//		 FirstDialog();
	}

	/**
	 * 上传想家联系人，新的朋友，获取想家联系人 
	 */
	private void initData() {
		intent = getIntent();
		Config.intent = new Intent(mcontext, UpdateContactService.class);
		Config.intent.putExtra("param", Config.Upload_Contact);
		startService(Config.intent);
		// Config.intent = new Intent(mcontext, MyJobService.class);
		// startService(Config.intent);
		// UiUtils.StartJobScheduler(mcontext);
	}

	/**
	 * 发送广播
	 */
	private void registerReceivers() {
//		homeKeyEventReceiver = new HomeKeyEventReceiver();
//		registerReceiver(homeKeyEventReceiver, new IntentFilter(
//				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		registerReceiver(newfriendsBroadcast, new IntentFilter(
				Config.NEW_FRIENDS_STATUS_CHAGED));
		/*
		 * LocalBroadcastManager.getInstance(this).registerReceiver(
		 * newfriendsBroadcast, new
		 * IntentFilter(Config.NEW_FRIENDS_STATUS_CHAGED));
		 */
		registerReceiver(CallDailReceiver, new IntentFilter(Config.CALL_DAIL));
	}

	private void init() {
		attn = (Button) findViewById(R.id.home_attn);// 联系人按钮
		dial = (Button) findViewById(R.id.home_dial);// 通话记录和拨号按钮
		more = (Button) findViewById(R.id.home_more);// 设置按钮
		SetOnClickListener(attn, dial, more);// 设置点击事件
		mTempDir = new File(Environment.getExternalStorageDirectory(), "Temp");
		if (!mTempDir.exists()) {
			mTempDir.mkdirs();
		}
	}

	public void initView() {
		viewPager = (ViewPager) findViewById(R.id.home_viewpager);
		fragmentlist = new ArrayList<Fragment>();
		fragment1 = new ContactsFragment();
		fragment2 = new AttnFragment();
		fragment3 = new MoreFragment();
		fragmentlist.add(fragment1);
		fragmentlist.add(fragment2);
		fragmentlist.add(fragment3);
		adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),
				fragmentlist);
		viewPager.setAdapter(adapter);
		setdial();
		attn.setSelected(true);
		more.setSelected(false);
		viewPager.setOnPageChangeListener(this);
		viewPager.setOffscreenPageLimit(2);
	}

	@Override
	public void OnPagerSelected(int arg0) {
		switch (arg0) {
		case 0:
			viewPager.setCurrentItem(0);
			attn.setSelected(true);
			more.setSelected(false);
			// home_call.setVisibility(View.GONE);
			setdial();
			break;
		case 1:
			viewPager.setCurrentItem(1);
			attn.setSelected(false);
			more.setSelected(false);
			setdial2();
			if (fragment2 != null) {
				fragment2.getNumber();
				if (fragment2.getLayout() == false) {
					fragment2.setLayout(false);
				}
			}
			break;
		case 2:
			viewPager.setCurrentItem(2);
			more.setSelected(true);
			attn.setSelected(false);
			// home_call.setVisibility(View.GONE);
			setdial();
			break;

		default:
			break;
		}
	}

	public void setdial() {
		drawable = getResources().getDrawable(R.drawable.dial2);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		dial.setCompoundDrawables(null, drawable, null, null);
	}

	public void setdial2() {
		drawable = getResources().getDrawable(R.drawable.dial3);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		dial.setCompoundDrawables(null, drawable, null, null);
	}

	public void setdial3() {
		drawable = getResources().getDrawable(R.drawable.dial1);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		dial.setCompoundDrawables(null, drawable, null, null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_attn:
			viewPager.setCurrentItem(0);
			break;
		case R.id.home_dial:
			if (viewPager.getCurrentItem() == 1) {// 如果在当前页面
				if (fragment2.getLayout() == true) {// 如果没有隐藏
					// setlayout++;
					fragment2.setLayout(true);
					drawable = getResources().getDrawable(R.drawable.dial1);
				} else {
					// setlayout = 0;
					fragment2.setLayout(false);
					drawable = getResources().getDrawable(R.drawable.dial3);
				}
			} else {
				drawable = getResources().getDrawable(R.drawable.dial3);
			}
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			dial.setCompoundDrawables(null, drawable, null, null);
			viewPager.setCurrentItem(1);

			break;
		case R.id.home_more:
			viewPager.setCurrentItem(2);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mLoginStatusChangedReceiver,
				new IntentFilter(LoginApi.EVENT_LOGIN_STATUS_CHANGED));
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mLoginStatusChangedReceiver);
	}

	protected void finishSelf() {
//		stopTimer();
		LoginCfg loginCfg = new LoginCfg();
		loginCfg.isAutoLogin = false;
		LoginApi.setCurrentUserLoginCfg(loginCfg);
		SharedPreferences sp = getSharedPreferences(Config.SP_NAME,
				MODE_PRIVATE);
		sp.edit().putString(Config.LOGIN_PASSWORD, "").commit();
		sp = getSharedPreferences(Config.UserName, MODE_PRIVATE);
		sp.edit().putBoolean(Config.HasUserName, true).commit();
		DbCore.getDaoSession().getContactDao().deleteAll();
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (mProgressDialog != null) {
					mProgressDialog.cancel();
				}
				Intent it = new Intent(HomeActivity.this, LoginActivity2.class);
				startActivity(it);
				finish();
			}
		};
		timer.schedule(task, 1500);

	}

//	private void startTimer() {
//		if (null == mHandler) {
//			mHandler = new Handler();
//		}
//		mHandler.postDelayed(mLogoutRunnable, 16 * 1000);
//	}
//
//	private void stopTimer() {
//		if (null != mHandler) {
//			mHandler.removeCallbacks(mLogoutRunnable);
//		}
//	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		UiUtils.UpdateAPK(mcontext);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Config.login_count = 0;
		if (alertDialog != null) {
			alertDialog.cancel();
			alertDialog = null;
		}

		/* unregister broadcast receivers. */
		unRegisterReceivers();
	}
	
	private void unRegisterReceivers() {
//		unregisterReceiver(homeKeyEventReceiver);
//		homeKeyEventReceiver = null;
		unregisterReceiver(newfriendsBroadcast);
		newfriendsBroadcast = null;
		unregisterReceiver(CallDailReceiver);
		CallDailReceiver = null;
		/*
		 * LocalBroadcastManager.getInstance(this).unregisterReceiver(
		 * newfriendsBroadcast);
		 */
	}

	public void showAlderDialog(String string) {
		// View view = View.inflate(mcontext, R.layout.dialog_style1, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
		builder.setMessage(string);
		builder.setCancelable(false);
		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (alertDialog != null) {
					alertDialog.cancel();
					alertDialog = null;
				}
				LoginApi.logout();
				finishSelf();
			}
		});
		builder.setCancelable(false);
		alertDialog = builder.create();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();

	}

	public void showAlderDialog2(String string) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
		builder.setMessage(string);
		builder.setCancelable(false);
		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Config.intent = new Intent(mcontext, NetConnectService.class);
				mcontext.startService(Config.intent);
				if (alertDialog != null) {
					alertDialog.cancel();
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

	private int longTime = 0;
	private BroadcastReceiver mLoginStatusChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// int old_status = intent.getIntExtra(LoginApi.PARAM_OLD_STATUS,
			// -1);
			longTime++;

			WorkLog.e("HomeActivity", "检测登录状态次数:" + longTime);
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
	};

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
			showToast("登录失败，用户名或密码错误");
			showAlderDialog("登录失败，用户名或密码错误");
			break;
		case LoginApi.REASON_CONNCET_ERR:// 连接错误
			reasonStr = "connect error";
			showToast("连接错误");
			showAlderDialog("咦，好像网络出了点问题");
			RxBus.getInstance().post(reLogin);
			break;
		case LoginApi.REASON_NET_UNAVAILABLE:// 没有网络
			reasonStr = "no network";
			showToast("当前网络不可用，请检查网络设置");
			showAlderDialog2("当前网络不可用，请检查网络设置");
			RxBus.getInstance().post(reLogin);
			break;
		case LoginApi.REASON_NULL:// 空
//			showToast("未知的异常！");
			showAlderDialog("未知的异常！");
			reasonStr = "none";
			break;
		case LoginApi.REASON_SERVER_BUSY:// 服务器繁忙
			reasonStr = "server busy";
			showToast("服务器繁忙！");
			showAlderDialog2("服务器繁忙！");
			RxBus.getInstance().post(reLogin);
			break;
		case LoginApi.REASON_SRV_FORCE_LOGOUT:// 强行注销
			reasonStr = "force logout";
//			showToast("账号异地登录，被服务器强制下线");
			showAlderDialog("账号异地登录，被服务器强制下线");
			break;
		case LoginApi.REASON_USER_CANCEL:// 用户取消了
			reasonStr = "user canceled";
			showToast(" 用户取消了！");
			break;
		case LoginApi.REASON_WRONG_LOCAL_TIME:// 当地时间错了
			reasonStr = "wrong local time";
			showToast("当地时间错误");
			showAlderDialog("客户端时间错误");
			break;
		case LoginApi.REASON_ACCESSTOKEN_INVALID:// 无效的访问令牌
			reasonStr = "invalid access token";
			showToast("无效的访问令牌");
			showAlderDialog("无效的访问令牌");
			break;
		case LoginApi.REASON_ACCESSTOKEN_EXPIRED:// 访问令牌过期
			reasonStr = "access token expired";
			showToast(" 访问令牌过期");
			showAlderDialog("访问令牌过期");
			break;
		case LoginApi.REASON_APPKEY_INVALID:// 无效的application 密钥
			reasonStr = "invalid application key";
			showToast("无效的application 密钥");
			showAlderDialog("无效的application 密钥");
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

	/**
	 * 显示新的朋友个数的广播
	 */
	private BroadcastReceiver newfriendsBroadcast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Config.NEW_FRIENDS_STATUS_CHAGED)) {
				// WorkLog.e("HomeActivity", "收到广播了");
				if (intent.hasExtra("extras")) {
					// WorkLog.e("HomeActivity", "获取请求成功");
					int size = intent.getIntExtra("extras", 0);
					WorkLog.e("HomeActivity", "newfriendsBroadcast>>new friends:"
							+ size + " people");
					if (fragment1 != null
							&& fragment1.contacts_friendsSize != null) {
						if (size > 0) {
							fragment1.contacts_friendsSize.setText((size + "")
									.trim());
							fragment1.contacts_friendsSize
									.setVisibility(View.VISIBLE);
						} else if (size == 0) {
							fragment1.contacts_friendsSize
									.setVisibility(View.GONE);
						}
					}
				} else if (intent.hasExtra("extra")) {
					// WorkLog.e("HomeActivity", "获取请求失败");
					int code = intent.getIntExtra("extra", 0);
					WorkLog.e("HomeActivity", "newfriendsBroadcast>>code:"
							+ code);
				} else {
					WorkLog.e("HomeActivity", "newfriendsBroadcast>>没拿到数据");
				}
			}
		}
	};
	/**
	 * 通过广播设置按钮的背景
	 */
	private BroadcastReceiver CallDailReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Config.CALL_DAIL)) {
				if (intent.hasExtra("onTouch")) {
					boolean isOnTouch = intent
							.getBooleanExtra("onTouch", false);
					if (isOnTouch) {
						setdial3();
					} else {
						setdial2();
					}
				}
			}
		}
	};

	/**
	 * 第一次进入的弹窗警告
	 * */
	public void FirstDialog() {
		final SharedPreferences sp = getSharedPreferences(Config.HOME,
				MODE_PRIVATE);
		boolean ISFIRST = sp.getBoolean(Config.IS_FIRST_DIALOG, true);
		if (ISFIRST) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
			builder.setTitle("温馨提示");
			builder.setMessage("为了您更好的体验视频通话，建议设置如下:\n1.设置为受保护的程序\n“设置->受保护的后台应用->想家->打开”\n"
					+ "2.设置为信任此应用\n“设置->权限管理->应用->想家->信任此应用”\n3.设置始终保持WLAN连接\n“设置->WLAN->高级->在休眠状态下保持WLAN连接->始终”"
					+ "\n4.设置始终保持数据连接\n“设置->移动网络->始终连接数据业务”");
			builder.setPositiveButton("不在提示", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor editor = sp.edit();
					editor.putBoolean(Config.IS_FIRST_DIALOG, false);
					editor.commit();
					if (alertDialog != null) {
						alertDialog.cancel();
						alertDialog = null;
					}
				}
			});
			builder.setNegativeButton("去设置", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (android.os.Build.VERSION.SDK_INT > 10) {
						// intent = new Intent(
						// android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						intent = new Intent(
								android.provider.Settings.ACTION_SETTINGS);
					} else {
						intent = new Intent();
						ComponentName component = new ComponentName(
								"com.android.settings",
								"com.android.settings.WirelessSettings");
						intent.setComponent(component);
						intent.setAction("android.intent.action.VIEW");
					}
					startActivity(intent);
					if (alertDialog != null) {
						alertDialog.cancel();
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
	}

	private long exitTime = 0;

	/*
	 * 按2次退出，进入后台
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			// showToast("再按一次退出,退出后继续在后台运行");
			showToast("再按一次退出");
			exitTime = System.currentTimeMillis();
		} else {
			// this.finish();
			// System.exit(0);
			// 注册广播
			// homeKeyEventReceiver.AddNotification(mcontext);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
		}
	}

	private File mTempDir;
	private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1458;

	/*
	 * 通过framgent的拍照或选择相册获得图片再返回结果 将返回的结果中根据判断把图片进行编辑并把路径保存
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Crop.REQUEST_PICK) {
				beginCrop(result.getData());
			} else if (requestCode == Crop.REQUEST_CROP) {
				handleCrop(resultCode, result);
			} else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
				WorkLog.e("HomeActivity", " REQUEST_CODE_CAPTURE_CAMEIA "
						+ fragment3.mCurrentPhotoPath);
				if (fragment3.mCurrentPhotoPath != null) {
					beginCrop(Uri
							.fromFile(new File(fragment3.mCurrentPhotoPath)));
				}
			}
		}
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == Activity.RESULT_OK) {
			WorkLog.e("HomeActivity", " handleCrop: Crop.getOutput(result) "
					+ Crop.getOutput(result));
			Bitmap scaledBitmap = ImageUtils.getScaledBitmap(
					(Crop.getOutput(result)).getPath(), 200, 200);
			fragment3.more_icon.setImageBitmap(scaledBitmap);
			SharedPreferences sp = getSharedPreferences(Config.HOME,
					MODE_PRIVATE);
			sp.edit()
					.putString(Config.HasPhoto,
							Crop.getOutput(result).getPath()).commit();
			// fragment3.more_icon.setImageURI(Crop.getOutput(result));
			// fragment3.more_icon.setImageBitmap(getCircleBitmap(Crop
			// .getOutput(result)));
		} else if (resultCode == Crop.RESULT_ERROR) {
			showToast(Crop.getError(result).getMessage());
		}
	}

	private void beginCrop(Uri source) {
		boolean isCircleCrop = true;
		String fileName = "Temp_" + String.valueOf(System.currentTimeMillis());
		File cropFile = new File(mTempDir, fileName);
		Uri outputUri = Uri.fromFile(cropFile);
		new Crop(source).output(outputUri).setCropType(isCircleCrop)
				.start(mcontext);
	}
}
