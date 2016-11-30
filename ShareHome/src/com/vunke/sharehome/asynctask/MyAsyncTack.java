package com.vunke.sharehome.asynctask;

import android.os.AsyncTask;

/**
 * AsyncTack使用方法 必须子类化 AsyncTack  AsyncTack只能实例化一次 AsyncTack 
 * 
 * @param Params
 *            执行任务的传入参数 Void String Integer ,多半情况下使用String类型作为参数 用于传入url地址 同时也是
 *            doInBackground()方法的参数
 * @param Progress
 *            在后台发布进度的使用的参数 调用 publishProgress(Progress)
 *            同时也是onProgressUpdate()方法的传入参数
 * @param Result
 *            doInBackground() 的返回值
 */
public class MyAsyncTack extends AsyncTask<String, Void, Void> {

	// 当 AsyncTack 启动 首先执行改方法，该方法执行完毕 离线调用 doInBackground -- UI Theard
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	// 在当该方法在onPreExecute执行之后执行 -- 后台(辅线程)
	@Override
	protected Void doInBackground(String... params) {
		// 该方法在后台执行，主要用于发布进度，该方法被调用，onProgressUpdate被回调
		// ，否则不会被回调onProgressUpdate方法
		publishProgress();
		return null;
	}

	// 只有在 publishProgress 被调用后才会被系统回调 --UI Theard
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}

	// 在 doInBackground 执行之后被调用
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}
}
