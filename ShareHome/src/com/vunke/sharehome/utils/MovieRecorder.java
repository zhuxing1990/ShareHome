package com.vunke.sharehome.utils;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.view.SurfaceView;

public class MovieRecorder {
	private MediaRecorder mediarecorder;
	boolean isRecording;

	public void startRecording(SurfaceView surfaceView) {
		mediarecorder = new MediaRecorder();// 创建mediarecorder对象
		// 设置录制视频源为Camera(相机)
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
		mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// 设置录制的视频编码h263 h264
		mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoSize(176, 144);
		// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoFrameRate(20);
		mediarecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
		// 设置视频文件输出的路径
		lastFileName = newFileName();
		mediarecorder.setOutputFile(lastFileName);
		try {
			// 准备录制
			mediarecorder.prepare();
			// 开始录制
			mediarecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isRecording = true;
		timeSize = 0;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				timeSize++;
			}
		}, 0, 1000);
	}

	Timer timer;
	int timeSize = 0;
	private String lastFileName;
	private MediaPlayer mPlayer;

	public void stopRecording() {
		if (mediarecorder != null) {
			// 停止
			mediarecorder.stop();
			mediarecorder.release();
			mediarecorder = null;
			timer.cancel();
			if (null != lastFileName && !"".equals(lastFileName)) {
				File f = new File(lastFileName);
				String name = f.getName().substring(0,
						f.getName().lastIndexOf(".3gp"));
				name += "_" + timeSize + "s.3gp";
				String newPath = f.getParentFile().getAbsolutePath() + "/"
						+ name;
				if (f.renameTo(new File(newPath))) {
					int i = 0;
					i++;
				}
			}
		}
	}

	public String newFileName() {
		File file = new File("/sdcard/text.3gp");
		return file.getAbsolutePath();
	}

	public void release() {
		if (mediarecorder != null) {
			// 停止
			mediarecorder.stop();
			mediarecorder.release();
			mediarecorder = null;
		}
	}

	public void play(String fileName, SurfaceView view) {
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setDisplay(view.getHolder()); // 定义一个SurfaceView播放它
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				stop();
				// canvas.drawColor(Color.TRANSPARENT,
				// PorterDuff.Mode.CLEAR);
			}
		});
		try {
			mPlayer.setDataSource(fileName);
			mPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPlayer.start();
	}

	// 结束播放时：
	public void stop() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
