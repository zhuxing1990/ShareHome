package com.vunke.sharehome.activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.vunke.sharehome.MainActivity;
import com.vunke.sharehome.R;
import com.vunke.sharehome.R.drawable;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.service.NetConnectService;
import com.vunke.sharehome.updata.AppTVStoreUpdateManager;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.CommonUtil;
import com.vunke.sharehome.utils.Encrypt3DES;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.SharedPreferencesUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.InputDataDialog;

/**
 * 登录
 * 
 * @author Administrator
 * 
 */

public class LoginActivity2 extends BaseActivity {
	private EditText login2_userName, login2_verificationCode;// 帐号输入框 验证码输入框
	private Button clear_userName, clear_verificationcode, login2_getcode,
			login2_commit;// 清除用户名 清除验证码 获取验证码 登录
	private int status1 = 0, status2 = 0;// 输入框的状态
	private SharedPreferences sp;
	private String username, code;// 帐号 验证码
	private ProgressDialog mypDialog;// 弹窗
	private String password;// 密码
	private String encodePass;//密钥
	private TextView app_title;
	/**
	 * 查询用户的配置数据
	 */
	private LoginCfg mLoginCfg = null;
	private UserInfo mLastUserInfo;// 华为SDK存储的用户信息
	public Handler smsHandler = new Handler() {
		// 这里可以进行回调的操作
	};
	private boolean isfirst = false;
	private TextView longin_versionName;
	private InputDataDialog inputDataDialog;
	/**
	 * App更新管理器
	 */
	private AppTVStoreUpdateManager updateManager;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_login2);
		init();
		initListener();
		registerReceiver();
		initData();
		UiUtils.UpdateAPK(mcontext);
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		login2_userName = (EditText) findViewById(R.id.login2_userName);
		login2_verificationCode = (EditText) findViewById(R.id.login2_verificationCode);
		clear_userName = (Button) findViewById(R.id.clear_userName);
		clear_verificationcode = (Button) findViewById(R.id.clear_verificationcode);
		login2_getcode = (Button) findViewById(R.id.login2_getcode);
		login2_commit = (Button) findViewById(R.id.login2_commit);
		app_title = (TextView) findViewById(R.id.app_title);
		longin_versionName = (TextView) findViewById(R.id.longin_versionName);
		longin_versionName.setText("版本号:" + UiUtils.getVersionName(mcontext));
		// longin_versionName.setText("型号:"+Build.MODEL+"\n版本信息"+Build.VERSION.RELEASE);
	}

	/**
	 * 初始化监听事件
	 */
	private void initListener() {
		SetOnClickListener(clear_userName, clear_verificationcode,
				login2_getcode, login2_commit, longin_versionName);
		login2_userName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				status1 = !TextUtils.isEmpty(s) ? 1 : 0;
				clear_userName.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE
						: View.GONE);
				login2_commit.setEnabled(status1 + status2 == 2 ? true : false);
				login2_commit
						.setBackgroundResource(status1 + status2 == 2 ? R.drawable.button_login_shape
								: R.drawable.button_login_shape2);
			}
		});
		login2_verificationCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				status2 = !TextUtils.isEmpty(s) ? 1 : 0;
				clear_verificationcode.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE
						: View.GONE);
				login2_commit.setEnabled(status1 + status2 == 2 ? true : false);
				login2_commit
						.setBackgroundResource(status1 + status2 == 2 ? R.drawable.button_login_shape
								: R.drawable.button_login_shape2);
			}
		});
		clear_userName.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				login2_userName.setText("");
				return false;
			}
		});
		clear_verificationcode
				.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						login2_verificationCode.setText("");
						return false;
					}
				});
