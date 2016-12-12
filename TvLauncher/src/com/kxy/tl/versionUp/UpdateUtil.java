/**
 * 
 */
package com.kxy.tl.versionUp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.kxy.ti.util.LogUtil;

public class UpdateUtil {
	/** 服务的运行状态，0:未运行 1:正在运行 */
	public static int state = 0;
	/** 连接和读取的超时时间 */
	private static final int TIMEOUT = 10 * 1000;
	/** 下载完成 */
	private static final int DOWN_OK = 1;
	/** 下载失败 */
	private static final int DOWN_ERROR = 2;

	/** 下载安装包保存路径 */
	public static final String savePath = "LauncherDownload";
	/** 下载安装包保存名字 */
	public static final String saveName = "Launcher.apk";

	public static final String DownLoadUrl = "http://124.232.135.225:9390/LauncherServer/FileDownload.action";

	private String downUrl;
	private File saveFile;
	private Thread downThread;
	TipInfoDialog2 dlg;
	Context context;
	private boolean isDownLoading = false;

	public UpdateUtil(Context context) {
		this.context = context;
		dlg = new TipInfoDialog2(context, "正在更新，请稍后...", null);
	}

	public void StartDownLoad() {
		isDownLoading = true;
		downUrl = DownLoadUrl;
		if (downUrl != null && !downUrl.isEmpty()) {
			if (!creatFile()) {// 创建文件
				Toast.makeText(context, "创建文件失败,请检查SD卡是否正常", Toast.LENGTH_SHORT)
						.show();
			}
			createNotification();// 创建通知
			createThread();// 线程下载}
		}
	}

	private boolean creatFile() {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			File rootFile = new File(Environment.getExternalStorageDirectory()
					+ File.separator + savePath);
			if (rootFile != null && !rootFile.exists()) {
				rootFile.mkdir();
			}
			saveFile = new File(rootFile, saveName);
			return true;
		}
		return false;
	}

	/***
	 * 创建通知栏
	 */
	public void createNotification() {
		if(!dlg.isShowing()){
			dlg.show();
		}
	}

	/***
	 * 更新UI
	 */
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OK:
				// 下载完成，准备安装
				isDownLoading = false;
				Uri uri = Uri.fromFile(saveFile);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri,
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				break;
			case DOWN_ERROR:
				isDownLoading = false;
				Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

		}

	};

	/**
	 * 开线程下载
	 */
	public void createThread() {

		final Message message = handler.obtainMessage();

		downThread = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					boolean result = downloadUpdateFile(downUrl,
							saveFile.getPath());
					if (result) {
						// 下载成功，准备安装
						message.what = DOWN_OK;
						handler.sendMessage(message);
					}

				} catch (IOException e) {
					e.printStackTrace();
					message.what = DOWN_ERROR;
					handler.sendMessageDelayed(message, 2000);
				}

			}
		});
		downThread.start();
	}

	/**
	 * 下载文件
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean downloadUpdateFile(String downUrl, String file)
			throws IOException {
		int downloadCount = 0;// 已经下载好的大小
		InputStream inputStream;
		OutputStream outputStream;

		HttpURLConnection httpURLConnection;
		URL url = new URL(downUrl);
		httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);
		// 获取下载文件的size
		if (httpURLConnection.getResponseCode() == 404) {
			throw new IOException("文件不存在!");
		}
		inputStream = httpURLConnection.getInputStream();
		outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
		try {
			byte buffer[] = new byte[1024];
			int readsize = 0;
			while ((readsize = inputStream.read(buffer)) != -1
					&& !Thread.currentThread().isInterrupted()) {
				outputStream.write(buffer, 0, readsize);
				downloadCount += readsize;// 时时获取下载到的大小
				LogUtil.i("tv_launcher", "--downloadCount--" + downloadCount);
			}
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
			inputStream.close();
			outputStream.close();
		}
		return true;
	}

	public boolean isDownLoding(){
		LogUtil.i("tv_launcher", "--isDownLoad--"+isDownLoading);
		return isDownLoading;
	}
}
