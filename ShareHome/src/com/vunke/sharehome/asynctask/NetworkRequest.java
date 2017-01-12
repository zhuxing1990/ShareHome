package com.vunke.sharehome.asynctask;

import com.lzy.okhttputils.callback.AbsCallback;
import com.vunke.sharehome.model.ResponseResult;

import android.app.Activity;
import android.content.Context;


public class NetworkRequest<T> {

	public static NetworkRequest instance;
	public Context context;

	public NetworkRequest(Context context) {
		this.context = context;
	}

	public static NetworkRequest getInstance(Context context) {
		if (instance == null) {
			instance = new NetworkRequest(context);
		}
		return instance;
	}
	public void startRequest(String url,String json,Activity tag, AbsCallback<T> callback){
//		OkHttpUtils.post(url).tag(tag).params("","").execute(callback);
		
	}
	
	public interface NetworkRequestCallBack<T>{
		 void OnSuccess(ResponseResult<T> response_result);
		 void OnFail(ResponseResult<T> response_result);
	}
}
