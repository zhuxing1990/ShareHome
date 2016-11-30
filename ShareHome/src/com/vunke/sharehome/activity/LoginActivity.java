package com.vunke.sharehome.activity;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.LoginCfg;
import com.huawei.rcs.login.UserInfo;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.R.drawable;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.NetConnectService;
import com.vunke.sharehome.service.UpdateContactService;
import com.vunke.sharehome.updata.AppTVStoreUpdateManager;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.CommonUtil;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.SharedPreferencesUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 登录 已弃用
 * 
 */
public class LoginActivity extends BaseActivity {
	private Button login, UserNameClear, PassWordClear, Register,
			ForgetPassword;
	private EditText UserName, PassWord;
	private int status1 = 0, status2 = 0;
	private String username, password;
	private ProgressDialog mypDialog;// 弹窗
	public static final String SP_NAME = Config.SP_NAME;
	public static final String LOGIN_PASSWORD = Config.LOGIN_PASSWORD;
	private boolean bIsAutoLogin = false;
	private SharedPreferences sp;
	private String login_password;
	protected String Callnum = "11831726";
	// 电话区号 中国地区为 +86
	private String key = "l7xUXj0ipu4wTE1BOKdCVQg6tCoa";// 登录密钥

	/**
	 * 查询用户的配置数据
	 */
	private LoginCfg mLoginCfg = null;
	private UserInfo mLastUserInfo;// 华为SDK存储的用户信息
	private TextView AppTitle;
	int i = 0;
	private Subscription subscription;
	/**
	 * App更新管理器
	 */
	private AppTVStoreUpdateManager updateManager;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_login);
		init();
		// 登录状态的广播
		LocalBroadcastManager.getInstance(this).registerReceiver(
				LoginStatusChangedReceiver,
				new IntentFilter(LoginApi.EVENT_LOGIN_STATUS_CHANGED));
		getData();
		initData();
		// AutoLogin();
		RXAutoLogin();
		UiUtils.UpdateAPK(mcontext);
	}

	public void RXAutoLogin() {
		subscription = RxBus.getInstance().toObservable(Integer.class)
				.filter(new Func1<Integer, Boolean>() {
					@Override
					public Boolean call(Integer arg0) {
						return arg0 == Config.Update_Login;
					}
				}).subscribe(new Subscriber<Integer>() {

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable throwable) {
						this.unsubscribe();
					}

					@Override
					public void onNext(Integer integer) {
						if (integer == Config.Update_Login) {
							SharedPreferences sp = getSharedPreferences(
									Config.SP_NAME, MODE_PRIVATE);
							UserName.setText(9 + sp.getString(
									Config.LOGIN_USER_NAME, ""));
							PassWord.setText(sp.getString(
									Config.LOGIN_PASSWORD, ""));
						}
					}

				});

	}

	public void getData() {
		mLastUserInfo = LoginApi.getUserInfo(LoginApi.getLastUserName());
	}

	public void initData() {
		if (mLastUserInfo != null) {
			if (!TextUtils.isEmpty(mLastUserInfo.countryCode)) {// 自动写入帐号
				countryCode = mLastUserInfo.countryCode;
				initEditData();
			}
			if (!TextUtils.isEmpty(mLastUserInfo.password)) {// 自动写入密码
				PassWord.setText(mLastUserInfo.password);
			}
			// 查询用户的配置数据
			mLoginCfg = LoginApi.getLoginCfg(mLastUserInfo.username);
		} else {

		}
		if (UserName.getText().length() != 0
				&& PassWord.getText().length() != 0) {
			login.setBackgroundResource(drawable.button_login_shape);
			login.setEnabled(true);
		}
	}

	/**
	 * 自动登录 可能不需要
	 * */
	/*
	 * private void AutoLogin() { if (null == mLoginCfg) {
	 * 
	 * return; } if (bIsAutoLogin && mLoginCfg.isVerified) {
	 * CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_USER_NAME,
	 * mLastUserInfo.username);
	 * CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_PASSWORD,
	 * mLastUserInfo.password); login(mLastUserInfo, mLoginCfg); } else {
	 * 
	 * } }
	 */

	/**
	 * 初始化控件
	 * */
	public void init() {
		login = (Button) findViewById(R.id.login_user);
		login.setBackgroundResource(drawable.button_login_shape2);
		login.setEnabled(false);
		UserNameClear = (Button) findViewById(R.id.login_UserNameClear);
		PassWordClear = (Button) findViewById(R.id.login_PassWordClear);
		Register = (Button) findViewById(R.id.login_Register);
		ForgetPassword = (Button) findViewById(R.id.login_ForgetPassword);
		SetOnClickListener(login, UserNameClear, PassWordClear, Register,
				ForgetPassword);

		UserName = (EditText) findViewById(R.id.login_UserName);
		PassWord = (EditText) findViewById(R.id.login_Password);
		sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
		login_password = sp.getString(LOGIN_PASSWORD, "");
		// ---设置密码来自mSp
		if (!TextUtils.isEmpty(login_password)) {
			PassWord.setText(login_password);
			PassWordClear.setVisibility(View.VISIBLE);
			login.requestFocus();
		}
		if (!TextUtils.isEmpty(login_password)) {
			UpdateContactService();
			if (CommonUtil.isServiceRunning(mcontext, NetConnectService.class)) {
				startActivity(new Intent(mcontext, HomeActivity.class));
			} else {
				Config.intent = new Intent(mcontext, NetConnectService.class);
				mcontext.startService(Config.intent);
			}
			finish();
			return;
		}

		UserName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数

			}

			@Override
			public void afterTextChanged(Editable s) {
				// s:变化后的所有字符
				if (s.length() == 0) {
					UserNameClear.setVisibility(View.GONE);
					status1 = 0;
				} else if (s.length() > 0) {
					UserNameClear.setVisibility(View.VISIBLE);
					status1 = 1;
				}
				if (status1 + status2 == 2) {
					login.setBackgroundResource(drawable.button_login_shape);
					login.setEnabled(true);
				} else {
					login.setBackgroundResource(drawable.button_login_shape2);
					login.setEnabled(false);
				}

			}
		});
		PassWord.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数

			}

			@Override
			public void afterTextChanged(Editable s) {
				// s:变化后的所有字符
				if (s.length() == 0) {
					PassWordClear.setVisibility(View.GONE);
					status2 = 0;
				} else if (s.length() > 0) {
					PassWordClear.setVisibility(View.VISIBLE);
					status2 = 1;
				}
				if (status1 + status2 == 2) {
					login.setBackgroundResource(drawable.button_login_shape);
					login.setEnabled(true);
				} else {
					login.setBackgroundResource(drawable.button_login_shape2);
					login.setEnabled(false);
				}
			}
		});
		// 跳转到隐藏的类。设置IP和端口的
		AppTitle = (TextView) findViewById(R.id.appTitle);
		AppTitle.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				intent = new Intent(mcontext, HideActivity.class);
				startActivity(intent);
				return false;
			}
		});
		UserNameClear.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				UserName.setText("");
				return false;
			}
		});
		PassWordClear.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				PassWord.setText("");
				return false;
			}
		});

	}

	/**
	 * 点击监听事件
	 * */
	@Override
	public void OnClick(View v) {
		username = UserName.getText().toString().trim();
		password = PassWord.getText().toString().trim();
		switch (v.getId()) {
		case R.id.login_user:
			// if (isAccountStandard(username) == true) {
			// if (isPasswordStandard(password) == true) {
			Login();
			// } else if (isPasswordStandard(password) == false) {
			// showToast("密码格式不对");
			// }
			// } else if (isAccountStandard(username) == false) {
			// showToast("帐号格式不对");
			// }
			break;
		case R.id.login_UserNameClear:
			// UserName.setText("");
			UiUtils.ClearNumber(UserName);
			break;
		case R.id.login_PassWordClear:
			// PassWord.setText("");
			UiUtils.ClearNumber(PassWord);
			break;
		case R.id.login_Register:
			startActivity(new Intent(mcontext, RegisterActivity.class));
			break;
		case R.id.login_ForgetPassword:
			startActivity(new Intent(mcontext, ForgetPasswordActivity.class));
			break;

		default:
			break;
		}
	}

	/**
	 * 登录广播
	 * */
	private BroadcastReceiver LoginStatusChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int old_status = intent.getIntExtra(LoginApi.PARAM_OLD_STATUS, -1);
			int new_status = intent.getIntExtra(LoginApi.PARAM_NEW_STATUS, -1);
			int reason = intent.getIntExtra(LoginApi.PARAM_REASON, -1);
			Log.d("tag", "the status is " + new_status);
			switch (new_status) {
			case LoginApi.STATUS_CONNECTED:// 连接成功
				if (mypDialog != null) {
					mypDialog.setMessage("登录成功");
					UiUtils.setLookHome(mcontext);
					LoginLog(username, Config.NINE,
							UiUtils.getVersionName(mcontext),
							UiUtils.getVersionCode(mcontext) + "");
					ClearDialog();
				}
				intent = new Intent(mcontext, HomeActivity.class);
				// intent.putExtra("is_video_call", false);
				if (Config.login_count == 0) {
					startActivity(intent);
				}
				sp.edit().putString(Config.LOGIN_PASSWORD, password).commit();
				finish();

				break;
			case LoginApi.STATUS_CONNECTING:// 正在连接中
				if (reason == LoginApi.REASON_NET_UNAVAILABLE) {
					if (mypDialog != null) {
						mypDialog.setMessage("正在连接中。。。");
					}
				}

				WorkLog.e("LoginActivity", mapReasonStringtoReasonCode(reason)
						+ "");
				break;
			case LoginApi.STATUS_DISCONNECTED:// 断开连接
				WorkLog.e("LoginActivity", mapReasonStringtoReasonCode(reason)
						+ "");
				if (mypDialog != null) {
					mypDialog.setMessage("登录失败");
					ClearDialog();
				}
				break;
			case LoginApi.STATUS_DISCONNECTING:// 断开连接中
				if (mypDialog != null) {
					mypDialog.setMessage("断开连接中...");
				}
				break;
			case LoginApi.STATUS_IDLE:// 闲置
				if (mypDialog != null) {
					mypDialog.setMessage("lading...");
				}
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 登录失败的原因
	 * */
	private String mapReasonStringtoReasonCode(int reason) {

		String reasonStr = null;
		switch (reason) {
		case LoginApi.REASON_AUTH_FAILED:// 鉴权失败，用户名或密码错误
			reasonStr = "auth failed";
			showToast("登录失败，用户名或密码错误");
			break;
		case LoginApi.REASON_CONNCET_ERR:// 连接错误
			reasonStr = "connect error";
			showToast("连接错误");
			break;
		case LoginApi.REASON_NET_UNAVAILABLE:// 没有网络
			reasonStr = "no network";
			showToast("当前网络不可用");
			break;
		case LoginApi.REASON_NULL:// 空
			reasonStr = "none";
			break;
		case LoginApi.REASON_SERVER_BUSY:// 服务器繁忙
			reasonStr = "server busy";
			showToast("服务器繁忙，请稍后再试！！");
			break;
		case LoginApi.REASON_SRV_FORCE_LOGOUT:// 强行注销
			reasonStr = "force logout";
			showToast("账号异地登录，被服务器强制下线");
			break;
		case LoginApi.REASON_USER_CANCEL:// 用户取消了
			reasonStr = "user canceled";
			break;
		case LoginApi.REASON_WRONG_LOCAL_TIME:// 当地时间错了
			reasonStr = "wrong local time";
			break;
		case LoginApi.REASON_ACCESSTOKEN_INVALID:// 无效的访问令牌
			reasonStr = "invalid access token";
			break;
		case LoginApi.REASON_ACCESSTOKEN_EXPIRED:// 访问令牌过期
			reasonStr = "access token expired";
			break;
		case LoginApi.REASON_APPKEY_INVALID:// 无效的application 密钥
			reasonStr = "invalid application key";
			break;
		case LoginApi.REASON_UNKNOWN:// 未知的
		default:
			reasonStr = "unknown";
			break;
		}
		return reasonStr;
	}

	/**
	 * 为帐号输入框写入帐号
	 */
	private void initEditData() {

		if ((null != mLastUserInfo) && (null != mLastUserInfo.countryCode)
				&& (null != mLastUserInfo.username)) {

			if (mLastUserInfo.username.startsWith("+")) {
				int length = mLastUserInfo.countryCode.length();
				if (!mLastUserInfo.countryCode.startsWith("+")) {
					length++;
				}
				String userName = mLastUserInfo.username.substring(length);
				if (userName.contains(Callnum)) {
					userName = userName.substring(8, userName.length());
				}

				UserName.setText(userName);
			} else {
				if (mLastUserInfo.username.contains(Callnum)) {
					mLastUserInfo.username = mLastUserInfo.username.substring(
							8, mLastUserInfo.username.length());
				}
				UserName.setText(mLastUserInfo.username);

			}
		}

	}

	private void Login() {
		UserInfo userInfo = new UserInfo();
		if (countryCode.matches("([+]|[0-9])\\d{0,4}")) {// 保存国家区号
			userInfo.countryCode = countryCode;
		}
		boolean startsWith = username.startsWith("9", 0);
		if (!startsWith) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("请输入9加手机号码");
			builder.setNegativeButton("确定", null);
			builder.create().show();
			return;
		}
		userInfo.username = countryCode + Callnum + username;
		// userInfo.username = countryCode+Callnum+nine+username;
		userInfo.password = password;
		/*
		 * userInfo.username = "11831726913319551978"; userInfo.password =
		 * "A121212";
		 */
		/*
		 * CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_USER_NAME,userInfo.countryCode
		 * + userInfo.username); Log.e(TAG, "" + CaasOmpCfg.EN_OMP_CFG_USER_NAME
		 * + userInfo.countryCode+ userInfo.username);
		 * 
		 * CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_PASSWORD,
		 * userInfo.password); Log.e(TAG, "" + CaasOmpCfg.EN_OMP_CFG_PASSWORD +
		 * userInfo.password);
		 */
		// Log.e(TAG, "username:" + userInfo.username + "password:"
		// + userInfo.password);

		LoginCfg loginCfg = new LoginCfg();
		loginCfg.isRememberPassword = true;
		loginCfg.isAutoLogin = true;
		loginCfg.isVerified = false;

		login(userInfo, loginCfg);
		dialog1("连接服务器。。。");
	}

	private void login(UserInfo userInfo, LoginCfg loginCfg) {

		/*
		 * CaasOmp.addPlugin(CaasOmp.OMP_DFT_PRIORITY);
		 * CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_APP_KEY, key);
		 */
		LoginApi.login(userInfo, loginCfg);

	}

	public void LoginLog(String username, String userType,
			String appVesionName, String appVesionCode) {
		try {
			JSONObject json = new JSONObject();
			json.put("userName", username);
			json.put("userType", userType);
			json.put("appVesionName", appVesionName);
			json.put("appVesionCode", appVesionCode);
			json.put("appMotifyTime", Config.appMotifyTime);
			// WorkLog.e("LoginActivity", "发送数据" + json.toString());
			if (!NetUtils.isNetConnected(mcontext)) {
				showToast("咦，貌似网络出了点问题");
				return;
			}
			OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.LoginLog)
					.params("json", json.toString())
					.execute(new StringCallback() {

						@Override
						public void onResponse(boolean isFromCache, String t,
								Request request, @Nullable Response response) {
							WorkLog.e("LoginActivity", "data:" + t);
							try {
								JSONObject jsonObject = new JSONObject(t);
								String message = jsonObject
										.getString("message");
								int code = jsonObject.getInt("code");
								switch (code) {
								case 200:
									WorkLog.e("LoginActivity", "登录上传成功");
									break;
								case 400:
									WorkLog.e("LoginActivity", "网络发送失败");
									break;
								case 500:
									WorkLog.e("LoginActivity", "网络发送异常");
									break;

								default:
									break;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onError(boolean isFromCache, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onError(isFromCache, call, response, e);
							WorkLog.e("LoginActivity", "登录数据没有上传,网络发送异常");
						}

						@Override
						public void onAfter(boolean isFromCache,
								@Nullable String t, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onAfter(isFromCache, t, call, response, e);
							UpdateContactService();
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void UpdateContactService() {
		Config.intent = new Intent(mcontext, UpdateContactService.class);
		Config.intent.putExtra("param", Config.Upload_Contact);
		startService(Config.intent);
	}

	private long exitTime = 0;

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
			showToast("再按一次退出程序");
			exitTime = System.currentTimeMillis();
		} else {
			this.finish();
			System.exit(0);
		}
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		OkHttpUtils.getInstance().cancelTag(this);
		if (LoginStatusChangedReceiver != null) {
			LocalBroadcastManager.getInstance(getApplicationContext())
					.unregisterReceiver(LoginStatusChangedReceiver);
		}
		if (mypDialog != null) {
			mypDialog.cancel();
		}
		if (!subscription.isUnsubscribed()) {
			subscription.unsubscribe();

		}
	}

	public void dialog1(String string) {
		mypDialog = new ProgressDialog(this);
		// 实例化
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置进度条风格，风格为圆形，旋转的
		// mypDialog.setTitle("");
		// 设置ProgressDialog 标题
		mypDialog.setMessage(string);
		// 设置ProgressDialog 提示信息
		mypDialog.setIcon(R.drawable.clear);
		// 设置ProgressDialog 标题图标

		mypDialog.setIndeterminate(false);
		// 设置ProgressDialog 的进度条是否不明确
		mypDialog.setCancelable(false);
		mypDialog.setCanceledOnTouchOutside(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		mypDialog.show();
		// 让ProgressDialog显示
	}

	public void ClearDialog() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (mypDialog != null) {
					mypDialog.cancel();
				}
			}
		};

		timer.schedule(task, 1500);
	}

}
