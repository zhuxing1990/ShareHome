package com.vunke.sharehome.activity;

import java.io.IOException;

import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.utils.WorkLog;

public class VideoViewActivity extends BaseActivity implements
		OnBufferingUpdateListener, OnCompletionListener,
		MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {

	private String path;
	private MediaPlayer mediaPlayer;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private int videoWidth;
	private int videoHeight;
	
	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_videoview);
		getData();
		init();
	}

	private void getData() {
		intent = getIntent();
		if (intent.hasExtra("path")) {
			path = intent.getStringExtra("path");
		}
	}

	private void init() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 强制为横屏
		this.surfaceView = (SurfaceView) this.findViewById(R.id.videoview_surface);
		this.surfaceHolder = this.surfaceView.getHolder();
		this.surfaceHolder.addCallback(this);
		this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void playVideo() throws IllegalArgumentException,
			IllegalStateException, IOException {
		this.mediaPlayer = new MediaPlayer();
		this.mediaPlayer.setDataSource(path);
		this.mediaPlayer.setDisplay(this.surfaceHolder);
		this.mediaPlayer.prepare();
		this.mediaPlayer.setOnBufferingUpdateListener(this);
		this.mediaPlayer.setOnPreparedListener(this);
		this.mediaPlayer.setOnCompletionListener(this);
		this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		WorkLog.i("mplayer", ">>>play video");
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		WorkLog.i("cat", ">>>surface changed");

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			this.playVideo();
		} catch (Exception e) {
			WorkLog.i("cat", ">>>error", e);
		}
		WorkLog.i("cat", ">>>surface created");

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		WorkLog.i("mplayer", ">>>surface destroyed");

	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		WorkLog.i("mplayer", ">>onCompletion");
		finish();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {

	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		this.videoWidth = this.mediaPlayer.getVideoWidth();
		this.videoHeight = this.mediaPlayer.getVideoHeight();

		if (this.videoHeight != 0 && this.videoWidth != 0) {
			this.surfaceHolder.setFixedSize(this.videoWidth, this.videoHeight);
			this.mediaPlayer.start();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (this.mediaPlayer != null) {
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
	}

	@Override
	public void OnClick(View v) {

	}

}
