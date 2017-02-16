package com.vunke.sharehome.rx;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.callback.StringCallback;

public  class OkHttpConfig {
	private String url;
	private String json;

	public OkHttpConfig(String url, String json,
			final OkHttpConfigCallBack configCallBack) {
		this.url = url;
		this.json = json;
		OkHttpUtils.post(url).tag(this).cacheKey("cacheKey")
				.params("json", json.toString()).cacheKey("cacheKey")
				// 先请求网络，如果请求网络失败，则读取缓存，如果读取缓存失败，本次请求失败。
				.cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						// isFromCache 表示当前回调是否来自于缓存
						if (isFromCache) {
							// 如果有缓存
							Log.e("tag", "有缓存");
							configCallBack.OnSuccess(t);
						} else {
							Log.e("tag", "无缓存");
							Log.e("tag", t);
							configCallBack.OnSuccess(t);
						}
					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						if (isFromCache) {
							Log.e("tag", "报错，有缓存");
							configCallBack.OnFail();
						} else {
							Log.e("tag", "报错，无缓存");
							configCallBack.OnFail();
						}

					}

				});
	}

	public interface OkHttpConfigCallBack {
		void OnSuccess(String string);

		void OnFail();

	}

}
