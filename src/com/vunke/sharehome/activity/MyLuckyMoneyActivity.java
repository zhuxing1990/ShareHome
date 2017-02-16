package com.vunke.sharehome.activity;

import android.view.View;
import android.widget.Button;

import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;

public class MyLuckyMoneyActivity extends BaseActivity {
	private Button myluckymoney_back;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_myluckymoney);
		init();
	}

	private void init() {
		myluckymoney_back = (Button) findViewById(R.id.myluckymoney_back);

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.myluckymoney_back:
			finish();
			break;

		default:
			break;
		}
	}

}
