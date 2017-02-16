package com.vunke.sharehome.adapter;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.huawei.rcs.system.SysApi.PhoneUtils;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.VideoListActivity;
import com.vunke.sharehome.activity.VideoViewActivity;
import com.vunke.sharehome.crop.Crop;
import com.vunke.sharehome.model.VideoBean;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.ShareSDK_Utils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.ActionSheetDialog;

@SuppressLint("ResourceAsColor")
public class VideoListAdapter extends BaseAdapter implements OnClickListener {
	private Activity context;
	private List<VideoBean> list;
	private LinearLayout videolist_funcation;
	private Button videolist_upload, videolist_delete, videolist_share;
	private ProgressDialog mypDialog;// 弹窗

	public VideoListAdapter(Activity context, List<VideoBean> list,
			LinearLayout videolist_funcation, Button videolist_share,
			Button videolist_delete, Button videolist_upload) {
		this.context = context;
		this.list = list;
		this.videolist_funcation = videolist_funcation;
		videolist_delete.setOnClickListener(this);
		videolist_upload.setOnClickListener(this);
		videolist_share.setOnClickListener(this);
		this.videolist_delete = videolist_delete;
		this.videolist_upload = videolist_upload;
		this.videolist_share = videolist_share;

	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		final ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = View.inflate(context, R.layout.listview_videolist, null);
			holder.videolist_time = (TextView) view
					.findViewById(R.id.videolist_time);
			holder.videolist_videotime = (TextView) view
					.findViewById(R.id.videolist_videotime);
			holder.videolist_playvideo = (Button) view
					.findViewById(R.id.videolist_playvideo);
			holder.videolist_img = (ImageView) view
					.findViewById(R.id.videolist_img);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final VideoBean data = list.get(position);
		try {
			int videoTime = Integer.valueOf(data.getFileVideoTime());// data.getFileVideoTime();
			holder.videolist_videotime.setText(PhoneUtils
					.getCallDurationTime(videoTime / 1000));

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		try {
			if (data.getFileBitmap() != null) {
				holder.videolist_img.setImageBitmap(data.getFileBitmap());
				if (data.isSelect() == true) {
					holder.videolist_img.setColorFilter(Color
							.parseColor("#6010a8ab"));
				}else {
					holder.videolist_img.setColorFilter(Color
							.parseColor("#00000000"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			holder.videolist_time.setText(data.getFileCreateTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		holder.videolist_playvideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (!TextUtils.isEmpty(data.getFilePath())) {
						if (!TextUtils.isEmpty(data.getFileType())) {
							if (data.getFileType().startsWith("video/")){
								Config.intent = new Intent(context,
										VideoViewActivity.class);
								Config.intent.putExtra("path", data.getFilePath());
								Config.intent
										.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(Config.intent);
							}else {
								Toast.makeText(context, "无法播放该类型文件", Toast.LENGTH_SHORT).show();
							}
						} else {
							WorkLog.i("VideoListAdapter", "获取文件类型失败");
						}
					} else {
						WorkLog.i("VideoListAdapter", "获取文件路径失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		holder.videolist_img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WorkLog.i("VideoListActivity", "点击了："+ position);
				list.get(position).setSelect(!list.get(position).isSelect());
				if (ItemPosition == -1) {
					ItemPosition = position;
				}else {
					if (ItemPosition != position) {
						list.get(ItemPosition).setSelect(false);
					}
					ItemPosition = position;
				}
				notifyDataSetChanged();
				if (list.get(position).isSelect() == true) {
					videolist_funcation.setVisibility(View.VISIBLE);
				}else {
					videolist_funcation.setVisibility(View.GONE);
				}
			}
		});
		return view;
	}

	private int ItemPosition = -1;

	class ViewHolder {
		public TextView videolist_time, videolist_videotime;
		public ImageView videolist_img;
		public Button videolist_playvideo;
		public LinearLayout videolist_layout;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.videolist_delete:
			DeleteFile();
			break;
		case R.id.videolist_upload:
			videolist_upload.setEnabled(false);
			UploadFile(false);
			break;
		case R.id.videolist_share:
			videolist_share.setEnabled(false);
			UploadFile(true);
			break;
		default:
			break;
		}
	}

	private String fileName;
	private String userName;

	private void UploadFile(final boolean isShare) {
		if (!NetUtils.isNetConnected(context)) {
			UiUtils.showToast("网络出现异常，请检测网络。");
			WorkLog.i("videoListAdpater", "网络出现异常,请检测网络");
			videolist_share.setEnabled(true);
			videolist_upload.setEnabled(true);
			return;
		}
		if (ItemPosition > -1) {
			String filePath = list.get(ItemPosition).getFilePath();
			File file = new File(filePath);
			WorkLog.i("VideoListAdapter", "filePath" + filePath);
			if (!file.exists()) {
				UiUtils.showToast("文件不存在");
				this.notifyDataSetChanged();
				return;
			}

			userName = UiUtils.GetUserName(context);
			if (TextUtils.isEmpty(userName) | userName.equals("null")) {
				userName = "video";
			}
			showProgressDiaLog("上传中...");
			isUpload = true;
			OkHttpUtils.post(UrlClient.VIDEO_UPLOAD_URL)
					.tag(VideoListAdapter.this).readTimeOut(60000)
					.writeTimeOut(60000).connTimeOut(60000)
					.params("fileName", userName).params("file", file)
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
									fileName = list.get(ItemPosition)
											.getFileName();
									UiUtils.showToast("上传成功");
									if (isShare == true) {
										showShare();
									}
									break;
								case 400:
									WorkLog.i("VideoRecordActivity", "上传错误");
									UiUtils.showToast("上传失败");
									break;
								case 500:
									WorkLog.i("VideoRecordActivity", "上传错误");
									UiUtils.showToast("上传失败");
									break;

								default:
									break;
								}
							} catch (JSONException e) {
								e.printStackTrace();
								UiUtils.showToast("上传失败");
								isUpload = false;
							}

						}

						@Override
						public void onError(boolean isFromCache, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onError(isFromCache, call, response, e);
							WorkLog.i("VideoRecordActivity", "上传错误,网络发送异常");
							UiUtils.showToast("上传失败");
							videolist_upload.setEnabled(true);
							videolist_share.setEnabled(true);
							isUpload = false;
						}

						@Override
						public void upProgress(long currentSize,
								long totalSize, float progress,
								long networkSpeed) {
							super.upProgress(currentSize, totalSize, progress,
									networkSpeed);
							// currentSize 当前上传的大小（单位字节）
							// totalSize 　 需要上传的总大小（单位字节）
							// progress 当前上传的进度，范围　0.0f ~ 1.0f
							// networkSpeed 当前上传的网速（单位秒）
							WorkLog.i("VideoRecordActivity", "当前上传的大小"
									+ totalSize);
							WorkLog.i("VideoRecordActivity", "当前上传的网速"
									+ networkSpeed);
							try {
								if (progress > 0) {
									NumberFormat nt = NumberFormat
											.getPercentInstance();
									// 设置百分数精确度2即保留两位小数
									nt.setMinimumFractionDigits(0);
									nt.format(progress);
									WorkLog.i("VideoRecordActivity", "上传进度:"
											+ nt.format(progress));
									mypDialog.setMessage(nt.format(progress));
								}
							} catch (Exception e) {
								e.printStackTrace();
								isUpload = false;
								videolist_upload.setEnabled(true);
								videolist_share.setEnabled(true);
							}
						}

						@Override
						public void onAfter(boolean isFromCache,
								@Nullable String t, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onAfter(isFromCache, t, call, response, e);
							videolist_upload.setEnabled(true);
							videolist_share.setEnabled(true);
							isUpload = false;
							if (mypDialog != null) {
								mypDialog.dismiss();
								mypDialog = null;
							}
						}
					});
		}

	}