//		app_title.setOnLongClickListener(new OnLongClickListener() {
//
//			@Override
//			public boolean onLongClick(View v) {
//				Config.intent = new Intent(mcontext, LoginActivity.class);
//				startActivity(Config.intent);
//				finish();
//				return false;
//			}
//		});
		smsObserver = new SmsObserver(this, smsHandler);
		getContentResolver().registerContentObserver(SMS_INBOX, true,
				smsObserver);
	}

	/**
	 * 注册广播
	 */
	private void registerReceiver() {
		// 登录状态的广播
		LocalBroadcastManager.getInstance(this).registerReceiver(
				LoginStatusChangedReceiver,
				new IntentFilter(LoginApi.EVENT_LOGIN_STATUS_CHANGED));
	}

	/**
	 * 初始化帐号信息
	 */
	private void initData() {
		sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
		password = sp.getString(Config.LOGIN_PASSWORD, "");
		if (!TextUtils.isEmpty(password)) {
			if (CommonUtil.isServiceRunning(mcontext, NetConnectService.class)) {
				startActivity(new Intent(mcontext, HomeActivity.class));
			} else {
				Config.intent = new Intent(mcontext, NetConnectService.class);
				mcontext.startService(Config.intent);
			}
			finish();
			return;
		}
		mLastUserInfo = LoginApi.getUserInfo(LoginApi.getLastUserName());
		if (mLastUserInfo != null) {
			if (TextUtils.isEmpty(UiUtils.GetUserName(mcontext))) {
				return;
			}
			login2_userName.setText(UiUtils.GetUserName(mcontext).substring(1));
			if (!TextUtils.isEmpty(mLastUserInfo.countryCode)) {// 自动写入帐号
				countryCode = mLastUserInfo.countryCode;
			}
			if (!TextUtils.isEmpty(mLastUserInfo.password)) {// 自动写入密码
//				login2_verificationCode.setText(mLastUserInfo.password);
			}
			// 查询用户的配置数据
			mLoginCfg = LoginApi.getLoginCfg(mLastUserInfo.username);
		} else {

		}
		if (!TextUtils.isEmpty(login2_userName.getText())
				&& !TextUtils.isEmpty(login2_verificationCode.getText())) {
			login2_commit.setBackgroundResource(drawable.button_login_shape);
			login2_commit.setEnabled(true);
		}
	}

	@Override
	public void OnClick(View v) {
		username = login2_userName.getText().toString().trim();
		code = login2_verificationCode.getText().toString().trim();
		switch (v.getId()) {
		case R.id.clear_userName:
			UiUtils.ClearNumber(login2_userName);
			break;
		case R.id.clear_verificationcode:
			UiUtils.ClearNumber(login2_verificationCode);
			break;
		case R.id.login2_getcode:
			getCode();
			break;
		case R.id.login2_commit:
			Login();
			break;
		case R.id.longin_versionName:
			Config.intent = new Intent(mcontext, VideoListActivity.class);
			startActivity(Config.intent);
			break;
		default:
			break;
		}

	}

	/**
	 * 获取短信验证码
	 */
	private void getCode() {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("当前网络未连接，请检查网络");
			return;
		}
		login2_verificationCode.requestFocus();
		String username = login2_userName.getText().toString().trim();
		if (UiUtils.isMobileNO(username)) {
			setButtonFalse();
			if (NetUtils.isNetConnected(mcontext)) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("mobile", username);
					getUrlRequest(UrlClient.HttpUrl + UrlClient.getCode,
							jsonObject.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				showToast("咦，好像网络出了点问题");
			}
		} else {
			showToast("请输入正确的手机号码");
		}

	}

	/**
	 * 定时器 60秒 获取短信验证码的定时器
	 */
	private void setButtonFalse() {
		login2_getcode.setClickable(false);
		Observable.interval(0, 1, TimeUnit.SECONDS)
				.filter(new Func1<Long, Boolean>() {
					@Override
					public Boolean call(Long aLong) {
						login2_getcode.setClickable(false);
						return aLong <= 60;
					}
				}).map(new Func1<Long, Long>() {
					@Override
					public Long call(Long aLong) {
						return -(aLong - 60);
					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<Long>() {
					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(Throwable e) {
						this.unsubscribe();
						login2_getcode.setClickable(true);
					}

					@Override
					public void onNext(Long aLong) {
						if (aLong != 0) {
							// WorkLog.i("LoginActivity2", aLong + "");
							login2_getcode.setText("请等待" + aLong + "秒");
						} else {
							this.unsubscribe();
							login2_getcode.setClickable(true);
							login2_getcode.setText("获取验证码");
						}
					}
				});
	}

	/**
	 * 获取短信验证码的请求
	 * 
	 * @param url
	 *            请求地址
	 * @param json
	 *            JSONObect.toString 的数据
	 */
	private void getUrlRequest(String url, String json) {
		 WorkLog.i("LoginActivity2", "获取验证码" + json);
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						WorkLog.i("LoginActivity2", "data:" + t);
						try {
							JSONObject jsonObject = new JSONObject(t);
							int Code = jsonObject.getInt("code");
							switch (Code) {
							case 200:
								showToast("您的信息已被接收，我们稍后以短信方式发送给您，请注意查收");
								break;
							case 400:
								showToast("咦，连接服务器失败");
								break;
							case 500:
								showToast("咦，连接服务器失败");
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
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						showToast("请求错误,网络发送异常");
						WorkLog.i("LoginActivity2", "获取短信验证码发生异常");
					}
				});

	}

	/**
	 * 登录
	 */
	private void Login() {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("当前网络未连接，请检查网络");
			return;
		}
		if (TextUtils.isEmpty(username)) {
			showToast("帐号不能为空");
			return;
		}
		if (TextUtils.isEmpty(code)) {
			showToast("验证码不能为空");
			return;
		}
		if (!UiUtils.isMobileNO(username)) {
			showToast("请输入正确的手机号码");
			return;
		}
		dialog1("连接服务器...");
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("username", username);
			jsonObject.put("smsCode", code);
			jsonObject.put("userType", "9");
			// IsSmsCode(UrlClient.HttpUrl + UrlClient.SmsLogin,
			// jsonObject.toString());
			IsSmsCode(UrlClient.HttpUrl + UrlClient.SmsLogin,
					jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 登录请求
	 * 
	 * @param url
	 * @param jsonObject
	 */
	private void IsSmsCode(String url, String jsonObject) {
		// WorkLog.i("LoginActivity2", "请求地址:" + url + "\n发送数据" + jsonObject);

		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", jsonObject)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						WorkLog.i("LoginActivity2", "data:" + t);
						try {
							JSONObject json = new JSONObject(t);
							int Code = json.getInt("code");
							String message = json.getString("message");
							switch (Code) {
							case 200:
								LoginSuccess(json);
								break;
							case 400:
								showToast("登录失败," + message);
								mypDialog.setMessage(message);
								ClearDialog();
								break;
							case 500:
								showToast("验证失败," + message);
								mypDialog.setMessage(message);
								ClearDialog();
								break;

							default:
								ClearDialog();
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
							ClearDialog();
						}
					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						showToast("请求错误,网络发送异常");
						WorkLog.i("LoginActivity2", "登录失败，发生异常");
						ClearDialog();
					}
				});
	}

	/**
	 * 获取密钥成功
	 * 
	 * @param json
	 * @throws JSONException
	 * @throws Exception
	 */
	private void LoginSuccess(JSONObject json) throws JSONException, Exception {
		encodePass = json.getString("encodePass");

		// WorkLog.i("LoginActivity2", "拿到密钥" +
		password = Encrypt3DES.getInstance().decrypt(encodePass);
		if (!TextUtils.isEmpty(password)) {
			// WorkLog.i("LoginActivity2", "解析密码" +
			// password);
			UserInfo userInfo = new UserInfo();
			if (countryCode.matches("([+]|[0-9])\\d{0,4}")) {
				userInfo.countryCode = countryCode;

			}
			userInfo.username = Config.CALL_BEFORE + Config.NINE + username;
			userInfo.password = password;
			LoginCfg loginCfg = new LoginCfg();
			loginCfg.isAutoLogin = true;
			loginCfg.isRememberPassword = true;
			loginCfg.isVerified = false;
			// LoginApi.login(userInfo, loginCfg);
			if (json.has("firstLogin")) {
				String firstLogin = json.getString("firstLogin");
				if (firstLogin.equals("1")) { // 推荐人 非首次登录 1
					// if (username.equals("13278875981")) {
					// showRefereeDialog(userInfo, loginCfg);
					// isfirst = true;
					// } else {
					isfirst = false;
					LoginApi.login(userInfo, loginCfg);
					// }
				} else if (firstLogin.equals("0")) { // 推荐人 首次登录 0
					isfirst = true;
					showRefereeDialog(userInfo, loginCfg);
				}
			} else {
				LoginApi.login(userInfo, loginCfg);
			}
			// encodePass);
		} else {
			// showToast("呜呜,找不到您的密码啦");
			mypDialog.setMessage("请重新登录");
			ClearDialog();
		}

	}

	/**
	 * 首次登录填写推荐人
	 * 
	 * @param userInfo
	 * @param loginCfg
	 */
	private void showRefereeDialog(final UserInfo userInfo,
			final LoginCfg loginCfg) {
		inputDataDialog = new InputDataDialog(mcontext);
		inputDataDialog.builder().setTitle("填写推荐人\n (无推荐人请直接点确定) ")
				.setHint(R.string.referee_hint).setCancelable(false)
				.setCanceledOnTouchOutside(false).setCancelVISIBLE(false)
				.setCommitAllow_null(true)
				.setDialogGravity(Gravity.LEFT | Gravity.CENTER)
				.SetCommitOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText editText = inputDataDialog.getEditText();
						String requestCode = editText.getText().toString()
								.trim();
						if (TextUtils.isEmpty(requestCode)) {
							LoginApi.login(userInfo, loginCfg);
							inputDataDialog.dismiss();
							return;
						}
						try {
							String getCalledName = UiUtils.GetUserName(mcontext);
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("username", username);
							if (getCalledName.startsWith("8")) {
								jsonObject.put("userType", "8");
							} else if (getCalledName.startsWith("9")) {
								jsonObject.put("userType", "9");
							}
							jsonObject.put("requestCode", requestCode);
							inputDataDialog.dismiss();
							getUrlRequest2(UrlClient.HttpUrl
									+ UrlClient.fillRequestCode,
									jsonObject.toString(), userInfo, loginCfg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).show();
	}

	private void getUrlRequest2(final String url, final String json,
			final UserInfo userInfo, final LoginCfg loginCfg) {
		WorkLog.i("LongActivity2", "请求推荐人" + json);
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						WorkLog.i("LoginActivity2", "data:" + t);
						try {
							JSONObject jsonObject = new JSONObject(t);
							int Code = jsonObject.getInt("code");
							switch (Code) {
							case 200:
								WorkLog.i("RefereeActivity", "成功");
								// finish();
								break;
							case 400:
								WorkLog.i("RefereeActivity", "400");
								break;
							case 500:
								showToast("连接服务器失败");
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
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						showToast("请求错误,网络发送异常");
					}

					@Override
					public void onAfter(boolean isFromCache,
							@Nullable String t, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						LoginApi.login(userInfo, loginCfg);
					}
				});
	}

	private Uri SMS_INBOX = Uri.parse("content://sms/");// 短信的uri
	private SmsObserver smsObserver;// 内容观察者

	/**
	 * 查询最近的一条短信
	 */
	public void getSmsFromPhone() {
		Cursor cursor = UiUtils
				.getContext()
				.getContentResolver()
				.query(Uri.parse("content://sms/inbox"), null, null, null,
						"date desc");
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				// String address = cursor.getString(cursor
				// .getColumnIndex("address"));
				String body = cursor.getString(cursor.getColumnIndex("body"));
				// WorkLog.i("LoginActivity2", address + ";" + body);
				// if (address.length() != 12) { // 判断发送验证码的号码，去除其它应用的验证码影响
				// return;
				// }
				// 正则表达式
				Pattern pattern = Pattern.compile("\\d{6}");// 表示连续的6个数字
				Matcher matcher = pattern.matcher(body);
				if (matcher.find()) {
					String code = matcher.group(0);// 获取匹配的数字
					login2_verificationCode.setText(code);
					// verification_code2 = code;
					// WorkLog.i("LoginActivity2","设置短信验证"+verification_code2);
				}
			}
			cursor.close();
		}
	}

	/**
	 * 内容观察者 获取短信验证码
	 */
	class SmsObserver extends ContentObserver {

		public SmsObserver(Context context, Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			// 每当有新短信到来时，使用我们获取短消息的方法
			getSmsFromPhone();
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
				
				sp.edit()
				.putString(Config.LOGIN_USER_NAME, username)
				.putString(Config.LOGIN_PASSWORD, encodePass).commit();
				if (isfirst == true) {
					GetLuckyMoney();
				}
				finish();
				break;
			case LoginApi.STATUS_CONNECTING:// 正在连接中
				if (reason == LoginApi.REASON_NET_UNAVAILABLE) {
					if (mypDialog != null) {
						mypDialog.setMessage("正在连接中。。。");
					}
				}

				WorkLog.i("LoginActivity", mapReasonStringtoReasonCode(reason)
						+ "");
				break;
			case LoginApi.STATUS_DISCONNECTED:// 断开连接
				WorkLog.i("LoginActivity", mapReasonStringtoReasonCode(reason)
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

		private void GetLuckyMoney() {
			try {

				OkHttpUtils
						.post(UrlClient.HttpUrl + UrlClient.ActivityInfo)
						.tag(this)
						.params("json",
								new JSONObject().put("activityId", "8")
										.toString())
						.execute(new StringCallback() {

							@Override
							public void onResponse(boolean isFromCache,
									String t, Request request,
									@Nullable Response response) {
								WorkLog.i("LoinActivity2", "获取数据" + t);
								try {
									JSONObject jsonobject = new JSONObject(t);
									if (jsonobject.has("code")) {
										int code = jsonobject.getInt("code");
										String message = jsonobject
												.getString("message");
										switch (code) {
										case 200:
											JSONObject getJsonUrl = new JSONObject(
													jsonobject
															.getString("locationUrl"));
											String activityName = getJsonUrl
													.getString("activityName");
											UrlClient.LuckyMoneyUrl = getJsonUrl
													.getString("getLuckyUrl");
											UrlClient.LuckyMoneyListUrl = getJsonUrl
													.getString("listLuckyUrl");
											smsHandler.postDelayed(
													new Runnable() {

														@Override
														public void run() {

															Config.intent = new Intent(
																	mcontext,
																	LuckyMoneyActivity.class);
															Config.intent
																	.putExtra(
																			"luckyResource",
																			"0");
															Config.intent
																	.putExtra(
																			"extParam1",
																			"1");
															startActivity(Config.intent);
														}
													}, 3000);
											break;
										case 400:

											break;
										case 500:

											break;

										default:
											break;
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onError(boolean isFromCache, Call call,
									Response response, Exception e) {
								WorkLog.i("LoinActivity2", "OnError");
							};
						});
			} catch (Exception e) {
				e.printStackTrace();
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
	 * 上传登录日志
	 * 
	 * @param username
	 *            用户名
	 * @param userType
	 *            用户类型
	 * @param appVesionName
	 *            版本名字
	 * @param appVesionCode
	 *            版本号
	 */
	public void LoginLog(String username, String userType,
			String appVesionName, String appVesionCode) {
		try {
			JSONObject json = new JSONObject();
			json.put("userName", username);
			json.put("userType", userType);
			json.put("appVesionName", appVesionName);
			json.put("appVesionCode", appVesionCode);
			json.put("appMotifyTime", Config.appMotifyTime);
			 WorkLog.i("LoginActivity2", "上传登录数据" + json.toString());
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
							try {
								WorkLog.i("LoginActivity2", "data:" + t);
								JSONObject jsonObject = new JSONObject(t);
								String message = jsonObject
										.getString("message");
								int code = jsonObject.getInt("code");
								switch (code) {
								case 200:
									WorkLog.i("LoginActivity2", "登录上传成功");
									break;
								case 400:
									WorkLog.i("LoginActivity2", "网络发送失败");
									break;
								case 500:
									WorkLog.i("LoginActivity2", "网络发送异常");
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
							WorkLog.i("LoginActivity2", "登录数据没有上传,网络发送异常");
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 弹窗
	 * 
	 * @param string
	 */
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

	/**
	 * 延迟销毁弹窗
	 */
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
		if (LoginStatusChangedReceiver != null) {
			LocalBroadcastManager.getInstance(getApplicationContext())
					.unregisterReceiver(LoginStatusChangedReceiver);
		}
		if (smsObserver != null) {
			getContentResolver().unregisterContentObserver(smsObserver);
		}
		if (mypDialog != null) {
			mypDialog.cancel();
		}
		OkHttpUtils.getInstance().cancelTag(this);
	}

}
