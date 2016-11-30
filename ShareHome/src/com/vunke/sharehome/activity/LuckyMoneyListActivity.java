package com.vunke.sharehome.activity;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.listener.MyWebViewDownLoadListener;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

public class LuckyMoneyListActivity extends BaseActivity {
	private WebView moneylist_webview;
	private Button moneylist_back;
	/**
	 * 编码格式 UTF-8
	 */
	static final String encoding = "utf-8";

	static final String mimeType = "text/html"; // "surpris/html"

	private Handler handler = new Handler();
	private PopupWindow popupWindow;
	private TextView moneylist_title;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_luckymoneylist);
		init();
		initWebView();
	}

	private void init() {
		moneylist_webview = (WebView) findViewById(R.id.moneylist_webview);
		moneylist_back = (Button) findViewById(R.id.moneylist_back);
		moneylist_title = (TextView) findViewById(R.id.moneylist_title);
		if (!TextUtils.isEmpty(UrlClient.ActivityName)) {
			moneylist_title.setText(UrlClient.ActivityName);
		}
		SetOnClickListener(moneylist_back);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.moneylist_back:
			finish();
			break;

		default:
			break;
		}
	}

	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void initWebView() {
		WebSettings settings = moneylist_webview.getSettings();
		settings.setJavaScriptEnabled(true);// 支持js
		settings.setDefaultTextEncodingName("GBK");// 设置字符编码
		// 启用支持javascript
		settings.setJavaScriptEnabled(true);
		// settings.setBuiltInZoomControls(false);
		// settings.setRenderPriority(RenderPriority.HIGH);
		// settings.setBlockNetworkImage(true);
		// 缓存
		// settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		// 设置可以支持缩放
		settings.setSupportZoom(true);
		// 设置出现缩放工具
		// settings.setBuiltInZoomControls(true);
		// //扩大比例的缩放
		settings.setUseWideViewPort(true);
		// 自适应屏幕
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setAllowFileAccess(true);
		settings.setAppCacheEnabled(true);
		moneylist_webview.addJavascriptInterface(new JavaScriptObject(),
				"shareHome");
		// settings.setSaveFormData(false);
		// settings.setPluginsEnabled(true);
		// moresurpris_webView.refreshPlugins(true);
		// settings.setLoadsImagesAutomatically(true);
		// http请求的时候，模拟为火狐的UA会造成实时公交那边的页面存在问题，所以模拟iPhone的ua来解决这个问题
		// String user_agent = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en)
		// AppleWebKit/124 (KHTML, like Gecko) Safari/125.1";
		// settings.setUserAgentString(user_agent);

		/* Enable zooming */
		// settings.setSupportZoom(false);

		moneylist_webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				super.onPageFinished(view, url);
			}
		});
		moneylist_webview.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) { // 网页加载完成
					ClearDialog();
				} else if (newProgress < 1) { // 加载中
					showPopupWindow();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							ClearDialog();
						}
					}, 1500);

				}
				super.onProgressChanged(view, newProgress);
			}
		});
		moneylist_webview.setDownloadListener(new MyWebViewDownLoadListener(
				mcontext));
		// try {
		// // 禁用硬件加速
		// Method method = WebView.class.getMethod("setLayerType", int.class,
		// Paint.class);
		// method.setAccessible(true);
		// method.invoke(moresurpris_webView, 1, null);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// new Handler().postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// moresurpris_webView.getSettings().setBlockNetworkImage(false);
		// }
		// }, 1000);

		// moneylist_webview.loadUrl("http://qllp4.free.natapp.cc/ShareHome/luckymoney/home.jsp");
		String userDn = Config.CALL_BEFORE + UiUtils.GetUserName();

		String activityId = "8";
		String phone = UiUtils.GetUserName().substring(1);
		String type = "android";
		try {
			// JSONObject jsonObject = new JSONObject();
			// jsonObject.put("userDn", userDn);
			// jsonObject.put("luckyResource", luckyResource);
			// jsonObject.put("activityId", activityId);
			// jsonObject.put("phone", phone);
			// jsonObject.put("payMessage", payMessage);
			// jsonObject.put("extParam1", extParam1);
			// jsonObject.put("title", title);
			// String data = "json="+jsonObject.toString();
			String data = "userDn=" + userDn + "&" + "activityId=" + activityId
					+ "&" + "phone=" + phone + "&" + "type=" + type;

			WorkLog.e("url", UrlClient.LuckyMoneyListUrl + "?" + data);
			// moneylist_webview.postUrl(UrlClient.LuckyMoneyUrl,
			// EncodingUtils.getBytes(data, "BASE64"));
			moneylist_webview.loadUrl(UrlClient.LuckyMoneyListUrl + "?" + data);

			// moneylist_webview.postUrl(UrlClient.LuckyMoneyUrl,data.ge
			// tBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭弹窗
	 */
	private void ClearDialog() {
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	/**
	 * 方法名称：onBackEvent
	 * <p>
	 * 方法描述：返回键事件
	 * 
	 * @author wangZhongfu
	 * @param view
	 *            <p>
	 *            备注：
	 */
	public void onBackEvent(View view) {
		moneylist_webview.destroy();
		finish();
	}

	private void showPopupWindow() {
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ClearDialog();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && moneylist_webview.canGoBack()) {

			moneylist_webview.goBack(); // goBack()表示返回WebView的上一页面

			return true;

		}

		return super.onKeyDown(keyCode, event);
	}

	public class JavaScriptObject {

		public void Finish() {

		}

		@JavascriptInterface
		public void clickOnAndroid(String data) {
			try {
				WorkLog.e("LuckyMoneyActivity", "data:" + data);
				JSONObject jsonObject = new JSONObject(data);
				if (jsonObject.has("code")) {
					int code = jsonObject.getInt("code");
					switch (code) {
					case 200:
						UiUtils.showToast(jsonObject.getString("message"));
						handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								finish();

							}
						},4000);
						break;
					case 400:
						UiUtils.showToast(jsonObject.getString("message"));
						break;
					case 500:
						UiUtils.showToast(jsonObject.getString("message"));
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// UiUtils.showToast("data="+data);
		}

	}

}
