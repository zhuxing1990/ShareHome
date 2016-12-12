package com.kxy.tl.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.kxy.base.RxBus;
import com.kxy.notify.NotifyActivity;
import com.kxy.notify.NotifyBean2;
import com.kxy.notify.NotifyService;
import com.kxy.notify.NotifyBean2.DataBean;
import com.kxy.notify.NotifyBean2.DataBean.ProductAttr;
import com.kxy.notify.UiUtils;
import com.kxy.ti.util.FileManager;
import com.kxy.ti.util.LogUtil;
import com.kxy.tl.R;
import com.kxy.tl.download.Util;

public class TupianActivity extends Activity {
	/** Called when the activity is first created. */
	private ImageView image = null;
	private AnimationDrawable animationDrawable = null;
	private static Handler handler;
	private static Runnable runnable;
	private Subscription subscribe;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_dispaly);
		image = (ImageView) findViewById(R.id.imageview);
		initService();
		initRX();
		animationDrawable = new AnimationDrawable();
		List<Drawable> picsPath = FileManager.getpicsPath(
				FileManager.isSdcard(TupianActivity.this), getResources());
		if (picsPath != null && picsPath.size() > 0) {
			for (int i = 0; i < picsPath.size(); i++) {
				animationDrawable.addFrame(picsPath.get(i), 5000);
			}
		} else {
			picsPath = FileManager.getPicFromAsset(this, getResources());
			for (int i = 0; i < picsPath.size(); i++) {
				animationDrawable.addFrame(picsPath.get(i), 5000);
			}
		}
		// 设置是否重复播放，false为重复
		animationDrawable.setOneShot(true);
		image.setImageDrawable(animationDrawable);
		animationDrawable.start();

	}

	/**
	 * 获取用户信息和推送数据 service
	 */
	private void initService() {
		LogUtil.e("tv_launcher", "start notifyservice");
		Intent intent = new Intent(TupianActivity.this, NotifyService.class);
		startService(intent);
	}

	/**
	 * 获取 用户信息 RxBus
	 */
	private void initRX() {
		subscribe = RxBus.getInstance().toObservable(NotifyBean2.class)
				.filter(new Func1<NotifyBean2, Boolean>() {

					@Override
					public Boolean call(NotifyBean2 arg0) {
						// TODO Auto-generated method stub
						return arg0.getRxBuscode() == 20161207;
					}
				}).subscribe(new Subscriber<NotifyBean2>() {

					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(Throwable e) {
						LogUtil.e("tv_launcher", "RxBus error");
						e.printStackTrace();
						this.isUnsubscribed();
					}

					@Override
					public void onNext(final NotifyBean2 arg0) {
						if (arg0.getRxBuscode() == 20161207) {
							UserId = arg0.getUserId();
							LogUtil.e("tv_launcher", "UserId:" + UserId);
							bean2 = arg0;
							handler2.sendEmptyMessageDelayed(0x1134, 2000);
							//
						}
					}
				});
	}
	private NotifyBean2 bean2;
	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x1134:
				if (Util.isNetConnected(getApplicationContext())==true) {
					initUrl(bean2);
				}else {
					handler2.sendEmptyMessageDelayed(0x1134, 2000);
				}
				break;

			default:
				break;
			}
		};
	};
	// 验证当前数据类型是否出现多条
	private int notifdata = 0;
	// 验证当前网页是否正常
	private boolean initWebView = false;

	/**
	 * 解析数据 加载WebView
	 * 
	 * @param bean
	 */
	protected void initUrl(NotifyBean2 bean) {
		if (bean != null && bean.getData().size() != 0) {
			List<DataBean> data = bean.getData();
			if (data != null && data.size() != 0) {
				LogUtil.e("tv_launcher", "data数据条数" + data.size());
				// 首先判断数据中是否有 push_type = 6 的字段
				LogUtil.e("tv_launcher", "首先判断数据中是否有 push_type");
				boolean getCode = false;
				boolean hasCode = false;
				for (int i = 0; i < data.size(); i++) {
					getCode = data.get(i).getPush_type() == 6 ? true : false;
					LogUtil.e("tv_launcher", "push_type是否存在？\n" + getCode);
					if (getCode == true) {
						path = data.get(i).getSpecial_url();
						if (!TextUtils.isEmpty(path)) {
							ArrayList<ProductAttr> productAttr = data.get(i)
									.getProductAttr();
							if (productAttr != null) {// 商机单
								getCode = false;
								continue;
							} else {
								hasCode = true;
								notifdata++;
								if (notifdata == 1) {
									push_id = data.get(i).getPush_id();
									strategy_id = data.get(i).getStrategy_id();
									behavior_type = data.get(i).getBusiType();
									push_type = data.get(i).getPush_type();
									LogUtil.e("tv_launcher", path);
									// notfy_webView.requestFocus();
									new Thread(new Runnable() {

										@Override
										public void run() {
											int status = -1;
											HttpHead head = new HttpHead(path);
											HttpClient client = new DefaultHttpClient();
											HttpResponse resp;
											try {
												resp = client.execute(head);
												status = resp.getStatusLine()
														.getStatusCode();
												if (status != 200) {
													LogUtil.e("tv_launcher",
															"验证网页失败,status:"
																	+ status);
													initWebView = false;
												} else {
													LogUtil.e("tv_launcher",
															"验证网页成功");
													initWebView = true;
												}
											} catch (Exception e) {
												initWebView = false;
												LogUtil.e("tv_launcher",
														"验证网页失败");
												e.printStackTrace();
											}
										}
									}).start();
									i = data.size();
								} else {
									LogUtil.e("tv_launcher",
											"path = 6 has two size");
								}
							}
						} else {
							LogUtil.e("tv_launcher", "path is null");
						}
					} else {
						LogUtil.e("tv_launcher", "push_type = 6 is false");
					}
				}
				if (hasCode) {

				} else {
					// StartEPGing();
				}
			} else {
				LogUtil.e("tv_launcher", "data is null");
			}
		} else {
			LogUtil.e("tv_launcher", "bean is null");
			// StartEPGing();
		}
	}

	private String path;
	private String UserId;
	private int push_id;
	private int strategy_id;
	private int behavior_type;
	private int push_type;

	@Override
	protected void onResume() {
		super.onResume();
		int duration = 0;
		for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
			duration += animationDrawable.getDuration(i);
		}
		handler = new Handler();
		runnable = new Runnable() {
			public void run() {
				if (initWebView == true) {
					Intent intent = new Intent(TupianActivity.this,
							NotifyActivity.class);

					intent.putExtra("path", path);
					intent.putExtra("UserId", UserId);
					intent.putExtra("push_id", push_id);
					intent.putExtra("strategy_id", strategy_id);
					intent.putExtra("behavior_type", behavior_type);
					intent.putExtra("push_type", push_type);

					startActivity(intent);
					LogUtil.e("tv_launcher", "start notifyActivity");
				} else {
					UiUtils.StartMangGuoEPG(getApplicationContext());
				}

			}
		};
		handler.postDelayed(runnable, duration);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null && runnable != null) {
			handler.removeCallbacks(runnable);
			handler = null;
			runnable = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!subscribe.isUnsubscribed()) {
			subscribe.isUnsubscribed();
		}
	}
}