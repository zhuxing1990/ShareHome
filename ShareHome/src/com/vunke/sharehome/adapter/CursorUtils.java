package com.vunke.sharehome.adapter;

import java.io.InputStream;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.Time;

import com.vunke.sharehome.utils.UiUtils;

public class CursorUtils {
	public static void printCursor(Cursor cursor) {
		if (cursor == null) {
//			System.out.println("cursor==null");
			return;
		}
		if (cursor.getCount() == 0) {
//			System.out.println("cursor.getCount()==0");
			return;
		}

		int count = cursor.getColumnCount();
		while (cursor.moveToNext()) {
//			System.out.println("这是第几行---------------:" + cursor.getPosition());
			for (int i = 0; i < count; i++) {
				String value = cursor.getString(i);
				String name = cursor.getColumnName(i);
//				System.out.println(name + ":" + value);
			}
		}
		
		cursor.moveToPosition(0);
	}

	/**
	 * 通过电话号码查找姓名
	 * 
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static String findNameByNumber(String number) {
		// 得到存放姓名那张表的uri
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number);
		// 再通过内容提供者查询这张表,姓名的字段为"display_name"
		Cursor cursor = UiUtils.getContext().getContentResolver()
				.query(uri, new String[] { "display_name" }, null, null, null);
		if (cursor.moveToNext()) {// 如果有下一行,说明有姓名
			String name = cursor.getString(0);
			return name;
		}
		return null;// 如果走这里说明没有下一行，即这个电话没有name
	}

	/**
	 * 通过电话查找联系人id
	 * 
	 * @param number
	 * @return
	 */
	public static int findIdByNumber(String number) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number);
		Cursor cursor = UiUtils.getContext().getContentResolver()
				.query(uri, new String[] { "_id" }, null, null, null);
		if (cursor.getCount() == 0) {
			return -1;// 说明没有这个联系人
		}
		cursor.moveToNext();// 游标移动到下一行
		int phoneId = cursor.getInt(0);// 得到联系人id
		return phoneId;
	}

	/**
	 * 通过电话id找联系人头像
	 * 
	 * @param string
	 * @return
	 */
	public static Bitmap findBitmapById(String string) {
		Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, string);
		InputStream inputStream = Contacts.openContactPhotoInputStream(UiUtils
				.getContext().getContentResolver(), contactUri);
		Bitmap face = BitmapFactory.decodeStream(inputStream);
		if (face == null) {
			// 说明这个联系人没有设置头像
			return null;
		} else {
			return face;
		}
	}

	/**
	 * 根据两个长整形数，判断是否是同一天
	 * 
	 * @param lastDay
	 * @param thisDay
	 * @return
	 */
	public static boolean isSameToday(long lastDay, long thisDay) {
		Time time = new Time();
		time.set(lastDay);

		int thenYear = time.year;
		int thenMonth = time.month;
		int thenMonthDay = time.monthDay;

		time.set(thisDay);
		return (thenYear == time.year) && (thenMonth == time.month)
				&& (thenMonthDay == time.monthDay);
	}
}
