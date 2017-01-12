package com.vunke.sharehome.activity;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TtsSpan.OrdinalBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.LoginActivity2.SmsObserver;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.model.BuyTrafficBean;
import com.vunke.sharehome.model.DnwSmsOrderBean;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.InputDataDialog;

/**
 * 
 * 订购定向流量包
 * 
 * @author zhuxi
 * 
 */
@SuppressLint("NewApi")
public class BuyTrafficActivity extends BaseActivity {
	/**
	 * 返回键
	 */
	private Button BuyTraffic_back;
	/**
	 * 订购按钮
	 */
	private Button BuyTraffic_commit;
	/**
	 * 订购Bean
	 */
	private DnwSmsOrderBean orderBean;
	/**
	 * 弹窗
	 */
	private InputDataDialog inputDataDialog;
	/**
	 * 弹窗
	 */
	private Button DialogBut;
	/**z
	 * 订单号
	 */
	private String orderNo;
	public Handler smsHandler = new Handler() {
		// 这里可以进行回调的操作

	};
	private String username;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_buytraffic);
		init();
		initListener();
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		BuyTraffic_back = (Button) findViewById(R.id.buyTraffic_back);
		BuyTraffic_commit = (Button) findViewById(R.id.buyTraffic_commit);
	}

	/**
	 * 初始化监听事件
	 */
	private void initListener() {
		username = UiUtils.GetUserName(mcontext).substring(1);
		SetOnClickListener(BuyTraffic_back, BuyTraffic_commit);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.buyTraffic_back:
			finish();
			break;
		case R.id.buyTraffic_commit:
			Commit();
			break;
		default:
			break;
		}

	}

	private void Commit() {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("网络未连接，请检查网络");
			return;
		}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("phone", username);
			getUrlRequest(UrlClient.HttpUrl + UrlClient.GetDnwSmsCode,
					jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		BuyTraffic_commit.setEnabled(false);
		BuyTraffic_commit.setBackgroundResource(R.drawable.button_login_shape2);
	}

	private void getUrlRequest(String url, String json) {
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						 WorkLog.i("GetDnwSmsCodeActivity", "data:" + t);
						try {
							JSONObject jsonObject = new JSONObject(t);
							int Code = jsonObject.getInt("code");
							switch (Code) {
							case 200:
								String result = jsonObject.getString("result");
								WorkLog.i("GetDnwSmsCodeActivity", "result"
										+ result);
								try {
									Gson gson = new Gson();
									orderBean = gson.fromJson(t,
											DnwSmsOrderBean.class);
								} catch (Exception e) {
									e.printStackTrace();
								}
								ShowBuyTrafficDialog();
								break;
							case 400:
								showToast("请求服务器失败");
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
						WorkLog.i("GetDnwSmsCodeActivity", "获取短信验证码发生异常");
					}

					@Override
					public void onAfter(boolean isFromCache,
							@Nullable String t, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						BuyTraffic_commit.setEnabled(true);
						BuyTraffic_commit
								.setBackgroundResource(R.drawable.button_login_shape);
					}
				});
	}
	
	private void ShowBuyTrafficDialog() {
		inputDataDialog = new InputDataDialog(mcontext);
		inputDataDialog
				.builder()
				.setTitle(
						"本次交易需要短信确认,验证码已经发送到您的手机："
								+ UiUtils.GetUserName(mcontext).substring(1))
				.setHint(R.string.pleaseInput_verificationCode)
				.setCancelable(false).setCanceledOnTouchOutside(false)
				.SetCommitOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							DialogBut = inputDataDialog.getCommit();
							DialogBut.setEnabled(false);
							DialogBut.setText("订购中...");
							DialogBut.setBackgroundResource(R.drawable.button_login_shape2);
							EditText editText = inputDataDialog.getEditText();
							String checkCode = editText.getText().toString()
									.trim();
							if (TextUtils.isEmpty(checkCode)) {
								showToast("验证码不能为空");
								return;
							}
							if (orderBean != null
									&& orderBean.getResult() != null) {
								orderNo = orderBean.getResult().getOrder_no();
							}
							if (TextUtils.isEmpty(orderNo)) {
								showToast("订单信息有误，请重新获取短信验证码");
								inputDataDialog.dismiss();
								return;
							}
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("phone", username);
							jsonObject.put("checkCode", checkCode);
							jsonObject.put("orderNo", orderNo);
							getUrlRequest2(UrlClient.HttpUrl
									+ UrlClient.DnwSmsOrder,
									jsonObject.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).show();
	}

	private BuyTrafficBean buyTrafficBean;
	private boolean codeErr = false;
	private void getUrlRequest2(String url, String json) {
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						 WorkLog.i("GetDnwSmsCodeActivity", "data:" + t);
						try {
							JSONObject jsonObject = new JSONObject(t);
							int Code = jsonObject.getInt("code");
							switch (Code) {
							case 200:
								WorkLog.i("GetDnwSmsCodeActivity", "订购成功");
								showToast("订购成功");
								orderSuccess();
								finish();
								break;
							case 400:
								WorkLog.i("GetDnwSmsCodeActivity", "订购失败400");
								try {
									Gson gson = new Gson();
									buyTrafficBean = gson.fromJson(t,
											BuyTrafficBean.class);
									if (buyTrafficBean!=null ) {
										try {
											if (buyTrafficBean.getResult()!=null) {
												if (buyTrafficBean.getResult().getMsg().equals("验证码不一致")) {
													showToast("订购失败,验证码错误,请重新输入");
													codeErr = true;
													return ;
												}else {
													codeErr = false;
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									showToast("订购失败");
									orderErr(buyTrafficBean);
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
							case 500:
								showToast("连接服务器失败");
								WorkLog.i("GetDnwSmsCodeActivity", "订购失败500");
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
						WorkLog.i("GetDnwSmsCodeActivity", "获取短信验证码发生异常");
					}

					@Override
					public void onAfter(boolean isFromCache,
							@Nullable String t, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						WorkLog.i("BuyTrafficActivity", "codeErr:"+codeErr);
						DialogBut.setText("确定");
						DialogBut.setEnabled(true);
						DialogBut.setBackgroundResource(R.drawable.button_login_shape);
						if (codeErr == true) {
							return;
						}else {
							inputDataDialog.dismiss();
						}
					}
				});
	}

	private void orderSuccess() {
		Dialog dialog = new Dialog(mcontext, R.style.ActionSheetDialogStyle);

		AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
		builder.setMessage("订购成功");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
		dialog = builder.show();
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.CENTER);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);
	}

	protected void orderErr(BuyTrafficBean bean) {
		Dialog dialog = new Dialog(mcontext, R.style.ActionSheetDialogStyle);

		AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
		builder.setMessage(bean.getMessage() + ":原因("
				+ bean.getResult().getRet()+")");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
		dialog = builder.show();
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.CENTER);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		OkHttpUtils.getInstance().cancelTag(this);
	}
}
