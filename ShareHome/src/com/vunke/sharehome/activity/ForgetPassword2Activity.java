package com.vunke.sharehome.activity;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.PopupWindow;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.R;
import com.vunke.sharehome.R.drawable;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 输入新密码
 * @author Administrator
 */
public class ForgetPassword2Activity extends BaseActivity {
	private Button forgetpassword2_back, forgetPW2_Clear,
			confirmPassword_Clear, forgetPW2_commit;
	private EditText forgetPW2_passWord, forgetPW2_confirmPassword;
	private String username, code;
	private String type = "9";
	private Map<String, Object> params;
	private int status1 = 0;
	private int status2 = 0;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_forgetpassword2);
		getExtras();
		init();
		initListener();
	}

	/**
	 * 获取意图
	 */
	private void getExtras() {
		Intent intent = getIntent();
		username = intent.getStringExtra("username");
		code = intent.getStringExtra("code");
	}

	/**
	 * 初始化控件
	 */
	public void init() {
		forgetpassword2_back = (Button) findViewById(R.id.forgetpassword2_back);
		forgetPW2_Clear = (Button) findViewById(R.id.forgetPW2_Clear);
		confirmPassword_Clear = (Button) findViewById(R.id.confirmPassword_Clear);
		forgetPW2_commit = (Button) findViewById(R.id.forgetPW2_commit);
		forgetPW2_passWord = (EditText) findViewById(R.id.forgetPW2_passWord);
		forgetPW2_confirmPassword = (EditText) findViewById(R.id.forgetPW2_confirmPassword);
	}

	/**
	 * 获取监听事件
	 */
	public void initListener() {
		SetOnClickListener(forgetpassword2_back, forgetPW2_Clear,
				confirmPassword_Clear, forgetPW2_commit);
		forgetPW2_passWord.addTextChangedListener(new TextWatcher() {

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
				forgetPW2_Clear.setVisibility(TextUtils.isEmpty(s) ? View.GONE
						: View.VISIBLE);
				status1 = (TextUtils.isEmpty(s) ? 0 : 1);
				forgetPW2_commit
						.setBackgroundResource(status1 + status2 == 2 ? drawable.button_login_shape
								: drawable.button_login_shape2);
				if (status1 + status2 == 2) {
					forgetPW2_commit.setEnabled(true);
				} else {
					forgetPW2_commit.setEnabled(false);
				}
			}
		});
		forgetPW2_confirmPassword.addTextChangedListener(new TextWatcher() {

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
				confirmPassword_Clear.setVisibility(TextUtils.isEmpty(s) ? View.GONE
						: View.VISIBLE);
				status2 = (TextUtils.isEmpty(s) ? 0 : 1);
				forgetPW2_commit
						.setBackgroundResource(status1 + status2 == 2 ? drawable.button_login_shape
								: drawable.button_login_shape2);
				if (status1 + status2 == 2) {
					forgetPW2_commit.setEnabled(true);
				} else {
					forgetPW2_commit.setEnabled(false);
				}
			}
		});
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.forgetpassword2_back:
			finish();
			break;
		case R.id.forgetPW2_Clear:
			UiUtils.ClearNumber(forgetPW2_passWord);
			break;
		case R.id.confirmPassword_Clear:
			UiUtils.ClearNumber(forgetPW2_confirmPassword);
			break;
		case R.id.forgetPW2_commit:
			showPopupWindow();
			smsHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (NetUtils.isNetConnected(mcontext)) {
						commit();
					} else {
						showToast("咦，貌似网络出了点问题");
					}
				}
			}, 1500);
			break;

		default:
			break;
		}
	}

	/**
	 * 提交 修改密码
	 */
	private void commit() {
		String password = forgetPW2_passWord.getText().toString().trim();
		String confirmPassword = forgetPW2_confirmPassword.getText().toString()
				.trim();
		if (UiUtils.isPasswordStandard(password)) {
			if (password.equals(confirmPassword)) {
				params = new HashMap<String, Object>();
				params.put("username", username);
				params.put("userType", type);
				params.put("smsCode", code);
				params.put("password", password);
				String string = NetUtils.Map_toJSONObject(params);
				getUrlRequest(UrlClient.HttpUrl + UrlClient.UpdatePassword,
						string);
			} else {
				if (popupWindow != null) {
					popupWindow.dismiss();
					popupWindow = null;
				}
				showToast("两次密码输入不相同,请重新输入");
			}
		} else {
			if (popupWindow != null) {
				popupWindow.dismiss();
				popupWindow = null;
			}
			showToast("密码不能纯数字或纯密码");
		}

	}

	private void getUrlRequest(String url, String json) {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						// isFromCache 表示当前回调是否来自于缓存
						 WorkLog.e("ForgetPassword2Activity", "data:" + t);
						try {
							JSONObject jsonObject = new JSONObject(t);
//							WorkLog.e("ForgetPassword2Activity", "解析" + jsonObject.toString());
							int code = jsonObject.getInt("code");
							switch (code) {
							case 200:
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								showToast("修改成功");
								finish();
								break;
							case 400:
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								showToast("修改失败"
										+ jsonObject.getString("message"));
								break;
							case 500:
								if (popupWindow != null) {
									popupWindow.dismiss();
									popupWindow = null;
								}
								showToast("连接错误"
										+ jsonObject.getString("message"));
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
						WorkLog.e("ForgetPassword2Activity", "修改密码网络错误");
						if (popupWindow != null) {
							popupWindow.dismiss();
							popupWindow = null;
						}
					}
				});

	}

	public Handler smsHandler = new Handler() {
		// 这里可以进行回调的操作

	};
	private PopupWindow popupWindow;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
		OkHttpUtils.getInstance().cancelTag(this);
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

}
