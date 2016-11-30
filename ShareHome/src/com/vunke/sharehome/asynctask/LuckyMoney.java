package com.vunke.sharehome.asynctask;

import android.app.Activity;
import android.content.Context;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;

public class LuckyMoney {
	private static LuckyMoney instance;
	private Context context;

	public LuckyMoney(Context context) {
		this.context = context;
	}

	public static LuckyMoney getInstance(Context context) {
		if (instance == null) {
			instance = new LuckyMoney(context);
		}
		return instance;
	}

	public void CreateLuckyMoney(String url, String json, Activity ts,
			final StringCallback callBack) {
		try {
			OkHttpUtils.post(url).tag(ts).params("json", json)
					.execute(callBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
