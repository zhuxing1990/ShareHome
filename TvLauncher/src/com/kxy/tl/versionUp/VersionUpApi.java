package com.kxy.tl.versionUp;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.kxy.ti.util.LogUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class VersionUpApi {

	public static void getUpdateVersion(final Context context) {
		final String version = getAppVersionName(context);
		if (version.isEmpty()) {
			LogUtil.i("tv_launcher","version.isEmpty()");
			return;
		}
		UpdateUtil uputil = new UpdateUtil(context);
		if(uputil.isDownLoding()){
			uputil.createNotification();
			return;
		}
		
		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
		    	String resultContent = "";
				try {
					resultContent = new String(arg2, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				try {
					JSONObject object = new JSONObject(resultContent);
					String versionName = object.getString("version");
					if (null != versionName && !versionName.equals("")) {
						if (version.equals(versionName)) {
						} else{
							new TipInfoDialog2(context,"发现新版本。\n请点击确认升级",new View.OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									new UpdateUtil(context).StartDownLoad();
									
								}
							}).show();
						}
					} 
				} catch (JSONException e) {
				} 

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				super.onFinish();
			}
		};
		HttpUtil.post(responseHandler);

	}

	/**
	 * 获取app版本名称
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {

		}
		return versionName;
	}
}
