package com.kxy.tl.versionUp;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpUtil {

	private static final String BASE_URL = "http://124.232.135.225:9390/LauncherServer/checkUpdateAction";
	
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(AsyncHttpResponseHandler responseHandler) {
    	RequestParams params = new RequestParams();
    	
    	client.setConnectTimeout(10000);
    	client.post(BASE_URL, params, responseHandler);
    }
}
