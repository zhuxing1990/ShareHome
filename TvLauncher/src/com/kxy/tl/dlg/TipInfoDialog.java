package com.kxy.tl.dlg;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.kxy.tl.R;

public class TipInfoDialog extends Dialog {
	String value;
	TextView tv_value;

	public TipInfoDialog(Context context, String value) {
		super(context, R.style.commonDialog);
		this.value = value;
		setOwnerActivity((Activity) context);
	}

	public void setMessage(String value){
		if (!TextUtils.isEmpty(value)) {
			this.value = value;
			tv_value.setText(value);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_tipinfo);
		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
		tv_value = (TextView) findViewById(R.id.tv_value);
		if (!TextUtils.isEmpty(value)) {
			tv_value.setText(value);
		}
		setCanceledOnTouchOutside(false);
	}
}
