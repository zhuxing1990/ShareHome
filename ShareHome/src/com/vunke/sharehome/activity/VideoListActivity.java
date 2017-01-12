package com.vunke.sharehome.activity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.adapter.VideoListAdapter;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.model.VideoBean;
import com.vunke.sharehome.utils.FileUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

@SuppressLint("NewApi")
public class VideoListActivity extends BaseActivity {

	private ListView videolist_listview;
	private Button videolist_back;
	private Button videolist_startrecorder;
	private Button videolist_upload;
	private Button videolist_delete;
	private Button videolist_share;
	private LinearLayout videolist_funcation;
	private AlertDialog alertDialog;
	private VideoListAdapter adapter;
	private VideoBean videoBean;
	private List<VideoBean> list;
	private TextView videolist_loading;
	private MediaMetadataRetriever mmr;
	private Subscription subscribe;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_videolist);
		init();
		initListener();
	}
	private void init() {
		videolist_listview = (ListView) findViewById(R.id.videolist_listview);
		videolist_back = (Button) findViewById(R.id.videolist_back);
		videolist_startrecorder = (Button) findViewById(R.id.videolist_startrecorder);
		videolist_upload = (Button) findViewById(R.id.videolist_upload);
		videolist_delete = (Button) findViewById(R.id.videolist_delete);
		videolist_share = (Button) findViewById(R.id.videolist_share);
		videolist_funcation = (LinearLayout) findViewById(R.id.videolist_funcation);
		videolist_loading = (TextView) findViewById(R.id.videolist_loading);
	}
	private int itemPosition = -1;
	private void initListener() {
		SetOnClickListener(videolist_back, videolist_startrecorder);
		videolist_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				WorkLog.i("VideoListActivity", "点击ITME");
				videolist_funcation.setVisibility(View.VISIBLE);
				itemPosition = position;

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		initRX();
	}

	private void initRX() {
		list = new ArrayList<>();
		subscribe = Observable.create(new OnSubscribe<VideoBean>() {

			@Override
			public void call(Subscriber<? super VideoBean> subscriber) {
				try {
					File videofiles = new File(Environment.getExternalStorageDirectory()
							+ File.separator + "video/");
					File[] f = FileUtils.getVideoFile(videofiles);
					if (f != null) {
						for (int i = f.length; i > 0; i--) {
							videoBean = new VideoBean();
							int a = i - 1;
							File file = f[a];
							mmr = new MediaMetadataRetriever();
							// 在这个类的其他方法之前调用此方法.。这种方法可能是耗时的。
							mmr.setDataSource(file.getPath());
							videoBean.setFileType(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
							if (videoBean.getFileType().startsWith("video/")) {
								videoBean.setFileName(file.getName());
								videoBean.setFilePath(file.getPath());
								videoBean.setFileBitmap(ThumbnailUtils.createVideoThumbnail(file.getPath(),Thumbnails.MINI_KIND));
								videoBean.setFileVideoTime(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)); 
								videoBean.setFileCreateTime(UiUtils.UTCtoLocalDate(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)));
								subscriber.onNext(videoBean);
							}	
						}
						subscriber.onCompleted();
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}).filter(new Func1<VideoBean, Boolean>() {

			@Override
			public Boolean call(VideoBean bean) {
				return bean.getFileType().startsWith("video/");
			}
		})		
		.subscribeOn(Schedulers.io())
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe(new Subscriber<VideoBean>() {

			@Override
			public void onCompleted() {
				WorkLog.i("VideoListActivity", "onCompleted");
				if (list!=null&&list.size()!=0) {
					 adapter = new VideoListAdapter(mcontext, list,
					 videolist_funcation, videolist_share, videolist_delete,
					 videolist_upload);
					 videolist_listview.setAdapter(adapter);
					 videolist_loading.setVisibility(View.GONE);
				}else {
					videolist_loading.setText("您还没有录制视频，请点击右上角开始录制来录制您的视频吧");
				}
			}

			@Override
			public void onError(Throwable arg0) {
				WorkLog.e("VideoListActivity", "获取文件信息失败");
				subscribe.isUnsubscribed();
			}

			@Override
			public void onNext(VideoBean bean) {
				WorkLog.i("VideoListActivity", bean.toString());
				list.add(bean);
			}		

		});
	}
	
	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.videolist_back:
			finish();
			break;
		case R.id.videolist_startrecorder:
			if (UiUtils.isFastDoubleClick(2000)) {
				return;
			}
			if (!UiUtils.isCameraCanUse()) {
				NoPermission();
				break;
			}
			Config.intent = new Intent(VideoListActivity.this,
					VideoRecordActivity.class);
			startActivity(Config.intent);
			break;
		// case R.id.videolist_delete:
		// if (itemPosition!=-1&& adapter.getCount()>itemPosition) {
		// list.remove(itemPosition);
		// adapter.notifyDataSetChanged();
		// File file = new File(list.get(itemPosition).getFilePath());
		// if (file.exists()) {
		// file.delete();
		// }
		// }
		// break;
		// case R.id.videolist_share:
		// if (itemPosition!=-1&& adapter.getCount()>itemPosition) {
		// File file = new File(list.get(itemPosition).getFilePath());
		// if (file.exists()) {
		//
		// }
		// }
		// break;
		// case R.id.videolist_loading:
		//
		// break;
		default:
			break;
		}
	}

	/**
	 * 提示 摄像头不可用
	 * */
	public void NoPermission() {
		Builder builder = new AlertDialog.Builder(VideoListActivity.this);
		builder.setTitle("温馨提示");
		builder.setMessage("想家没有权限打开你的摄像头 \n建议设置如下:\n1、请到“设置 - 权限管理”中打开想家权限\n2、其他应用程序正在占用摄像头,请先将摄像头关闭");

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});
		builder.setCancelable(false);
		alertDialog = builder.create();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!subscribe.isUnsubscribed()) {
			subscribe.isUnsubscribed();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}

}
