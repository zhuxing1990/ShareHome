package com.vunke.sharehome.listener;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;

public class MyWebViewDownLoadListener implements DownloadListener{
	private Context context;
	public MyWebViewDownLoadListener(Context context) {
		this.context = context;
	}
	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
			long contentLength) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(intent);
	}
}
