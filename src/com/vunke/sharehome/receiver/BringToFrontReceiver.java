package com.vunke.sharehome.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vunke.sharehome.activity.LoginActivity2;

public class BringToFrontReceiver extends BroadcastReceiver {
	  public static final String ACTION_BRING_TO_FRONT ="neal.pushtest.action.BringToFront";  
	    public BringToFrontReceiver() {  
	    }  
	  
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	        //获取ActivityManager  
	        ActivityManager mAm = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);  
	        //获得当前运行的task  
	        List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);  
	        for (ActivityManager.RunningTaskInfo rti : taskList) {  
	            //找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台  
	            if(rti.topActivity.getPackageName().equals(context.getPackageName())) {  
	                try {  
	                    Intent  resultIntent = new Intent(context, Class.forName(rti.topActivity.getClassName()));  
	                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);  
	                    context.startActivity(resultIntent);  
	                }catch (ClassNotFoundException e) {  
	                    e.printStackTrace();  
	                }  
	                return;  
	            }  
	        }  
	        //若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity  
	        Intent resultIntent = new Intent(context, LoginActivity2.class);  
	        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);  
	        context.startActivity(resultIntent);  
	  
	    }  
}
