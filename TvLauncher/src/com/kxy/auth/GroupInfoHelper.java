package com.kxy.auth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class GroupInfoHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "groupinfo.db";
	private final static int DATABASE_VERSION = 1;
	public GroupInfoHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table groupinfo(_id integer not null primary key autoincrement ,body varchar,user_id varchar,create_time varchar)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "drop table if exists groupinfo";
		db.execSQL(sql);
	}

}
