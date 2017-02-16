package com.vunke.sharehome.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UploadVideoService extends Service {
	public static final String UPLOAD_VIDEO_SERVICE = "upload_video_service";
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action.equals(UPLOAD_VIDEO_SERVICE)) {
			
		}
		return super.onStartCommand(intent, flags, startId);
	}
}
