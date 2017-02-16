package com.vunke.sharehome.activity;

import android.view.View;
import android.widget.Button;

import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;

/**
 * 服务条款
 * @author Administrator
 */
public class ServiceTermsActivity extends BaseActivity {
	Button terms_back;//返回键
	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_serviceterms);
		init();
	}

	private void init() {
		terms_back = (Button) findViewById(R.id.terms_back);//返回键
		SetOnClickListener(terms_back);//设置点击事件
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.terms_back://返回键
			finish();
			break;

		default:
			break;
		}

	}

}
