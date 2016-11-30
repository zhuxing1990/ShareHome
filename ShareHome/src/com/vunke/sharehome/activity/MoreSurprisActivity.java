package com.vunke.sharehome.activity;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;

import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.listener.MyWebViewDownLoadListener;
import com.vunke.sharehome.url.UrlClient;

/**
 * 更多精彩
 * 
 * @author Administrator
 */
/**
 * @author zhuxi
 *
 */
/**
 * @author zhuxi
 *
 */
public class MoreSurprisActivity extends BaseActivity {
	/**
	 * 返回键
	 */
	private Button moresurpris_back;
	/**
	 * 更多精彩 WebView
	 */
	private WebView moresurpris_webView;

	/**
	 * 
	 */
	// private static final int code = 486739;

	/**
	 * 编码格式 UTF-8
	 */
	static final String encoding = "utf-8";

	static final String mimeType = "text/html"; // "surpris/html"

	private Handler handler = new Handler();

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_moresurpris);
		init();
		initListener();
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		moresurpris_back = (Button) findViewById(R.id.moresurpris_back);
		moresurpris_webView = (WebView) findViewById(R.id.moresurpris_webView);
		SetOnClickListener(moresurpris_back);
	}

	/**
	 * 初始化WebView
	 */
	private void initListener() {
		WebSettings settings = moresurpris_webView.getSettings();
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

		moresurpris_webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				super.onPageFinished(view, url);
			}
		});
		moresurpris_webView.setWebChromeClient(new WebChromeClient() {

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
		moresurpris_webView.setDownloadListener(new MyWebViewDownLoadListener(mcontext));
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

		moresurpris_webView.loadUrl(UrlClient.MoreSurpris);
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
	
	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.moresurpris_back:
			finish();
			if (popupWindow != null) {
				popupWindow.dismiss();
				popupWindow = null;
			}
			break;

		default:
			break;
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
		moresurpris_webView.destroy();
		finish();
	}
	/**
	 * 短息
	 */
	public Handler smsHandler = new Handler() {
		// 这里可以进行回调的操作

	};
	private PopupWindow popupWindow;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ClearDialog();
	}

	private void showPopupWindow() {
		View view = View.inflate(mcontext, R.layout.dialog_progress, null);

		popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setFocusable(true); // 设置PopupWindow可获得焦点
		popupWindow.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
		// 在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
		popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
		popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] - popupWindow.getHeight());
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
