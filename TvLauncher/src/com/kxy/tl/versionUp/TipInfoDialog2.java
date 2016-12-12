package com.kxy.tl.versionUp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.kxy.tl.R;

public class TipInfoDialog2 extends Dialog {
	TextView tv_value;
	String tv_hint;
	String msg;
	View.OnClickListener listener;

	public TipInfoDialog2(Context context, String message,
			View.OnClickListener onlistener) {
		super(context, R.style.commonDialog);
		setOwnerActivity((Activity) context);
		listener = onlistener;
		this.msg = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_tipinfo);
		if (listener == null) {
			findViewById(R.id.cancel).setVisibility(View.GONE);
		} else {
			findViewById(R.id.cancel).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							dismiss();
							listener.onClick(arg0);
						}
					});
		}
		((TextView) findViewById(R.id.tv_hint)).setText("升级提醒");
		tv_value = (TextView) findViewById(R.id.tv_value);
		tv_value.setText(msg);
		setCanceledOnTouchOutside(false);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}
}
