package com.kxy.ti.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.kxy.tl.dlg.CustomProgressDialog;
import com.kxy.tl.dlg.TipInfoDialog;

public class UIUtil {
	
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	
	public static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}
	
	public static void showWaitDialog(Activity aty) {
		CustomProgressDialog.show(aty);
	}
	
	/**
	 * 子线程中无法直接修改UI
	 * @param aty
	 * @param message
	 * @param handler
	 */
	public static void showWaitDialogInThread(final Activity aty, final String message, Handler handler) {
		handler.post(new Runnable() {
			public void run() {
				showWaitDialog(aty);
			}
		});
	}

	public static void dismissDlg() {
		CustomProgressDialog.hidden();
	}

	public static void showToast(Activity aty,String msg) {
		Toast.makeText(aty, msg, Toast.LENGTH_LONG).show();
	}

	public static void showToast(Activity aty,int id) {
		Toast.makeText(aty, id, Toast.LENGTH_LONG).show();
	}

	public static void showShortToast(Context aty,String msg) {
		Toast.makeText(aty, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static void showShortToast(Activity aty,int id) {
		Toast.makeText(aty, id, Toast.LENGTH_SHORT).show();
	}
	
	public static TipInfoDialog tipDlg;
	public static void showTipDlg(final Activity aty, String msg,
			TipInfoDialog.OnDismissListener ilistener) {
		if (aty != null && !aty.isFinishing()) {
			if (tipDlg != null && tipDlg.isShowing()) {
				tipDlg.setMessage(msg);
			} else {
				tipDlg = new TipInfoDialog(aty, msg);
				tipDlg.show();
			}
			if(ilistener!=null)
				tipDlg.setOnDismissListener(ilistener);
		}
	}
	
	
	private static long lastClickTime = 0;
	// 防止按钮重复点击
	public static boolean isFastDoubleClick(float ts) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		lastClickTime = time;
		if (0 < timeD && timeD < ts * 1000) {
			return true;
		}
		return false;
	}
	
	/**
	 * 隐藏软键盘，只在edittext没有获取焦点时有用
	 * @param aty
	 */
	public static void hideSoftKeyboard(Activity aty) {
		aty.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 隐藏意见反馈残留的软键盘
	}
	
	/**
	 * 隐藏软键盘
	 */
	public static void hideSoftKeyboard(Context mcontext,EditText v) {
	    InputMethodManager imm = (InputMethodManager) mcontext.getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	/**
	 * 让activity全屏
	 * @param aty
	 */
	public static void makeFullScreenAty(Activity aty) {
		aty.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public static void sendBroadCast(Context mcontext,String Action,Intent intent){
        intent.setAction(Action);  
		intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mcontext.sendBroadcast(intent); 
	}
	public static void ShowToast(Context context,String str){
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}
}
