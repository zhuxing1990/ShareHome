package com.vunke.sharehome.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PhoneUtils {
	public static void call(Context context ,String phone){
		Intent intent = new Intent();
		//--- 跳转至打电话activity 
		/*intent.setAction(Intent.ACTION_CALL);//隐式意图*/

		intent.setAction(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:"+phone));
		context.startActivity(intent);
	}
}
