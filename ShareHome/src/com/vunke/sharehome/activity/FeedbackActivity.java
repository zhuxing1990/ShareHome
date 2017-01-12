package com.vunke.sharehome.activity;

import java.io.File;
import java.text.NumberFormat;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.message.T;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.FileUtils;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.utils.ZipUtils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.AndroidCharacter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 反馈Activity
 */
public class FeedbackActivity extends BaseActivity {
	private Button feedback_back, feedback_TOS, feedback_commit;// 返回 服务条款 提交
	private CheckBox feedback_checkbox;// 复选框
	private ProgressDialog dialog;// 弹窗
	private boolean isAlive = true;// 用来关闭线程的
	private Thread thread;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_feedback);
		init();
		initListener();
		// registerReceiver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		// OkHttpUtils.getInstance().cancelTag(this);
		if (dialog != null) {
			dialog.cancel();
			dialog = null;
		}
	}

	/**
	 * 注册广播
	 */
	private void registerReceiver() {
		IntentFilter requestFilter = new IntentFilter(LogApi.EVENT_LOG_UPLOAD_REQUEST);
		IntentFilter resultFilter = new IntentFilter(LogApi.EVENT_LOG_UPLOAD_RESULT);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, resultFilter);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, requestFilter);
	}

	/**
	 * 初始化监听事件
	 */
	private void initListener() {
		SetOnClickListener(feedback_back, feedback_TOS, feedback_commit);
		feedback_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				feedback_commit.setEnabled(b);
				feedback_commit
						.setBackgroundResource(b ? R.drawable.button_login_shape : R.drawable.button_login_shape2);
			}
		});
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		feedback_back = (Button) findViewById(R.id.feedback_back);
		feedback_TOS = (Button) findViewById(R.id.feedback_TOS);
		feedback_commit = (Button) findViewById(R.id.feedback_commit);
		feedback_checkbox = (CheckBox) findViewById(R.id.feedback_checkbox);
	}

	private void initRx() {
		File zipFile = new File(Environment.getExternalStorageDirectory(), "/hrslog/hrslog.zip");
		if (zipFile.exists()) {
			zipFile.delete();
		}
		File file = new File(Environment.getExternalStorageDirectory(), "/hrslog");
		// WorkLog.i("FeedbackActivity", "文件路径" +
		// file.getPath());
		if (!file.exists()) {
			WorkLog.i("FeedbackActivity", "getFlie is null");
			return;
		}
		Collection<File> fileList = FileUtils.getFileListOnSys(file);
		Observable.from(fileList).flatMap(new Func1<File, Observable<File>>() {

			@Override
			public Observable<File> call(File f) {
				
				return Observable.from(f.listFiles());
			}

		}).filter(new Func1<File, Boolean>() {

			@Override
			public Boolean call(File f) {

				return null;
			}
		}).map(new Func1<File, Bitmap>() {

			@Override
			public Bitmap call(File f) {
				// TODO Auto-generated method stub
				return null;
			}

		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {

			@Override
			public void call(Bitmap arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.feedback_back:
			finish();
			break;
		case R.id.feedback_TOS:// 服务条款
//			Config.intent = new Intent(mcontext, ServiceTermsActivity.class);
//			startActivity(Config.intent);
			break;
		case R.id.feedback_commit:
			feedback_checkbox.setChecked(false);
			// LogApi.d("ShareHome", "invoke onClick_uploadLog");
			// LogApi.uploadLog(1024 * 512);
			dialog = new ProgressDialog(this);
			dialog.setTitle("上传日志");
			dialog.setMessage("正在上传，请稍后...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
			thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {

						File zipFile = new File(Environment.getExternalStorageDirectory(), "/hrslog/hrslog.zip");
						if (zipFile.exists()) {
							zipFile.delete();
						}
						File file = new File(Environment.getExternalStorageDirectory(), "/hrslog");
						// WorkLog.i("FeedbackActivity", "文件路径" +
						// file.getPath());
						if (!file.exists()) {
							WorkLog.i("FeedbackActivity", "getFlie is null");
							return;
						}
						Collection<File> fileList = FileUtils.getFileListOnSys(file);
						if (fileList.isEmpty()) {
							WorkLog.i("FeedbackActivity", "getFlieList is null");
							return;
						}
						ZipUtils.zipFiles(fileList, zipFile);
						if (!zipFile.exists()) {
							WorkLog.i("FeedbackActivity", "getzipFlie is null");
							return;
						}
						UploadLog(zipFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			thread.start(); 
			break;
		default:
			break;
		}

	}

	/**
	 * 上传压缩文件到服务器
	 * 
	 * @param zipFile
	 */
	public void UploadLog(final File zipFile) {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.UploadFile).tag(this).params("userName", UiUtils.GetUserName(mcontext))
				.writeTimeOut(30000).params("txt", zipFile).execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t, Request request,
							@Nullable Response response) {
						// isFromCache 表示当前回调是否来自于缓存
						 WorkLog.i("FeedbackActivity", "结果" + t);
						try {
							JSONObject js = new JSONObject(t);
							int code = js.getInt("code");
							switch (code) {
							case 200:
								WorkLog.i("FeedbackActivity", "上传成功");
								dialog.setMessage("上传成功啦");
								break;
							case 400:
								WorkLog.i("FeedbackActivity", "上传错误" + js.getString("message"));
								dialog.setMessage("上传失败");
								break;
							case 500:
								WorkLog.i("FeedbackActivity", "上传错误");
								dialog.setMessage("上传失败" + js.getString("message"));
								break;

							default:
								break;
							}
						} catch (JSONException e) {

							e.printStackTrace();
						}
						if (dialog != null) {
							dialog.dismiss();
						}
					}

					@Override
					public void onError(boolean isFromCache, Call call, @Nullable Response response,
							@Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						WorkLog.i("FeedbackActivity", "上传错误,网络发送异常");
						dialog.setMessage("上传失败");
						if (dialog != null) {
							dialog.dismiss();
						}
					}

					@Override
					public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
						super.upProgress(currentSize, totalSize, progress, networkSpeed);
						// dialog.setMessage(networkSpeed + "/s");
						try {
							if (progress>0) {
								NumberFormat nt = NumberFormat.getPercentInstance();
								// 设置百分数精确度2即保留两位小数
								nt.setMinimumFractionDigits(0);
								dialog.setMessage(nt.format(progress));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						// WorkLog.i("FeedbackActivity",nt.format(progress));
					}
					@Override
					public void onAfter(boolean isFromCache, String t, Call call, Response response, Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						if (zipFile.exists()) {
							zipFile.delete();
						}
					}
				});
			
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogApi.e("ShareHome", "log receive broadcast");
			if (dialog != null) {
				dialog.dismiss();
			}
			int upload_result = intent.getIntExtra(LogApi.PARAM_LOG_UPLOAD_RESULT, -1);
			switch (upload_result) {
			case LogApi.RESULT_SUCCESS:// logs are uploaded successfully
				showToast("上传成功");
				break;
			case LogApi.RESULT_FAILED:// logs fail to be uploaded
				showToast("上传失败");
				break;
			case LogApi.RESULT_TIMEOUT:// timeout
				showToast("请求超时");
				break;
			}
			if (LogApi.EVENT_LOG_UPLOAD_REQUEST.equalsIgnoreCase(intent.getAction())) {
				Toast.makeText(mcontext, "接收请求上传日志", Toast.LENGTH_SHORT).show();
			} else if (LogApi.EVENT_LOG_UPLOAD_RESULT.equalsIgnoreCase(intent.getAction())) {
				Toast.makeText(mcontext, "上传日志结果:" + intent.getIntExtra(LogApi.PARAM_LOG_UPLOAD_RESULT, -1),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mcontext, "未知的异常", Toast.LENGTH_SHORT).show();
			}
		}
	};

}
