package com.vunke.sharehome.activity;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.huawei.rcs.system.SysApi.PhoneUtils;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.FileUtils;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.ShareSDK_Utils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.FullScreenVideoView;
import com.vunke.sharehome.view.MovieRecorderView;
import com.vunke.sharehome.view.MovieRecorderView.OnRecordFinishListener;
import com.vunke.sharehome.view.RoundProgressBar;

public class VideoRecordActivity extends BaseActivity {
	private Button videorecord_filish;
	private Button videorecord_switch_camera;
	private Button videorecord_startrecord;
	private Button videorecord_resume;
	private Button videorecord_upload;
	private MovieRecorderView videorecord_movie;
	private FullScreenVideoView videorecord_full;
	private RelativeLayout videorecord_layout;
	private RelativeLayout videorecord_title;
	private RoundProgressBar progressBar;
	private int record_time = 0;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_videorecord);
		init();
		initLinstener();
	}

	private void init() {
		videorecord_filish = (Button) findViewById(R.id.videorecord_filish);
		videorecord_switch_camera = (Button) findViewById(R.id.videorecord_switch_camera);
		videorecord_startrecord = (Button) findViewById(R.id.videorecord_startrecord);
		videorecord_resume = (Button) findViewById(R.id.videorecord_resume);
		videorecord_upload = (Button) findViewById(R.id.videorecord_upload_video);
		videorecord_movie = (MovieRecorderView) findViewById(R.id.videorecord_movie);
		videorecord_full = (FullScreenVideoView) findViewById(R.id.videorecord_full);
		videorecord_layout = (RelativeLayout) findViewById(R.id.videorecord_layout);
		videorecord_title = (RelativeLayout) findViewById(R.id.videorecord_title);
		progressBar = (RoundProgressBar) findViewById(R.id.videorecord_progress);
	}

	private void initLinstener() {
		SetOnClickListener(videorecord_filish, videorecord_switch_camera,
				videorecord_resume, videorecord_upload, videorecord_startrecord);
		if (FileUtils.getSDFreeSize() < 50) {
			showToast("温馨提示：您的内存卡空间少于50M");
			WorkLog.e("VideoRecordActivity", "Sdcard 少于50M");
		}
		videorecord_startrecord.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {// 用户按下拍摄按钮
					StartRecord();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {// 用户抬起拍摄按钮
					videorecord_title.setVisibility(View.VISIBLE);
					stopCallTimeTask();
					if (videorecord_movie.getTimeCount() <= 2) {// 判断用户按下时间是否大于3秒
						if (videorecord_movie.mIsRecording) {
							videorecord_movie.stopRecording();
						}
						videorecord_startrecord
								.setBackgroundResource(R.drawable.startrecord1);
						if (videorecord_movie.getmVecordFile() != null)
							videorecord_movie.getmVecordFile().delete();// 删除录制的过短视频
						Toast.makeText(VideoRecordActivity.this, "视频录制时间太短~",
								Toast.LENGTH_SHORT).show();
					} else {
						stopCallTimeTask();
						videorecord_movie.stopRecording();
						videorecord_startrecord
								.setBackgroundResource(R.drawable.startrecord1);
						showSendView();
						videorecord_full.setVideoPath(videorecord_movie
								.getmVecordFile().getAbsolutePath());
						String path = videorecord_movie.getmVecordFile()
								.getAbsolutePath();
						WorkLog.i("VideoRecordActivity", path);
						videorecord_full.start();
						videorecord_full
								.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
									@Override
									public void onPrepared(MediaPlayer mp) {
										mp.start();
										mp.setLooping(true);
									}
								});
						videorecord_full
								.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {
										videorecord_full
												.setVideoPath(videorecord_movie
														.getmVecordFile()
														.getAbsolutePath());
										videorecord_full.start();
									}
								});
					}
				}
				return true;
			}
		});
		// 超过三十秒的时候调用
		videorecord_movie
				.setOnRecordFinishListener(new OnRecordFinishListener() {
					@Override
					public void onRecordFinish() {
						// mShutter.setBackgroundResource(R.drawable.recording_shutter);
						showSendView();
						videorecord_full.setVideoPath(videorecord_movie
								.getmVecordFile().getAbsolutePath());
						videorecord_full.start();
						videorecord_full
								.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
									@Override
									public void onPrepared(MediaPlayer mp) {
										mp.start();
										mp.setLooping(true);
									}
								});
						videorecord_full
								.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {
										videorecord_full
												.setVideoPath(videorecord_movie
														.getmVecordFile()
														.getAbsolutePath());
										videorecord_full.start();
									}
								});
					}
				});
	}

	/**
	 * 开始录制
	 */
	private void StartRecord() {
		videorecord_startrecord.setBackgroundResource(R.drawable.startrecord2);
		if (FileUtils.getSDFreeSize() < 50) {
			showToast("温馨提示：您的内存卡空间不足");
			return;
		}
		showToast("开始录制");
		videorecord_title.setVisibility(View.GONE);
		videorecord_movie.startRecording();
		setVideoProgress();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

		};
	};
	private Timer timer;

	protected void setVideoProgress() {
		timer = new Timer();
		// WorkLog.i("VidoeRecord", ""+initProgress);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {

				handler.post(new Runnable() {
					@Override
					public void run() {
						record_time++;
						progressBar.setProgress(record_time);
					}
				});
			}
		}, 1000, 1000);

	}

	private void stopCallTimeTask() {
		if (timer != null) {
			timer.cancel();
			record_time = 0;
			timer = null;
		}
	}

	/**
	 * 展示录制完成的视频
	 */
	private void showSendView() {
		videorecord_resume.setVisibility(View.VISIBLE);
		videorecord_upload.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		videorecord_startrecord.setVisibility(View.GONE);
		videorecord_full.setVisibility(View.VISIBLE);
		videorecord_movie.setRecoderViewInVisiable();
	}

	/**
	 * 重拍
	 */
	private void showRecodeView() {
		videorecord_resume.setVisibility(View.GONE);
		videorecord_upload.setVisibility(View.GONE);
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);
		videorecord_startrecord.setVisibility(View.VISIBLE);
		videorecord_movie.setRecoderViewVisiable();
		videorecord_full.setVisibility(View.GONE);
		videorecord_full.suspend();
		videorecord_full.stopPlayback();
		// 删除文件
		if (videorecord_movie.getmVecordFile() != null)
			// 删除录制的过短视频
			videorecord_movie.getmVecordFile().delete();
	}

	/**
	 * 重拍
	 */
	private void showRecodeView2() {
		videorecord_resume.setVisibility(View.GONE);
		videorecord_upload.setVisibility(View.GONE);
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);
		videorecord_startrecord.setVisibility(View.VISIBLE);
		videorecord_movie.setRecoderViewVisiable();
		videorecord_full.setVisibility(View.GONE);
		videorecord_full.suspend();
		videorecord_full.stopPlayback();
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.videorecord_filish:
			finish();
			break;
		case R.id.videorecord_switch_camera:
			if(UiUtils.isFastDoubleClick(2000)){
				showToast("切换太频繁");
			}else {
				WorkLog.i("VideoRecordActivity", "switch_camear");
				videorecord_movie.switchCamera();
			}
			break;
		case R.id.videorecord_resume:
			WorkLog.i("VideoRecordActivity", "video_resume");
			showRecodeView();
			break;
		case R.id.videorecord_upload_video:
			WorkLog.i("VideoRecordActivity", "upload_video");
			videorecord_upload.setEnabled(false);
			UploadVideo();
			break;
		default:
			break;
		}
	}

	private String userName;
	private ProgressDialog mypDialog;// 弹窗
	private boolean isUpload = false;
	private void UploadVideo() {
		if (!NetUtils.isNetConnected(mcontext)) {
			UiUtils.showToast("网络出现异常，请检测网络。");
			WorkLog.i("VideoRecordActivity", "网络出现异常,请检测网络");
			videorecord_upload.setEnabled(true);
			return;
		}
		showProgressDiaLog("正在上传...");
	
		if (!videorecord_movie.getmVecordFile().isFile()) {
			showToast("文件不存在" + videorecord_movie.getmVecordFile());
			WorkLog.i("VideoRecordActivity", "文件不存在");
			return;
		}
		userName = UiUtils.GetUserName(mcontext);
		if (TextUtils.isEmpty(userName) || userName.equals("null")) {
			userName = "video";
		}
		isUpload =true;
		OkHttpUtils.post(UrlClient.VIDEO_UPLOAD_URL).tag(VideoRecordActivity.this)
				.readTimeOut(60000).writeTimeOut(60000).connTimeOut(60000)
				.params("fileName", userName)
				.params("file", videorecord_movie.getmVecordFile())
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						WorkLog.i("VideoRecordActivity", "结果" + t);
						try {
							JSONObject js = new JSONObject(t);
							int code = js.getInt("code");
							switch (code) {
							case 200:
								WorkLog.i("VideoRecordActivity", "上传成功");
								showToast("上传成功");
								mypDialog.setMessage("上传成功");
								showRecodeView2();
								break;
							case 400:
								WorkLog.i("VideoRecordActivity", "上传错误");
								mypDialog.setMessage("上传失败");
								UiUtils.showToast("上传失败");
								break;
							case 500:
								WorkLog.i("VideoRecordActivity", "上传错误");
								UiUtils.showToast("上传失败");
								mypDialog.setMessage("上传失败");
								break;

							default:
								break;
							}
						} catch (JSONException e) {
							e.printStackTrace();
							UiUtils.showToast("上传失败");
							videorecord_upload.setEnabled(true);
							isUpload = false;
						}

					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						WorkLog.i("VideoRecordActivity", "上传错误,网络发送异常");
						UiUtils.showToast("上传失败");
						mypDialog.setMessage("上传失败");
						videorecord_upload.setEnabled(true);
						isUpload = false;
					}

					@Override
					public void upProgress(long currentSize, long totalSize,
							float progress, long networkSpeed) {
						super.upProgress(currentSize, totalSize, progress,
								networkSpeed);
						// currentSize 当前上传的大小（单位字节）
						// totalSize 　 需要上传的总大小（单位字节）
						// progress 当前上传的进度，范围　0.0f ~ 1.0f
						// networkSpeed 当前上传的网速（单位秒）
						WorkLog.i("VideoRecordActivity", "当前上传的大小" + totalSize);
						WorkLog.i("VideoRecordActivity", "当前上传的网速"
								+ networkSpeed);
						try {
							if (progress > 0) {
								NumberFormat nt = NumberFormat
										.getPercentInstance();
								// 设置百分数精确度2即保留两位小数
								nt.setMinimumFractionDigits(0);
								nt.format(progress);
								WorkLog.i("VideoRecordActivity",
										"上传进度:" + nt.format(progress));
								mypDialog.setMessage("" + nt.format(progress));
							}
						} catch (Exception e) {
							e.printStackTrace();
							isUpload = false;
							videorecord_upload.setEnabled(true);
						}
					}

					@Override
					public void onAfter(boolean isFromCache,
							@Nullable String t, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						isUpload = false;
						videorecord_upload.setEnabled(true);
						if (mypDialog != null) {
							mypDialog.dismiss();
							mypDialog = null;
						}
					}
				});

	}

	/**
	 * 弹窗
	 * 
	 * @param string
	 */
	public void showProgressDiaLog(String string) {
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
		mypDialog.setCanceledOnTouchOutside(true);
		// 设置ProgressDialog 是否可以按退回按键取消
		mypDialog.show();
		// 让ProgressDialog显示
		mypDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (isUpload != false) {
					OkHttpUtils.getInstance().cancelTag(VideoRecordActivity.this);
					showToast("取消上传");
					videorecord_upload.setEnabled(true);
				}
				
			}
		});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (videorecord_movie != null) {
				videorecord_movie.stopCamera();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
