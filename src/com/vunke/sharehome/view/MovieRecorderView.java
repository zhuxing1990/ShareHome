package com.vunke.sharehome.view;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.vunke.sharehome.R;
import com.vunke.sharehome.utils.SupportedSizesReflect;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class MovieRecorderView extends LinearLayout implements OnErrorListener {
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	private Camera mCamera;
	private MediaRecorder mRecorder;

	private static final String TAG = "mediaRecoder";

	private boolean mIsSufaceCreated = false;
	public boolean mIsRecording = false;

	private int mTimeCount;// 时间计数
	private static final int mRecordMaxTime = 15;// 一次拍摄最长时间
	private File mVecordFile;

	private Handler mHandler = new Handler();

	private OnRecordFinishListener mOnRecordFinishListener;// 录制完成回调接口
	private Context context;
	private OrientationEventListener orient;// 方向监听

	/**
	 * 录制完成回调接口
	 */
	public interface OnRecordFinishListener {
		public void onRecordFinish();
	}

	public MovieRecorderView(Context context) {
		super(context);
		init(context);
		this.context = context;
	}

	public MovieRecorderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		this.context = context;
	}

	@SuppressLint("NewApi")
	public MovieRecorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		this.context = context;
	}

	@Override
	public void onError(MediaRecorder arg0, int arg1, int arg2) {

	}

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.video_recoder, this);
		mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(customCallBack);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceView.setFocusable(true);
		orient = new OrientationEventListener(context) {

			@Override
			public void onOrientationChanged(int orientation) {
				// String showorient = null;
				if (orientation < 45 || orientation >= 315) {
					// showorient = "bottom";
					WorkLog.i("MovieRecorderView", "bottom");
				} else if (orientation >= 45 && orientation < 135) {
					// showorient = "right";
					WorkLog.i("MovieRecorderView", "right");
				} else if (orientation >= 135 && orientation < 225) {
					// showorient = "top";
					WorkLog.i("MovieRecorderView", "top");
				} else if (orientation >= 225 && orientation < 315) {
					// showorient = "left";
					WorkLog.i("MovieRecorderView", "left");
				}
			}
		};
	}

	private SurfaceHolder.Callback customCallBack = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mIsSufaceCreated = false;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mIsSufaceCreated = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			startCamera();
		}
	};

	// 启动预览
	public void startCamera() {
		// 保证只有一个Camera对象
		if (mCamera != null || !mIsSufaceCreated) {
			WorkLog.d(TAG, "startPreview will return");
			return;
		}
		mCamera = Camera.open();
		Parameters parameters = mCamera.getParameters();
		parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		parameters.setPreviewFrameRate(20);
		// 设置相机预览方向
		mCamera.setDisplayOrientation(90);
		mCamera.setParameters(parameters);
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			// mCamera.setPreviewCallback(mPreviewCallback);
		} catch (Exception e) {
			WorkLog.d(TAG, e.getMessage());
		}
		mCamera.startPreview();
	}

	private int cameraCount = 0;
	private int cameraPosition = 1;// 0代表前置摄像头，1代表后置摄像头

	public void switchCamera() {
		cameraCount = Camera.getNumberOfCameras();
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

		for (int i = 0; i < cameraCount; i++) {

			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
			if (cameraPosition == 1) {
				// 现在是后置，变更为前置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 摄像头方位
																					// CAMERA_FACING_FRONT前置
																					// CAMERA_FACING_BACK后置
					mCamera.stopPreview();// 停掉原来摄像头的预览
					mCamera.release();// 释放资源
					mCamera = null;// 取消原来摄像头
					mCamera = Camera.open(i);// 打开当前选中的摄像头
					try {
						deal(mCamera);
						mCamera.setPreviewDisplay(mSurfaceHolder);// 通过surfaceview显示取景画面
					} catch (IOException e) {
						e.printStackTrace();
					}
					mCamera.startPreview();// 开始预览
					cameraPosition = 0;
					break;
				}
			} else {
				// 现在是前置， 变更为后置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
																				// CAMERA_FACING_BACK后置
					mCamera.stopPreview();// 停掉原来摄像头的预览
					mCamera.release();// 释放资源
					mCamera = null;// 取消原来摄像头
					mCamera = Camera.open(i);// 打开当前选中的摄像头
					try {
						deal(mCamera);
						mCamera.setPreviewDisplay(mSurfaceHolder);// 通过surfaceview显示取景画面
					} catch (IOException e) {
						e.printStackTrace();
					}
					mCamera.startPreview();// 开始预览
					cameraPosition = 1;
					break;
				}
			}

		}

	}

	public Camera deal(Camera camera) {
		// 设置camera预览的角度，因为默认图片是倾斜90度的
		camera.setDisplayOrientation(90);

		Size pictureSize = null;
		Size previewSize = null;
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewFrameRate(5);
		// 设置旋转代码
		parameters.setRotation(90);
		// parameters.setPictureFormat(PixelFormat.JPEG);

		List<Size> supportedPictureSizes = SupportedSizesReflect
				.getSupportedPictureSizes(parameters);
		List<Size> supportedPreviewSizes = SupportedSizesReflect
				.getSupportedPreviewSizes(parameters);

		if (supportedPictureSizes != null && supportedPreviewSizes != null
				&& supportedPictureSizes.size() > 0
				&& supportedPreviewSizes.size() > 0) {

			// 2.x
			pictureSize = supportedPictureSizes.get(0);

			int maxSize = 1280;
			if (maxSize > 0) {
				for (Size size : supportedPictureSizes) {
					if (maxSize >= Math.max(size.width, size.height)) {
						pictureSize = size;
						break;
					}
				}
			}

			WindowManager windowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = windowManager.getDefaultDisplay();
			DisplayMetrics displayMetrics = new DisplayMetrics();
			display.getMetrics(displayMetrics);

			previewSize = getOptimalPreviewSize(supportedPreviewSizes,
					display.getWidth(), display.getHeight());

			parameters.setPictureSize(pictureSize.width, pictureSize.height);
			parameters.setPreviewSize(previewSize.width, previewSize.height);

		}
		camera.setParameters(parameters);
		return camera;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void stopCamera() {
		// 释放Camera对象
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(null);
			} catch (Exception e) {
				WorkLog.i(TAG, e.getMessage());
			}
			mCamera.stopPreview();
			mCamera.lock();
			mCamera.release();
			mCamera = null;
		}
	}

	public void setRecoderViewInVisiable() {
		// stopCamera();
		mCamera.stopPreview();
		mSurfaceView.setVisibility(View.GONE);
	}

	public void setRecoderViewVisiable() {
		mSurfaceView.setVisibility(View.VISIBLE);
		// startCamera();
		// mCamera = Camera.open();
		Parameters parameters = mCamera.getParameters();
		parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		parameters.setPreviewFrameRate(20);
		// 设置相机预览方向
		mCamera.setDisplayOrientation(90);
		mCamera.setParameters(parameters);
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			// mCamera.setPreviewCallback(mPreviewCallback);
		} catch (Exception e) {
			WorkLog.d(TAG, e.getMessage());
		}
		mCamera.startPreview();
	}

	@SuppressLint("NewApi")
	private void initMediaRecorder() {
		mRecorder = new MediaRecorder();// 实例化
		mCamera.setDisplayOrientation(90);
		if (cameraPosition == 1) {
			mRecorder.setOrientationHint(90);// 视频旋转90度
		} else {
			mRecorder.setOrientationHint(270);// 视频旋转270度
		}
		mCamera.unlock();
		// 给Recorder设置Camera对象，保证录像跟预览的方向保持一致
		mRecorder.setCamera(mCamera);
		// mRecorder.setOrientationHint(90); //
		// 改变保存后的视频文件播放时是否横屏(不加这句，视频文件播放的时候角度是反的)
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置从麦克风采集声音
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 设置从摄像头采集图像
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 设置视频的输出格式
																		// MP4
		// mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //
		// 设置视频的输出格式 3GP

		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // 设置音频的编码格式
		mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // 设置视频的编码格式
		// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
		mRecorder.setVideoSize(640, 480); // 设置视频大小
		mRecorder.setVideoFrameRate(20); // 设置帧率
		// mRecorder.setMaxDuration(10000); //设置最大录像时间为10s
		mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

		// 设置视频存储路径
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "video/");// 录制视频的保存地址
		if (!file.exists()) {
			// 多级文件夹的创建
			file.mkdirs();
		}

		File vecordDir = file;
		// 创建文件
		try {
			String name = UiUtils.GetUserName(context).substring(1);
			// mVecordFile = new
			// File(vecordDir+File.separator+name+"-"+UiUtils.getDateTime2()+".3gp");//
			// 3gp格式的录制的视频文件
			mVecordFile = new File(vecordDir + File.separator + name + "-"
					+ UiUtils.getDateTime2() + ".mp4");// mp4格式的录制的视频文件
			String a = "1" + mVecordFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		mRecorder.setOutputFile(mVecordFile.getAbsolutePath());
	}

	public void startRecording() {
		initMediaRecorder();
		if (mRecorder != null) {
			try {
				mRecorder.prepare();
				mRecorder.start();
			} catch (Exception e) {
				mIsRecording = false;
				WorkLog.i(TAG, e.getMessage());
			}
		}
		mTimeCount = 0;
		// 开始录像后，每隔1s去更新录像的时间戳
		mHandler.postDelayed(mTimestampRunnable, 1000);
		mIsRecording = true;
	}

	public void stopRecording() {
		if (mCamera != null) {
			mCamera.lock();
		}
		if (mRecorder != null) {
			// 设置后不会崩
			mRecorder.setOnErrorListener(null);
			mRecorder.setPreviewDisplay(null);
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		mIsRecording = false;
		mHandler.removeCallbacks(mTimestampRunnable);
		// 重启照相机
		startCamera();
	}

	private Runnable mTimestampRunnable = new Runnable() {
		@Override
		public void run() {
			updateTimestamp();
			mHandler.postDelayed(this, 1000);
		}
	};

	public int getTimeCount() {
		return mTimeCount;
	}

	// 返回录制的视频文件
	public File getmVecordFile() {
		return mVecordFile;
	}

	private void updateTimestamp() {
		mTimeCount++;
		if (mTimeCount == mRecordMaxTime) {// 达到指定时间，停止拍摄
			stopRecording();
			if (mOnRecordFinishListener != null)
				mOnRecordFinishListener.onRecordFinish();
		}
	}

	public void setOnRecordFinishListener(
			OnRecordFinishListener onRecordFinishListener) {
		mOnRecordFinishListener = onRecordFinishListener;
	}
}
