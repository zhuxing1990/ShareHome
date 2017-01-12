package com.vunke.sharehome.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.R.drawable;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 注册  (第二期弃用) 
 * 
 */
public class RegisterActivity extends BaseActivity {
	private EditText userName, passWord, verificationCode, confirmPassword,
			invitationCode;
	private Button getcode, commit, userName_Clear, verificationcode_Clear,
			passWord_Clear, confirmPassword_Clear, InvitationCode_Clear;
	private String username, password, verification_code, confirm_password,
			invitation_code;
	private ImageButton activity_back;
	private boolean smsCode_Istrue = false;
	public Handler smsHandler = new Handler() {
		// 这里可以进行回调的操作

	};
	private Map<String, Object> params;
	private AlertDialog alertDialog;
	private PopupWindow popupWindow;
	/**
	 * 每一个status 对应每一个输入框的状态
	 * */
	private int status1 = 0, status2 = 0, status3 = 0, status4 = 0;

	// private String username_code,verification_code2;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_register);
		init();
		initEditText();
		smsObserver = new SmsObserver(this, smsHandler);
		getContentResolver().registerContentObserver(SMS_INBOX, true,
				smsObserver);
	}

	/**
	 * 初始化
	 * */
	public void init() {
		userName = (EditText) findViewById(R.id.register_userName);// 帐号
		passWord = (EditText) findViewById(R.id.register_passWord);// 密码
		verificationCode = (EditText) findViewById(R.id.register_verificationCode);// 验证码
		confirmPassword = (EditText) findViewById(R.id.register_confirmPassword);// 重复密码
		invitationCode = (EditText) findViewById(R.id.register_invitationCode);// 邀请码

		getcode = (Button) findViewById(R.id.register_getcode);// 获取手机验证码
		commit = (Button) findViewById(R.id.register_commit);// 提交
		userName_Clear = (Button) findViewById(R.id.userName_Clear);// 清除帐号
		verificationcode_Clear = (Button) findViewById(R.id.verificationcode_Clear);// 清除验证码
		passWord_Clear = (Button) findViewById(R.id.passWord_Clear);// 清除密码
		confirmPassword_Clear = (Button) findViewById(R.id.confirmPassword_Clear);// 清除确认密码
		InvitationCode_Clear = (Button) findViewById(R.id.InvitationCode_Clear);// 清除邀请码
		activity_back = (ImageButton) findViewById(R.id.activity_back);// 返回

		SetOnClickListener(getcode, commit, userName_Clear,
				verificationcode_Clear, passWord_Clear, confirmPassword_Clear,
				InvitationCode_Clear, activity_back);

	}

	/**
	 * 初始化输入框
	 * */
	public void initEditText() {
		userName.addTextChangedListener(new TextWatcher() {

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
				if (s.length() == 0) {
					status1 = 0;
					userName_Clear.setVisibility(View.GONE);
				} else if (s.length() > 0) {
					status1 = 1;
					userName_Clear.setVisibility(View.VISIBLE);
				}
				if (status1 + status2 + status3 + status4 == 4) {
					commit.setBackgroundResource(drawable.button_login_shape);
					commit.setEnabled(true);
				} else {
					commit.setBackgroundResource(drawable.button_login_shape2);
					commit.setEnabled(false);
				}
			}
		});
		verificationCode.addTextChangedListener(new TextWatcher() {

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
				if (s.length() == 0) {
					status2 = 0;
					verificationcode_Clear.setVisibility(View.GONE);
				} else if (s.length() > 0) {
					status2 = 1;
					verificationcode_Clear.setVisibility(View.VISIBLE);
				}
				if (status1 + status2 + status3 + status4 == 4) {
					commit.setBackgroundResource(drawable.button_login_shape);
					commit.setEnabled(true);
				} else {
					commit.setBackgroundResource(drawable.button_login_shape2);
					commit.setEnabled(false);
				}
			}
		});
		passWord.addTextChangedListener(new TextWatcher() {

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
				if (s.length() == 0) {
					status3 = 0;
					passWord_Clear.setVisibility(View.GONE);
				} else if (s.length() > 0) {
					status3 = 1;
					passWord_Clear.setVisibility(View.VISIBLE);
				}
				if (status1 + status2 + status3 + status4 == 4) {
					commit.setBackgroundResource(drawable.button_login_shape);
					commit.setEnabled(true);
				} else {
					commit.setBackgroundResource(drawable.button_login_shape2);
					commit.setEnabled(false);
				}
			}
		});
		confirmPassword.addTextChangedListener(new TextWatcher() {

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
				if (s.length() == 0) {
					status4 = 0;
					confirmPassword_Clear.setVisibility(View.GONE);
				} else if (s.length() > 0) {
					status4 = 1;
					confirmPassword_Clear.setVisibility(View.VISIBLE);
				}
				if (status1 + status2 + status3 + status4 == 4) {
					commit.setBackgroundResource(drawable.button_login_shape);
					commit.setEnabled(true);
				} else {
					commit.setBackgroundResource(drawable.button_login_shape2);
					commit.setEnabled(false);
				}
			}
		});
		invitationCode.addTextChangedListener(new TextWatcher() {

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
				if (s.length() == 0) {
					InvitationCode_Clear.setVisibility(View.GONE);
				} else if (s.length() > 0) {
					InvitationCode_Clear.setVisibility(View.VISIBLE);
				}
				if (status1 + status2 + status3 + status4 == 4) {
					commit.setBackgroundResource(drawable.button_login_shape);
					commit.setEnabled(true);
				} else {
					commit.setBackgroundResource(drawable.button_login_shape2);
					commit.setEnabled(false);
				}
			}
		});

	}

	@Override
	public void OnClick(View v) {
		username = userName.getText().toString().trim();
		password = passWord.getText().toString().trim();
		verification_code = verificationCode.getText().toString().trim();
		confirm_password = confirmPassword.getText().toString().trim();
		invitation_code = invitationCode.getText().toString().trim();
		switch (v.getId()) {
		case R.id.register_getcode:// 获取验证码
			if (UiUtils.isMobileNO(username)) {
				setButtonFalse();
				if (NetUtils.isNetConnected(mcontext)) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("mobile", username);
					String jsonObject = NetUtils.Map_toJSONObject(map);
					getUrlRequest(UrlClient.HttpUrl + UrlClient.getCode,
							jsonObject);
				} else {
					showToast("咦，好像网络出了点问题");
				}
			} else {
				showToast("请输入正确的手机号码");
			}
			break;
		case R.id.register_commit:// 提交
			showPopupWindow();
			smsHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					commit();
				}
			}, 1500);

			break;
		case R.id.userName_Clear:// 清除帐号
			UiUtils.ClearNumber(userName);
			break;
		case R.id.verificationcode_Clear:// 清除验证码
			UiUtils.ClearNumber(verificationCode);
			break;
		case R.id.passWord_Clear:// 清除密码
			UiUtils.ClearNumber(passWord);
			break;
		case R.id.confirmPassword_Clear:// 清除重复密码
			UiUtils.ClearNumber(confirmPassword);
			break;
		case R.id.InvitationCode_Clear:// 清除邀请码
			UiUtils.ClearNumber(invitationCode);
			break;
		case R.id.activity_back:// 返回
			finish();
			break;

		default:
			break;
		}
	}

	/**
	 * 设置按钮的定时器
	 * */
	private void setButtonFalse() {
		getcode.setClickable(false);
		Observable.interval(0, 1, TimeUnit.SECONDS)
				.filter(new Func1<Long, Boolean>() {
					@Override
					public Boolean call(Long aLong) {
						getcode.setClickable(false);
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
						getcode.setClickable(true);
					}

					@Override
					public void onNext(Long aLong) {
						if (aLong != 0) {
//							 WorkLog.i("RegisterActivity", aLong + "");
							getcode.setText("请等待" + aLong + "秒");
						} else {
							this.unsubscribe();
							getcode.setClickable(true);
							getcode.setText("获取验证码");
						}
					}
				});

	}

	public void commit() {
		// WorkLog.i("RegisterActivity", "username"+username);
		// WorkLog.i("RegisterActivity","username_code"+username_code);
		// WorkLog.i("RegisterActivity", "password"+password);
		// WorkLog.i("RegisterActivity", "confirm_password"+confirm_password);
		// WorkLog.i("RegisterActivity", "verification_code"+verification_code);
		// WorkLog.i("RegisterActivity", "verification_code2"+verification_code2);
		// 如果当前帐号的验证不存在并且当前帐号的验证和当前帐号不匹配
		// if (!TextUtils.isEmpty(username_code)&&
		// username_code.equals(username)) {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("请检查您的网络");
			return;
		}
		if (!UiUtils.isMobileNO(username)) {
			showToast("请输入正确的手机号码");
			return;
		}
		if (!TextUtils.isEmpty(verificationCode.getText().toString().trim())) {
			try {
				JSONObject smsJson = new JSONObject();
				smsJson.put("userName", username);
				smsJson.put("smsCode", verification_code);
				getUrlRequest3(UrlClient.HttpUrl + UrlClient.VaildateSmsCode,
						smsJson.toString());
			} catch (Exception e) {
				e.printStackTrace();
				if (popupWindow != null) {
					popupWindow.dismiss();
					popupWindow = null;
				}
			}
		}

		// }else {
		// showToast("当前帐号和获取验证码的帐号不一致，请重新获取验证码");
		// }
	}

	/**
	 * 注册帐号
	 * @param url2
	 * @param string
	 */
	private void getUrlRequest2(String url2, String string) {
		//WorkLog.i("RegisterActivity", "请求数据"+string);
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url2).params("json", string)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						try {
							WorkLog.i("RegisterActivity", "data:" + t);
							JSONObject jsonObject = new JSONObject(t);
//							WorkLog.i("RegisterActivity", "解析" + t.toString());
							int code = jsonObject.getInt("code");
							switch (code) {
							case 200:
								SharedPreferences sp = getSharedPreferences(
										Config.SP_NAME, MODE_PRIVATE);
								sp.edit()
										.putString(Config.LOGIN_USER_NAME,
												username)
										.putString(Config.LOGIN_PASSWORD,
												password).commit();
								SuccessDialog(sp);
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								break;
							case 400:
								if (jsonObject.has("imsCode")) {
									String imsCode = jsonObject
											.getString("imsCode");
									if (imsCode.equals("网元命令执行失败。")) {
										showToast("注册失败,该帐号已注册");
									} else {
										showToast("注册失败"
												+ jsonObject
														.getString("imsCode"));
									}
								} else if (jsonObject.has("message")) {
									showToast("注册失败"
											+ jsonObject.getString("message"));
								} else {
									showToast("注册失败");
								}

								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								break;
							case 500:
								if (jsonObject.has("imsCode")) {
									showToast("连接错误"
											+ jsonObject.getString("imsCode"));
								} else {
									showToast("连接错误");
								}
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
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
						WorkLog.i("RegisterActivity", "注册失败，发生异常");
						if (popupWindow != null) {
							popupWindow.dismiss();
							popupWindow = null;
						}
					}
				});

	}

	/**
	 * 注册成功提示框
	 * @param sp
	 */
	private void SuccessDialog(SharedPreferences sp) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
		builder.setCancelable(false);
		builder.setTitle("注册成功");
		builder.setMessage("注册帐号:9" + sp.getString(Config.LOGIN_USER_NAME, "")
				+ "\n注册密码:" + sp.getString(Config.LOGIN_PASSWORD, ""));
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				RxBus.getInstance().post(Config.Update_Login);
				finish();
				if (alertDialog != null) {
					alertDialog.cancel();
					alertDialog = null;
				}
			}
		});
		alertDialog = builder.create();
		alertDialog.show();
	}


	/**
	 * 获取短信验证码
	 * @param url
	 * @param json
	 */
	private void getUrlRequest(String url, String json) {
		// username_code = username;
		// WorkLog.i("RegisterActivity", "设置帐号验证"+username_code);
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						try {
							WorkLog.i("RegisterActivity", "data:" + t);
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

						// int a = Integer.parseInt(t.trim());
						// switch (a) {
						// case 0:
						// showToast("您的信息已被接收，我们稍后以短信方式发送给您，请注意查收");
						// break;
						// default:
						// showToast("咦，连接服务器失败");
						// break;
						// }
					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						showToast("请求错误,网络发送异常");
						WorkLog.i("RegisterActivity", "获取短信验证码失败,发生异常");
					}
				});
	}

	/**
	 * 验证短信验证码
	 * @param url
	 * @param jsonObject
	 */
	private void getUrlRequest3(String url, String jsonObject) {
		//WorkLog.i("RegisterActivity", "请求数据" + jsonObject);
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", jsonObject)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						try {
							WorkLog.i("RegisterActivity", "data:" + t);
							JSONObject json = new JSONObject(t);
							int Code = json.getInt("code");
							String message = json.getString("message");
							switch (Code) {
							case 200:
								if (password.length() >= 6) {// 密码长度必须大于等于6位
									if (UiUtils.isPasswordStandard(password)) {
										if (confirm_password.length() >= 6) {// 重复密码长度必须大于6位
											if (password
													.equals(confirm_password)) {// 密码不等于重复密码
												params = new HashMap<String, Object>();
												params.put("username", username);
												params.put("userType", "9");
												params.put("smsCode",
														verification_code);
												params.put("password", password);
												if (!TextUtils
														.isEmpty(invitationCode
																.getText()
																.toString()
																.trim())) {
													params.put("requestCode",
															invitationCode
																	.getText()
																	.toString()
																	.trim());
												} else {
													params.put("requestCode",
															"");
												}
												String string2 = NetUtils
														.Map_toJSONObject(params);
												getUrlRequest2(
														UrlClient.HttpUrl
																+ UrlClient.Register,
														string2);
											} else {
												showToast("两次密码输入不相同，请重新输入");
												confirmPassword.setText("");
												if (popupWindow != null) {
													popupWindow.dismiss();
													popupWindow = null;
												}
											}
										} else {
											showToast("重复密码长度至少是6位");
											if (popupWindow != null) {
												popupWindow.dismiss();
												popupWindow = null;
											}
										}
									} else {
										showToast("密码不能纯数字或纯密码");
										if (popupWindow != null) {
											popupWindow.dismiss();
											popupWindow = null;
										}
									}
								} else {
									showToast("密码长度至少是6位");
									if (popupWindow != null) {
										popupWindow.dismiss();
										popupWindow = null;
									}
								}
								break;
							case 400:
								showToast("验证失败," + message);
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								break;
							case 500:
								showToast("验证失败," + message);
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								break;

							default:
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
							if (popupWindow != null) {
								popupWindow.dismiss();
								popupWindow = null;
							}
						}
					}
					
					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						showToast("请求错误,网络发送异常");
						WorkLog.i("RegisterActivity", "验证短信验证码失败，发生异常");
						if (popupWindow != null) {
							popupWindow.dismiss();
							popupWindow = null;
						}
					}
				});
	}

	/**
	 * 显示转圈圈
	 */
	private void showPopupWindow() {
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		View view = View.inflate(mcontext, R.layout.dialog_progress, null);

		popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setFocusable(true); // 设置PopupWindow可获得焦点
		popupWindow.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
		// 在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
		popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		popupWindow
				.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		backgroundAlpha(0.7f);
		// 添加pop窗口关闭事件
		popupWindow.setOnDismissListener(new poponDismissListener());
		popupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				return false;
			}
		});
		// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
		// 我觉得这里是API的一个bug
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
				location[1] - popupWindow.getHeight());
	}

	/**
	 * 设置添加屏幕的背景透明度
	 * 
	 * @param bgAlpha
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}

	/**
	 * 添加新笔记时弹出的popWin关闭的事件，主要是为了将背景透明度改回来
	 * 
	 */
	class poponDismissListener implements PopupWindow.OnDismissListener {

		@Override
		public void onDismiss() {
			// Log.v("List_noteTypeActivity:", "我是关闭事件");
			backgroundAlpha(1f);
		}

	}

	private Uri SMS_INBOX = Uri.parse("content://sms/");
	private SmsObserver smsObserver;

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
				// WorkLog.i("RegisterActivity", address + ";" + body);
				// if (address.length() != 12) { // 判断发送验证码的号码，去除其它应用的验证码影响
				// return;
				// }
				// 正则表达式
				Pattern pattern = Pattern.compile("\\d{6}");// 表示连续的6个数字
				Matcher matcher = pattern.matcher(body);
				if (matcher.find()) {
					String code = matcher.group(0);// 获取匹配的数字
					verificationCode.setText(code);
					// verification_code2 = code;
					//WorkLog.i("RegisterActivity", "设置短信验证"+verification_code2);
				}
			}
			cursor.close();
		}
	}

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		OkHttpUtils.getInstance().cancelTag(this);
		getContentResolver().unregisterContentObserver(smsObserver);
		if (alertDialog != null) {
			alertDialog.cancel();
			alertDialog = null;
		}
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}
}
