package com.vunke.sharehome.updata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.SharedPreferencesUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * @author zs
 * @since 2015-08-24
 */
@SuppressLint({ "InflateParams", "NewApi" })
public class AppTVStoreUpdateManager {
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	// 更新信号量
	private static final int UPDATE = 3;
	// 没有空闲的控件
	private static final int NotFreeSpace = 4;
	// 固定App名称
	private static final String mAppName = "ShareHome.apk";
	/* 下载保存路径 */
	// 固定存放下载的apk的路径：SD卡目录下,可以在此基础上继续添加子目录
	// private static final String mSavePath = Environment
	// .getExternalStorageDirectory().getPath()
	// + File.separator
	// + "AppTVStore" + File.separator + "download" + File.separator;
	private static final String mSavePath = "/sdcard/ShareHomeDownload";
	// APk下载链接，等服务器信息修改后改成mAppUpdateInfos.get(0).get....(待修改！)
//	private String mAppDownLoadURL = UrlClient.AppDownLoadURL;
	// 上下文信息
	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	// 更新进度百分比
	private TextView mTextView;
	// 下载对话框
	private Dialog mDownloadDialog;
	// 后台地址
	// private String mServerURL = "http://115.159.97.109/v1.0";
	// 获取更新信息的链接Json
	private String mAppUpdateJasonURL = UrlClient.ServerURL + "/update.html";
	// 更新信息
	private List<AppTVStoreUpdateInfo> mAppUpdateInfos;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新信号量 */
	private boolean cancelUpdate = false;
	// 获取rom内存剩于的运行空间
	public static long rom_freeSpace = Environment.getDataDirectory()
			.getFreeSpace();
	// 获取sd卡的剩于空间
	public static long sd_freeSpace = Environment.getExternalStorageDirectory()
			.getFreeSpace();

