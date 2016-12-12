package com.kxy.notify;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.Response;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.widget.Toast;

import com.kxy.ti.util.Constants;
import com.kxy.ti.util.LogUtil;
import com.kxy.ti.util.SharedPreferencesUtil;
import com.kxy.ti.util.UIUtil;
import com.kxy.tl.activity.TupianActivity;
import com.lzy.okhttputils.OkHttpUtils;

public class UiUtils {

	// private void StartEPG(StartInfoBean startInfo, Context context) {
	// String packageName = startInfo.getData().getStartPackage();
	/**
	 * 根据包名启动APK
	 * 
	 * @param packageName
	 * @param context
	 */
	public static void StartEPG(String packageName, Context context) {
		if (TextUtils.isEmpty(packageName)) {
			LogUtil.e("tv_launcher", "包名为空");
			return;
		}
		PackageInfo pi;
		try {
			pi = context.getPackageManager().getPackageInfo(packageName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.setPackage(pi.packageName);
			PackageManager pManager = context.getPackageManager();
			List apps = pManager.queryIntentActivities(resolveIntent, 0);
			ResolveInfo ri = (ResolveInfo) apps.iterator().next();
			if (ri != null) {
				packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;
				Intent intent = new Intent(Intent.ACTION_MAIN);
				ComponentName cn = new ComponentName(packageName, className);
				intent.setComponent(cn);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @return versionName 版本名字
	 */
	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			versionName = context.getPackageManager().getPackageInfo(
					"com.kxy.tl", 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
		return versionName;
	}

	/**
	 * @param context
	 * @return versionCode 版本号
	 */
	public static int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					"com.kxy.tl", 0).versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return versionCode;
	}

	/**
	 * 获取上次启动信息
	 * 
	 * @param context
	 * @param key
	 * @param defultValue
	 * @return
	 */
	public static String getPackageName(Context context, String key,
			String defultValue) {
		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		String result = "";
		if (null != sp) {
			result = sp.getString(key, defultValue);
		}
		return result;
	}

	/**
	 * 设置本次启动信息
	 * 
	 * @param context
	 * @param key
	 * @param vaule
	 */
	public static void setPackageName(Context context, String key, String vaule) {
		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString(key, vaule);
		edit.commit();
	}

	public static void StartMangGuoEPG(Context context) {
		UIUtil.sendBroadCast(context, Constants.ADVERTISING_ACTION,
				new Intent());// 节目播放业务
		SharedPreferencesUtil.setBooleanValue(context,
				SharedPreferencesUtil.IS_PALYED_ADVERT, true);
		// finish();
		LogUtil.i("tv_launcher", "send BroadCast to play iptv,start time:"
				+ new Date());
	}

	public static void GetUrlCode(String url) {
		try {
			Response response = OkHttpUtils.get(url).execute();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void ShowToast(Context mcontext, String str) {
		if (mcontext == null) {
			LogUtil.e("tv_launcher", "ShowToast()无法获取上下文");
			return;
		}
		if (TextUtils.isEmpty(str)) {
			Toast.makeText(mcontext, "未定义提示内容", Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(mcontext, str, Toast.LENGTH_SHORT).show();
	}
}
