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
import android.content.Context;
import android.content.Intent;
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
import android.view.View.OnLongClickListener;
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
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 修改密码 获取短信验证码
 * @author Administrator
 */
public class ForgetPasswordActivity extends BaseActivity {
	private EditText forgetPW_userName, forgetPW_verificationCode;
	private Button clear_userName, clear_verificationcode, forgetPW_getcode,
			forgetPW_commit;
	private ImageButton forgetpassword_back;
	private int status1 = 0;
	private int status2 = 0;
	public Handler smsHandler = new Handler() {
		// 这里可以进行回调的操作

	};
	private PopupWindow popupWindow;
	private Uri SMS_INBOX = Uri.parse("content://sms/");
	private SmsObserver smsObserver;
	private String username;
	private Map<String, Object> params;
	private String code;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_forgetpassword);
		init();
		initListener();
	}

	/**
	 * 监听事件
	 */
	public void initListener() {
		SetOnClickListener(forgetpassword_back, clear_userName,
				clear_verificationcode, forgetPW_getcode, forgetPW_commit);
		clear_userName.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				forgetPW_userName.setText("");
				return false;
			}
		});
		clear_verificationcode
				.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						forgetPW_verificationCode.setText("");
						return false;
					}
				});

		forgetPW_userName.addTextChangedListener(new TextWatcher() {

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
				clear_userName.setVisibility(TextUtils.isEmpty(s) ? View.GONE
						: View.VISIBLE);
				status1 = (TextUtils.isEmpty(s) ? 0 : 1);
				forgetPW_commit
						.setBackgroundResource(status1 + status2 == 2 ? drawable.button_login_shape
								: drawable.button_login_shape2);
				// WorkLog.e("ForgetPasswordActivity", "status1" + status1);
				if (status1 + status2 == 2) {
					forgetPW_commit.setEnabled(true);
				} else {
					forgetPW_commit.setEnabled(false);
				}
			}
		});
		forgetPW_verificationCode.addTextChangedListener(new TextWatcher() {

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
				clear_verificationcode.setVisibility(TextUtils.isEmpty(s) ? View.GONE
						: View.VISIBLE);
				status2 = (TextUtils.isEmpty(s) ? 0 : 1);
				forgetPW_commit
						.setBackgroundResource(status1 + status2 == 2 ? drawable.button_login_shape
								: drawable.button_login_shape2);
				// WorkLog.e("ForgetPasswordActivity", "status2" + status2);
				if (status1 + status2 == 2) {
					forgetPW_commit.setEnabled(true);
				} else {
					forgetPW_commit.setEnabled(false);
				}
			}
		});
		smsObserver = new SmsObserver(this, smsHandler);
		getContentResolver().registerContentObserver(SMS_INBOX, true,
				smsObserver);
	}

	/**
	 * 初始化控件
	 */
	public void init() {
		forgetPW_userName = (EditText) findViewById(R.id.forgetPW_userName);
		forgetPW_verificationCode = (EditText) findViewById(R.id.forgetPW_verificationCode);
		forgetpassword_back = (ImageButton) findViewById(R.id.forgetpassword_back);
		clear_userName = (Button) findViewById(R.id.clear_userName);
		clear_verificationcode = (Button) findViewById(R.id.clear_verificationcode);
		forgetPW_getcode = (Button) findViewById(R.id.forgetPW_getcode);
		forgetPW_commit = (Button) findViewById(R.id.forgetPW_commit);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.clear_userName:
			UiUtils.ClearNumber(forgetPW_userName);
			break;
		case R.id.clear_verificationcode:
			UiUtils.ClearNumber(forgetPW_verificationCode);
			break;
		case R.id.forgetPW_getcode:
			username = forgetPW_userName.getText().toString().trim();
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
		case R.id.forgetPW_commit:
			code = forgetPW_verificationCode.getText().toString().trim();
			showPopupWindow();
			smsHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					commit();
				}
			}, 1500);
			break;
		case R.id.forgetpassword_back:
			finish();
			break;
		default:
			break;
		}

	}

	private void commit() {
		username = forgetPW_userName.getText().toString().trim();
		if (UiUtils.isMobileNO(forgetPW_userName.getText().toString().trim())) {
			if (!TextUtils
					.isEmpty(forgetPW_getcode.getText().toString().trim())) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("userName", username);
					jsonObject.put("smsCode", code);
//					WorkLog.e("ForgetPasswordActivity", "发送数据" + jsonObject.toString());
					getUrlRequest2(UrlClient.HttpUrl
							+ UrlClient.VaildateSmsCode, jsonObject.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				showToast("验证码不能为空");
				if (popupWindow != null) {
					popupWindow.dismiss();
					popupWindow = null;
				}
			}
		} else {
			showToast("您输入的手机号码不正确");
			if (popupWindow != null) {
				popupWindow.dismiss();
				popupWindow = null;
			}
		}

	}

	private void getUrlRequest(String url, String json) {// username_code =
															// username;
		// WorkLog.e("ForgetPasswordActivity", "设置帐号验证"+username_code);
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						 WorkLog.e("ForgetPasswordActivity", "data:" + t);
						try {
							JSONObject jsonObject = new JSONObject(t);
							int Code = jsonObject.getInt("code");
							String message = jsonObject.getString("message");
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
						WorkLog.e("ForgetPasswordActivity", "获取短信失败,发生异常");
					}
				});
	}

	private void getUrlRequest2(String url, String jsonObject) {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", jsonObject)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						// WorkLog.e("ForgetPasswordActivity", "json=" + t);
						 WorkLog.e("ForgetPasswordActivity", "data:" + t);
						try {
							JSONObject json = new JSONObject(t);
							int Code = json.getInt("code");
							String message = json.getString("message");
							switch (Code) {
							case 200:
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								Config.intent = new Intent(mcontext,
										ForgetPassword2Activity.class);
								Config.intent.putExtra("username", username);
								Config.intent.putExtra("code", code);
								startActivity(Config.intent);
								finish();
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
						WorkLog.e("ForgetPasswordActivity", "验证短信验证码失败，发生异常");
						if (popupWindow != null) {
							popupWindow.dismiss();
							popupWindow = null;
						}
					}
				});
	}

	private void setButtonFalse() {
		forgetPW_getcode.setClickable(false);
		Observable.interval(0, 1, TimeUnit.SECONDS)
				.filter(new Func1<Long, Boolean>() {
					@Override
					public Boolean call(Long aLong) {
						forgetPW_getcode.setClickable(false);
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
						forgetPW_getcode.setClickable(true);
					}

					@Override
					public void onNext(Long aLong) {
						if (aLong != 0) {
							// WorkLog.e("ForgetPasswordActivity",aLong + "");

							forgetPW_getcode.setText("请等待" + aLong + "秒");
						} else {
							this.unsubscribe();
							forgetPW_getcode.setClickable(true);
							forgetPW_getcode.setText("获取验证码");
						}
					}
				});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		OkHttpUtils.getInstance().cancelTag(this);
		getContentResolver().unregisterContentObserver(smsObserver);
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

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


	public void getSmsFromPhone() {
		Cursor cursor = UiUtils
				.getContext()
				.getContentResolver()
				.query(Uri.parse("content://sms/inbox"), null, null, null,
						"date desc");
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String address = cursor.getString(cursor
						.getColumnIndex("address"));
				String body = cursor.getString(cursor.getColumnIndex("body"));
				// WorkLog.e("ForgetPasswordActivity", address + ";" + body);
				// if (!address.equals("073184203359")) { //
				// 判断发送验证码的号码，去除其它应用的验证码影响
				// return;
				// }
				// 正则表达式
				Pattern pattern = Pattern.compile("\\d{6}");// 表示连续的6个数字
				Matcher matcher = pattern.matcher(body);
				if (matcher.find()) {
					String code = matcher.group(0);// 获取匹配的数字
					// WorkLog.e("ForgetPasswordActivity", code);
					forgetPW_verificationCode.setText(code);
					// verification_code2 = code;
					// WorkLog.e("ForgetPasswordActivity",
					// "设置短信验证"+verification_code2);
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

}
