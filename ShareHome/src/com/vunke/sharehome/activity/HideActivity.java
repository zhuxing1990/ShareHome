package com.vunke.sharehome.activity;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.huawei.rcs.login.LoginApi;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
/**
 * 隐藏的页面(设置DM地址)
 * */
public class HideActivity extends BaseActivity {
	EditText ip = null;
	EditText port = null;
	Button commit ;
	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_hide);
		init();
	}

	public void init() {
		ip = (EditText) findViewById(R.id.hide_Ip);
		port = (EditText) findViewById(R.id.hide_port);
		ip.setText(LoginApi.getConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP, LoginApi.CONFIG_MINOR_TYPE_DEFAULT));
		port.setText(LoginApi.getConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT, LoginApi.CONFIG_MINOR_TYPE_DEFAULT));
		
//		ip.setText("183.62.212.193");
		ip.setText("222.246.189.244");
		commit = (Button) findViewById(R.id.hide_commit);
		commit.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				String sip = ip.getText().toString();
				String sport = port.getText().toString();
				if(TextUtils.isEmpty(sip) || TextUtils.isEmpty(sport)) {
					showToast("please input value");
				}else{
				LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP, LoginApi.CONFIG_MINOR_TYPE_DEFAULT, "222.246.189.244");
				LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT, LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sport);
				finish();
				}
				return false;
			}
		});
	}

	@Override
	public void OnClick(View v) {
		
	}

}
