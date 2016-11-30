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


	/**
	 * 正则表达式判断邮箱
	 * */
	public static boolean isValidEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 正则表达式 判断账号
	 */
	public static boolean isAccountStandard(String username) {
		// 不能包含中文
		if (hasChinese(username)) {
			return false;
		}

		/**
		 * 正则匹配： [a-zA-Z]:字母开头 \\w :可包含大小写字母，数字，下划线,@ {5,17} 5到17位，加上开头字母
		 * 字符串长度5到18 [a-zA-Z](@?+\\w){5,17}+
		 */
		String format = "(@?+\\w){5,17}+";
		if (username.matches(format)) {
			return true;
		}
		return false;
	}

	/**
	 * 正则表达式 判断密码
	 */
	public static boolean isPasswordStandard(String user_wd) {

		// 不能包含中文
		if (hasChinese(user_wd)) {
			return false;
		}

		/**
		 * 正则匹配 \\w{6,18}匹配所有字母、数字、下划线 字符串长度6到18（不含空格）
		 */
		String format = "(@?+\\w){6,18}+";
		if (user_wd.matches(format)) {
			return true;
		}
		return false;
	}

	/**
	 * 中文识别
	 * 
	 * @author luman
	 */
	public static boolean hasChinese(String source) {
		String reg_charset = "([\\u4E00-\\u9FA5]*+)";
		Pattern p = Pattern.compile(reg_charset);
		Matcher m = p.matcher(source);
		boolean hasChinese = false;
		while (m.find()) {
			if (!"".equals(m.group(1))) {
				hasChinese = true;
			}

		}
		return hasChinese;
	}

	/**
	 * 设置字体
	 * */
	public static void setTextStyle(View view) {
		Typeface face = Typeface.createFromAsset(BaseApplication
				.getApplication().getAssets(), "fonts/fzjt.ttf");
		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			tv.setTypeface(face);
		} else if (view instanceof Button) {
			Button btn = (Button) view;
			btn.setTypeface(face);
		} else if (view instanceof EditText) {
			EditText et = (EditText) view;
			et.setTypeface(face);

		}

	}

	/**
	 * 设置字体
	 * */
	public static void setTextStyle(View... v) {
		for (int i = 0; i < v.length; i++) {
			View view = v[i];
			Typeface face = Typeface.createFromAsset(BaseApplication
					.getApplication().getAssets(), "fonts/fzjt.ttf");
			if (view instanceof TextView) {
				TextView tv = (TextView) view;
				tv.setTypeface(face);
			} else if (view instanceof Button) {
				Button btn = (Button) view;
				btn.setTypeface(face);
			} else if (view instanceof EditText) {
				EditText et = (EditText) view;
				et.setTypeface(face);
			} else if (view instanceof CheckBox) {
				CheckBox box = (CheckBox) view;
				box.setTypeface(face);
			}

		}
	}

	/**
	 * 判断当前网络是否连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * 转向另一个页面
	 * 
	 * @param poFrom
	 *            当前activity
	 * @param poTo
	 *            跳转到这个activity
	 * @param pbFinish
	 *            是否finish当前页面
	 * @param pmExtra
	 *            携带数据，不带数据写null Map<String, String> lmExtra = null; String
	 *            msRedirectPage = "登录成功"; if
	 *            (!Utils.isStrEmpty(msRedirectPage)) { lmExtra = new
	 *            HashMap<String, String>(); lmExtra.put("redirect",
	 *            msRedirectPage); }
	 */
	public static void gotoActivity(Activity poFrom, Class<?> poTo,
			boolean pbFinish, Map<String, String> pmExtra) {
		Intent loIntent = new Intent(poFrom, poTo);
		if (pmExtra != null && !pmExtra.isEmpty()) {
			Iterator<String> loKeyIt = pmExtra.keySet().iterator();
			while (loKeyIt.hasNext()) {
				String lsKey = loKeyIt.next();
				loIntent.putExtra(lsKey, pmExtra.get(lsKey));
			}
		}
		if (pbFinish)
			poFrom.finish();
		poFrom.startActivity(loIntent);
	}

	/** 隐藏软键盘 */
	public static void hideSoftInput(Context context) {

		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager.isActive()) {
			inputMethodManager.toggleSoftInput(0, 2); // 隐藏输入盘
		}
	}

	/**
	 * dip转换px
	 */
	public static int dip2px(Context context, int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
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

	@Override
	public void onClick(View v) {
		OnClick(v);
	}

	/**
	 * 根据两个长整形数，判断是否是同一天
	 * 
	 * @param lastDay
	 * @param thisDay
	 * @return
	 */
	public static boolean isSameToday(long lastDay, long thisDay) {
		Time time = new Time();
		time.set(lastDay);

		int thenYear = time.year;
		int thenMonth = time.month;
		int thenMonthDay = time.monthDay;

		time.set(thisDay);
		return (thenYear == time.year) && (thenMonth == time.month)
				&& (thenMonthDay == time.monthDay);
	}

	/**
	 * 使用系统工具类判断是否是今天 是今天就显示发送的小时分钟 不是今天就显示发送的那一天
	 * */
	public static String getDate(Context context, long when) {
		String date = null;
		if (DateUtils.isToday(when)) {
			date = DateFormat.getTimeFormat(context).format(when);
		} else {
			date = DateFormat.getDateFormat(context).format(when);
		}
		return date;
	}

	/**
	 * 测试当前摄像头能否被使用
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public static boolean isCameraCanUse() {
		boolean canUse = true;
		Camera mCamera = null;
		try {
			mCamera = Camera.open(0);
			mCamera.setDisplayOrientation(90);
		} catch (Exception e) {
			canUse = false;
		}
		if (canUse) {
			mCamera.release();
			mCamera = null;
		}
//		WorkLog.e("BaseActivity", "isCameraCanuse=" + canUse);
		return canUse;
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