	// 构造函数
	public AppTVStoreUpdateManager(Context context) {
		this.mContext = context;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 检查更新
			case UPDATE:
				if (mAppUpdateInfos != null && mAppUpdateInfos.size() != 0) {
					CheckAppTVStoreUpdate();
					// 待修改
					// mAppUpdateJasonURL=mAppUpdateInfos.get(0).getUpdateUrl();
				}
				break;
			// 正在下载
			case DOWNLOAD:
				// 设置进度条位置
				mProgress.setProgress(progress);
				// mProgressSize.setText(result + "%");
				mTextView.setText(progress + "%");
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			case NotFreeSpace:
				showToast("没有储存的空间");
				break;
			default:
				break;
			}
		}

	};

	// 检查AppTVStore是否需要更新
	public void GetAppTVStoreUpdateInfo() {
		// 获取服务器上的更新信息
		new Thread() {
			@Override
			public void run() {
				try {
					mAppUpdateInfos = JsonGetAppsInfos
							.getAppUpdateInfo(mAppUpdateJasonURL);
				} catch (Exception e) {
					Log.i("JsonGetApps.getHomeApps", e.toString());
				}
				mHandler.sendEmptyMessage(UPDATE);
			};
		}.start();
	}

	private void CheckAppTVStoreUpdate() {
		// 获取当前软件版本
		int versionCode = getAppTVStoreVersionCode(mContext);
		// 判断更新信息是否为空
		if (!(mAppUpdateInfos.isEmpty())) {
			WorkLog.i("UpdateManager", "服务器版本:"
					+ mAppUpdateInfos.get(0).getVersion() + "\n当前版本:"
					+ versionCode);
			if (mAppUpdateInfos.get(0).getVersion() == versionCode) {
				WorkLog.i("UpdateManager", "当前为最新版本");
				return;
			}
			// 判断服务器应用版本是否高于本地版本
			// (待修改！)
			if (mAppUpdateInfos.get(0).getVersion() > versionCode) {// 升级判断
																	// 如果当前版本号比最新版本号大的话
																	// 升级
				// 判断升级模式，0强制升级，1可选升级
				if (1 == mAppUpdateInfos.get(0).getUpdateType()) {
					// 可选升级
					// showDownloadDialog();
					showNoticeDialog();
					// downloadAppTVStore();
					// showDownloadDialogNoCancel();
				} else if (0 == mAppUpdateInfos.get(0).getUpdateType()) {// 如果当前版本号
																			// 等于0
																			// 强制升级
					// 强制升级
					// 有进度条，又取消按钮
					// showDownloadDialog();
					// 有进度条
					// showDownloadDialogNoCancel();
					// 无进度条
					downloadAppTVStore();
				}
			}
		}
	}

	private int getAppTVStoreVersionCode(Context context) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo(
					"com.vunke.sharehome", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog() {

		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_info);
		// 更新
		builder.setPositiveButton(R.string.soft_update_updatebtn,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int

					which) {
						dialog.dismiss();
						// 显示下载对话框
						showDownloadDialog();
					}
				});
		// 稍后更新
		builder.setNegativeButton(R.string.soft_update_later,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int

					which) {
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(R.string.soft_update_tomorrow,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						long time = System.currentTimeMillis();
						SharedPreferencesUtils.put(mContext,
								Config.UPDATE_TOMORROW, time);
						dialog.dismiss();
					}
				});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框,有取消按钮
	 */
	private void showDownloadDialog() {
		if (UiUtils.hasSDCard() == false) {
			Toast.makeText(mContext, "SD卡不存在", Toast.LENGTH_SHORT).show();
			return;
		}
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("想家正在升级...");
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		mTextView = (TextView) v.findViewById(R.id.update_textView);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int

					which) {
						dialog.dismiss();
						// 设置取消状态
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 现在文件
		downloadApk();
	}

	/**
	 * 显示软件下载对话框,没有取消按钮
	 */
	@SuppressLint("InflateParams")
	private void showDownloadDialogNoCancel() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("想家正在升级...");
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		mTextView = (TextView) v.findViewById(R.id.update_textView);
		builder.setView(v);
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 现在文件
		downloadApk();
	}

	// 下载apk文件
	private void downloadApk() {
		// 判断SD卡是否存在，并且是否具有读写权限
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	// 下载文件线程

	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				if (UiUtils.hasSDCard() == true) {
					// 获得存储卡的路径
					// 下载链接
					URL url = new URL(mAppUpdateInfos.get(0).getUpdateUrl());
					// URL url = new URL(mHashMap.get("url"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 获取文件大小
					long length = conn.getContentLength();
					if ((sd_freeSpace / 1024) > (length / 1024)) {
						// WorkLog.a("SD卡内存"+sd_freeSpace);
						// WorkLog.a("下载大小"+length/1024);
					} else {
						// WorkLog.a("SD卡内存"+sd_freeSpace);
						// WorkLog.a("下载大小"+length/1024);
						mHandler.sendEmptyMessage(NotFreeSpace);
						return;
					}
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					// 存储名称
					File apkFile = new File(mSavePath, mAppName);
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024 * 4];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);

						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage

							(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	// 下载AppTVStore文件

	private void downloadAppTVStore() {
		if (UiUtils.hasSDCard() == false) {
			Toast.makeText(mContext, "SD卡不存在", Toast.LENGTH_SHORT).show();
			return;
		}
		// 启动新线程下载软件
		new downloadAppTVStoreThread().start();
	}

	// 下载文件线程
	private class downloadAppTVStoreThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 下载链接
					URL url = new URL(mAppUpdateInfos.get(0).getUpdateUrl());
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 创建输入流
					InputStream is = conn.getInputStream();
					// 创建文件目录
					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					// 存储名称
					File apkFile = new File(mSavePath, mAppName);
					FileOutputStream fos = new FileOutputStream(apkFile);
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
						// 点击取消就停止下载.
					} while (!cancelUpdate);
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, mAppName);
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}

	public String getmSavePath() {
		return mSavePath;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public ProgressBar getmProgress() {
		return mProgress;
	}

	public void setmProgress(ProgressBar mProgress) {
		this.mProgress = mProgress;
	}

	public Dialog getmDownloadDialog() {
		return mDownloadDialog;
	}

	public void setmDownloadDialog(Dialog mDownloadDialog) {
		this.mDownloadDialog = mDownloadDialog;
	}

	public String getmAppUpdateJasonURL() {
		return mAppUpdateJasonURL;
	}

	public void setmAppUpdateJasonURL(String mAppUpdateJasonURL) {
		this.mAppUpdateJasonURL = mAppUpdateJasonURL;
	}

	public List<AppTVStoreUpdateInfo> getmAppUpdateInfos() {
		return mAppUpdateInfos;
	}

	public void setmAppUpdateInfos(List<AppTVStoreUpdateInfo> mAppUpdateInfos) {
		this.mAppUpdateInfos = mAppUpdateInfos;
	}

	public Handler getmHandler() {
		return mHandler;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

//	public String getmAppDownLoadURL() {
//		return mAppDownLoadURL;
//	}
//
//	public void setmAppDownLoadURL(String mAppDownLoadURL) {
//		this.mAppDownLoadURL = mAppDownLoadURL;
//	}

	private void showToast(String string) {
		Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
	};

}