	private void DeleteFile() {
		WorkLog.i("VideoListAdapter", "DelectFile");
		try {
			new ActionSheetDialog(context)
					.builder()
					.setTitle("您确定要删除吗?")
					.setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("确定", ActionSheetDialog.SheetItemColor.Blue,
							new ActionSheetDialog.OnSheetItemClickListener() {
								@Override
								public void onClick(int which) {
									if (ItemPosition > -1) {
										String filePath = list
												.get(ItemPosition)
												.getFilePath();
										WorkLog.i("VideoListAdapter",
												"filePath" + filePath);
										File file = new File(filePath);
										if (file.exists()) {
											file.delete();
											list.remove(ItemPosition);
											videolist_funcation
													.setVisibility(View.GONE);
											notifyDataSetChanged();
										} else {
											WorkLog.i("VideoListAdapter",
													"文件不存在");
											notifyDataSetChanged();
										}
									}
								}
							}).show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showShare() {
		// String path = "/"+userName+"/"+fileName;
		String path = UrlClient.VidepUploadURL + userName + "/" + fileName;
		// String path = UrlClient.VidepUploadURL3 + userName + "/" + fileName;
		WorkLog.i("VideoRecordActivity", "path:" + path);
		// String ShareUrl = UrlClient.VideoPlayerURL+"?path="+'"'+path+'"';
		String ShareUrl = UrlClient.VideoPlayerURL + "?path=" + path;

		WorkLog.i("VideoRecordActivity", "ShareUrl:" + ShareUrl);

		ShareSDK_Utils.Share(context, ShareUrl, "想家视频电话:");
	}

	private boolean isUpload = false;

	/**
	 * 弹窗
	 * 
	 * @param string
	 */
	public void showProgressDiaLog(String string) {
		mypDialog = new ProgressDialog(context);
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
					OkHttpUtils.getInstance().cancelTag(VideoListAdapter.this);
					Toast.makeText(context, "上传取消", Toast.LENGTH_SHORT).show();
					videolist_upload.setEnabled(true);
					videolist_share.setEnabled(true);
				}

			}
		});
	}
}
