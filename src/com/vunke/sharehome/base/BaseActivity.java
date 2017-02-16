package com.vunke.sharehome.base;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vunke.sharehome.utils.AppManager;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 基类
 * */
@SuppressLint("NewApi")
public abstract class BaseActivity extends Activity implements OnClickListener {
	public static Toast mToast;// 吐司
	public String filename = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "userpic.jpg";
	public String publishfilename = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "publish.jpg";
	protected BaseActivity mcontext;
	protected String countryCode = "+86";
	protected Intent intent;
	protected String nine = "9";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mcontext = this;
		//禁止横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);
		OnCreate();

	}

	public abstract void OnCreate();

	public abstract void OnClick(View v);

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View view) {
		view.setOnClickListener(this);
	}

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View... v) {
		for (int i = 0; i < v.length; i++) {
			View view = v[i];
			view.setOnClickListener(this);
		}
	}

	/**
	 * 吐司
	 * */
	public void showToast(String string) {
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT)
				.show();
	}


	@Override
	public void onClick(View v) {
		OnClick(v);
	}
	/**
	 * 头像圆角
	 * */
	public Bitmap setPicRoundCorner(Bitmap thePic) {
		thePic = getDiskBitmap(filename);
		Bitmap output = Bitmap.createBitmap(thePic.getWidth(),
				thePic.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, thePic.getWidth(), thePic.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, 100, 100, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(thePic, rect, rect, paint);

		return output;
	}

	/**
	 * 将要分享的图片保存到本地图片上
	 * */
	public void savePublishPicture(Bitmap bitmap) {
		File f = new File(publishfilename);
		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);// 把Bitmap对象解析成流
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从本地读取头像
	 * */
	public Bitmap getDiskBitmap(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return bitmap;
	}
	/**
	 * 获取App内容
	 * */
	private String getAppInfo() {
		try {
			String pkName = getPackageName();
			String versionName = getPackageManager().getPackageInfo(pkName, 0).versionName;
			int versionCode = getPackageManager().getPackageInfo(pkName, 0).versionCode;
			return pkName + " " + versionName + " " + versionCode;
		} catch (Exception e) {
		}
		return null;
	}
}
