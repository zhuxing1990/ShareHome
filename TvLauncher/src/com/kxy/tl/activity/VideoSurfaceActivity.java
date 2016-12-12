package com.kxy.tl.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.kxy.ti.util.Constants;
import com.kxy.ti.util.FileManager;
import com.kxy.ti.util.SharedPreferencesUtil;
import com.kxy.ti.util.UIUtil;
import com.kxy.tl.R;

/**
 * 使用MediaPlayer完成播放，同时界面使用SurfaceView来实现
 * 
 * 这里我们实现MediaPlayer中很多状态变化时的监听器
 * 
 * 使用Mediaplayer时，也可以使用MediaController类，但是需要实现MediaController.mediaController接口
 * 实现一些控制方法。
 * 
 * 然后，设置controller.setMediaPlayer(),setAnchorView(),setEnabled(),show()就可以了，
 * 这里不再实现
 * 
 */
public class VideoSurfaceActivity extends Activity implements
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener,
		SurfaceHolder.Callback {
	private Display currDisplay;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private MediaPlayer mplayer;
	private int vWidth, vHeight;

	// private boolean readyToPlay = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.video_surface);

		surfaceView = (SurfaceView) this.findViewById(R.id.video_surface);
		// 给SurfaceView添加CallBack监听
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		// 为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
		// 设置surfaceview不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// 下面开始实例化MediaPlayer对象
		mplayer = new MediaPlayer();
		// MediaPlayer.create(this,
		// Uri.parse("storage/sdcard0/music.mp3"));
		mplayer.setOnCompletionListener(this);
		mplayer.setOnErrorListener(this);
		mplayer.setOnInfoListener(this);
		mplayer.setOnPreparedListener(this);
		mplayer.setOnSeekCompleteListener(this);
		mplayer.setOnVideoSizeChangedListener(this);
		Log.v("Begin:::", "surfaceDestroyed called");
		// 然后指定需要播放文件的路径，初始化MediaPlayer
		String dataPath = FileManager.VideoPath(FileManager.isSdcard(VideoSurfaceActivity.this));

		 if(dataPath == null){
		 UIUtil.sendBroadCast(this, Constants.ADVERTISING_ACTION,new Intent());//节目播放业务
		 this.finish();
		 }

		try {
			mplayer.reset();

//			if (dataPath == null) {
//	            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.stellar);
//	            if (afd == null) 
//	            	return;
//
//	            mplayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//	            afd.close();
//			} else {
				mplayer.setDataSource(dataPath);
//			}
			Log.v("Next:::", "surfaceDestroyed called");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 然后，我们取得当前Display对象
		currDisplay = this.getWindowManager().getDefaultDisplay();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// 当Surface尺寸等参数改变时触发
		Log.v("Surface Change:::", "surfaceChanged called");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mplayer != null) {
			// 当SurfaceView中的Surface被创建的时候被调用
			// 在这里我们指定MediaPlayer在当前的Surface中进行播放
			mplayer.setDisplay(holder);
			// player.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置音乐流的类型

			// 在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
			mplayer.prepareAsync();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mplayer != null && mplayer.isPlaying()) {
			// prosition=mediaPlayer.getCurrentPosition();
			mplayer.stop();
		}
		Log.v("Surface Destory:::", "surfaceDestroyed called");
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
		// 当video大小改变时触发
		// 这个方法在设置player的source后至少触发一次
		Log.v("Video Size Change", "onVideoSizeChanged called");

	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		// seek操作完成时触发
		Log.v("Seek Completion", "onSeekComplete called");

	}

	@Override
	public void onPrepared(MediaPlayer player) {
		// 当prepare完成后，该方法触发，在这里我们播放视频

		// 首先取得video的宽和高
		vWidth = player.getVideoWidth();
		vHeight = player.getVideoHeight();
		Point outSize = new Point();
		currDisplay.getSize(outSize);
		if (vWidth > outSize.x || vHeight > outSize.y) {
			// 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
			float wRatio = (float) vWidth / (float) outSize.x;
			float hRatio = (float) vHeight / (float) outSize.y;

			// 选择大的一个进行缩放
			float ratio = Math.max(wRatio, hRatio);

			vWidth = (int) Math.ceil((float) vWidth / ratio);
			vHeight = (int) Math.ceil((float) vHeight / ratio);

			// 设置surfaceView的布局参数
			surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth,
					vHeight));

		}
		// 然后开始播放视频
		player.start();
	}

	@Override
	public boolean onInfo(MediaPlayer player, int whatInfo, int extra) {
		// 当一些特定信息出现或者警告时触发
		switch (whatInfo) {
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			break;
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			break;
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			break;
		}
		return false;
	}

	@Override
	public boolean onError(MediaPlayer player, int whatError, int extra) {
		Log.v("Play Error:::", "onError called");
		switch (whatError) {
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Log.v("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Log.v("Play Error:::", "MEDIA_ERROR_UNKNOWN");
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		// 当MediaPlayer播放完成后触发
		Log.v("Play Over:::", "onComletion called");
		if (mplayer != null) {
			player.release();
			mplayer = null;
		}
		UIUtil.sendBroadCast(this, Constants.ADVERTISING_ACTION,new Intent());// 节目播放业务
		SharedPreferencesUtil.setBooleanValue(VideoSurfaceActivity.this, SharedPreferencesUtil.IS_PALYED_ADVERT, true);
		this.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mplayer != null) {
			if (mplayer.isPlaying()) {
				mplayer.stop();
			}
			mplayer.release();
			mplayer = null;
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

}