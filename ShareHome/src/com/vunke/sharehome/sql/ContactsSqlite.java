package com.vunke.sharehome.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.widget.Toast;

import com.vunke.sharehome.utils.WorkLog;

public class ContactsSqlite extends Activity {
	private ContactsSql sql;
	private Context context;
	private SQLiteDatabase sqLiteDatabase;

	public ContactsSqlite(Context context) {
		this.context = context;
		sql = new ContactsSql(context);
		// 给权限
		sqLiteDatabase = sql.getWritableDatabase();
	}

	public void addContacts(String name, String number) {
		try {
			if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {
				sqLiteDatabase
						.execSQL("insert into contacts(name,number)values('"
								+ name + "','" + number + "')");
				sqLiteDatabase.close();
				Toast.makeText(context, "添加成功", Toast.LENGTH_LONG).show();
				Activity activity = (Activity) context;
				activity.finish();
			} else {
				if (TextUtils.isEmpty(name)) {
//					WorkLog.e("ContactsSqlite", "name为空");
				}
				if (TextUtils.isEmpty(number)) {
//					WorkLog.e("ContactsSqlite", "number为空");
				}
				Toast.makeText(context, "添加失败", Toast.LENGTH_LONG).show();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "添加成功", Toast.LENGTH_LONG).show();
		}
	}

	// 查询全部
	public List<Map<String, Object>> selectAll() {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map;
		try {

			Cursor cursor = sqLiteDatabase.rawQuery("select * from contacts", null); 
			if (cursor!=null) {
				while (cursor.moveToNext()) {
					map = new HashMap<>();
					String name = cursor.getString(cursor.getColumnIndex("name"));
					String number = cursor.getString(cursor.getColumnIndex("number"));
					String pid = cursor.getString(cursor.getColumnIndex("pid"));
					map.put("name", name);
					map.put("number", number);
					map.put("pid", pid);
					list.add(map);
				}
			}
			cursor.close();
			sqLiteDatabase.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	public List<Map<String, Object>> selectNumber(String number,String name){
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map;
		if (TextUtils.isEmpty(name)) {
			name= "";
		}
		if (TextUtils.isEmpty(number)) {
			number= "";
		}
		try {
			Cursor cursor = sqLiteDatabase.query("contacts", new String[]{"name","number","pid"}, "name = ? or number = ?", new String[]{name,number}, null,null,null);
			while(cursor.moveToNext()){
				map = new HashMap<>();
				String Name = cursor.getString(cursor.getColumnIndex("name"));
				String Number = cursor.getString(cursor.getColumnIndex("number"));
				String pid = cursor.getString(cursor.getColumnIndex("pid"));
				map.put("name", Name);
//				WorkLog.e("ContactsSqlite", "查询的名字"+Name);
				map.put("number", Number);
				map.put("pid", pid);
				list.add(map);
			}
			cursor.close();
			sqLiteDatabase.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 删除
	 * */
	public void deleteContact(String number){
		if (number == "") {
			number = "";
			Toast.makeText(context, "删除失败,没有找到该号码", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			sqLiteDatabase.delete("contacts", "number = ?", new String[]{number});
			sqLiteDatabase.close();
		} catch (Exception e) {
		   e.printStackTrace();
		}
	}
	/**
	 * 更新
	 * */
	public void updateContact(String name,String number,String pid){
		if (TextUtils.isEmpty(pid)) {
			Toast.makeText(context, "修改失败", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(name)) {
			name = "";
		}
		if (TextUtils.isEmpty(number)) {
			number = "";
		}
		try {
			ContentValues values=new ContentValues();
			values.put("name", name);
			values.put("number", number);
			sqLiteDatabase.update("contacts", values, "pid=?", new String[]{pid});
			Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
