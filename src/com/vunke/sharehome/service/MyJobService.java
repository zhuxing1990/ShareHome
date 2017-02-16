package com.vunke.sharehome.service;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.utils.WorkLog;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@SuppressLint("NewApi")
public class MyJobService extends JobService {

	@Override
	public boolean onStartJob(JobParameters params) {
		WorkLog.i("MyJobService", "mylong:"+mylong++);
		if (isNetworkConnected()) {
			Config.intent = new Intent(this,NetConnectService.class);
			startService(Config.intent);
			return true;
		}
		return false;
	}
	private long mylong = 0;
	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
	}

	@SuppressLint("NewApi")
	private boolean isNetworkConnected() {

		ConnectivityManager connManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());

	}

}
