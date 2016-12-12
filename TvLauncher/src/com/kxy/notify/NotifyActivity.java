package com.kxy.notify;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kxy.auth.Auth;
import com.kxy.auth.AuthInfo;
import com.kxy.auth.Config;
import com.kxy.auth.MACUtil;
import com.kxy.notify.NotifyBean2.DataBean;
import com.kxy.notify.NotifyBean2.DataBean.ProductAttr;
import com.kxy.ti.util.Constants;
import com.kxy.ti.util.LogUtil;
import com.kxy.ti.util.SharedPreferencesUtil;
import com.kxy.ti.util.UIUtil;
import com.kxy.tl.R;
import com.kxy.tl.activity.TupianActivity;
import com.kxy.tl.div.CircleLoadingView;
import com.kxy.tl.dlg.CustomProgressDialog;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;

/**
 * 消息推送
 * 
 * @author zhuxi
 * 
 */
@SuppressLint("NewApi")
public class NotifyActivity extends Activity {
	private WebView notfy_webView;
	private LinearLayout notify_layout;
	private String name;
	private long endtime;
	private long starttime;
	private long timestamp;
	private int push_id;
	private int strategy_id;
	private int push_type;
	private int behavior_type;
	private String path;
	private String version_code;

	private String keyvalue = "113";
	private String getVersion_vode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LogUtil.e("tv_launcher", "onCreate");
		setContentView(R.layout.activity_notify);
		init();
		initView();
	}

	public void init() {
		Intent intent = getIntent();
		if (intent.hasExtra("path")) {
			path = intent.getStringExtra("path");
			LogUtil.e("tv_launcher", "path:" + path);
			if (TextUtils.isEmpty(path)) {
				StartEPGing();
			}
		} else {
			StartEPGing();
		}
		if (intent.hasExtra("UserId")) {
			name = intent.getStringExtra("UserId");
			LogUtil.e("tv_launcher", "UserId:" + name);
		}

		if (intent.hasExtra("push_id")) {
			push_id = intent.getIntExtra("push_id", 0);
			LogUtil.e("tv_launcher", "push_id:" + push_id);
		}
		if (intent.hasExtra("strategy_id")) {
			strategy_id = intent.getIntExtra("strategy_id", 0);
			LogUtil.e("tv_launcher", "strategy_id:" + strategy_id);
		}
		if (intent.hasExtra("behavior_type")) {
			behavior_type = intent.getIntExtra("behavior_type", 0);
			LogUtil.e("tv_launcher", "behavior_type:" + behavior_type);
		}
		if (intent.hasExtra("push_type")) {
			push_type = intent.getIntExtra("push_type", 0);
			LogUtil.e("tv_launcher", "push_type:" + push_type);
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date(System.currentTimeMillis());
		String format = dateFormat.format(date);
		getVersion_vode = 1 + format;
	}

	private void StartEPGing() {
		UiUtils.StartMangGuoEPG(getApplicationContext());
		finish();
	}

	/**
	 * 初始化 webView
	 */
	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void initView() {
		notify_layout = (LinearLayout) findViewById(R.id.notify_layout);
		notfy_webView = (WebView) findViewById(R.id.notfy_webView);

		// notfy_webView.clearCache(true);
		// notfy_webView.clearHistory();
		WebSettings settings = notfy_webView.getSettings();
		// 支持js
		settings.setJavaScriptEnabled(true);
		// 设置字符编码
		settings.setDefaultTextEncodingName("GBK");
		// 启用支持javascript
		settings.setJavaScriptEnabled(true);
		// 设置可以支持缩放
		settings.setBuiltInZoomControls(true);
		settings.setLightTouchEnabled(true);
		settings.setSupportZoom(true);
		// 不使用缓存，只从网络获取数据.
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		// settings.setLoadWithOverviewMode(true);
		// 支持JS交互
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		notfy_webView.addJavascriptInterface(new JavaScriptObject(),
				"tv_launcher");

		notfy_webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				LogUtil.e("tv_launcher", "网页加载中");
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				LogUtil.e("tv_launcher", "网页加载结束");
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						notfy_webView.setVisibility(View.VISIBLE);
					}
				}, 200);
			}
			@Override
			 public boolean shouldOverrideUrlLoading(WebView view, String url) {
			 view.loadUrl(url);//在2.3上面不加这句话，可以加载出页面，在4.0上面必须要加入，不然出现白屏
			 return true;
			}
		});
		notfy_webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress < 1) { // 加载中
					LogUtil.e("tv_launcher", "网页加载进度：" + newProgress);
					notfy_webView.requestFocus();
					// notfy_webView.setVisibility(View.GONE);
				} else if (newProgress == 100) { // 网页加载完成
					LogUtil.e("tv_launcher", "网页加载进度：" + newProgress);
					notfy_webView.requestFocus();
					// notfy_webView.setVisibility(View.VISIBLE);
				}
				super.onProgressChanged(view, newProgress);
			}
		});
		notfy_webView.setDownloadListener(new MyWebViewDownLoadListener());
		String pasams = "account="+name;
		notfy_webView.postUrl(path, pasams.getBytes());
	}

	/**
	 * WebView 点击下载监听
	 * 
	 * @author zhuxi
	 */
	private class MyWebViewDownLoadListener implements DownloadListener {

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}

	}

	/**
	 * 安卓与JS交互
	 * 
	 * @author zhuxi
	 */
	public class JavaScriptObject {
		@JavascriptInterface
		public void sendMessageToJAVA(String json) {
			LogUtil.e("tv_launcher", "getJavaScript:" + json);
			// Toast.makeText(getApplicationContext(), json,
			// Toast.LENGTH_SHORT).show();
			if (json.equals("0")) {
				LogUtil.e("tv_launcher", "用户没有操作");
				UiUtils.StartMangGuoEPG(getApplicationContext());
				finish();
			} else if (json.equals("1")) {
				LogUtil.e("tv_launcher", "用户正在操作");
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.e("tv_launcher", "keyCode:" + keyCode);
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {// && notfy_webView.canGoBack()
			StartEPGing();
			// notfy_webView.goBack(); // goBack()表示返回WebView的上一页面
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			LogUtil.e("tv_launcher", "用户按HOME键");
			StartEPGing();
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onStop() {
		super.onStop();
		endtime = System.currentTimeMillis() / 1000;
		timestamp = System.currentTimeMillis() / 1000;
		OkHttpUtils.post(Config.Notify_Url + Config.Behaviour).tag(this)
				.params("push_id", push_id + "").params("name", name)
				.params("stratey_id", strategy_id + "")
				.params("behavior_type", behavior_type + "")
				.params("push_type", push_type + "")
				.params("starttime", starttime + "")
				.params("endtime", endtime + "")
				.params("timestamp", timestamp + "")
				.params("keyvalue", keyvalue)
				.params("version_code", version_code)
				.params("business_type", "2").execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						// LogUtil.e(TAG, request.body());
						LogUtil.e("tv_launcher", "请求成功:" + t);
					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						LogUtil.e("tv_launcher", "Onerror");
					}
				});
	}

}
