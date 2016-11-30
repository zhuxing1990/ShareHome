package com.vunke.sharehome.service;

import android.content.Intent;

import com.huawei.rcs.RCSService;

public class ShareHomeService extends RCSService {
	/**
	 * 唯一标识开发者开发的应用，用于与其它系统的服务进行区分。
	 * */
	public static final String SERVICE_NAME="com.vunke.rcs.SERVICE";
	@Override
	public int onStartCommand(Intent arg0, int arg1, int arg2) {
		return super.onStartCommand(arg0, arg1, arg2);
	}
}
