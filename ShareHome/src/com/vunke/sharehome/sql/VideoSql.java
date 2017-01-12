package com.vunke.sharehome.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoSql extends SQLiteOpenHelper {

	private static final int version_code = 1;
	private static final String sqlName = "video.db";

	public VideoSql(Context context) {
		super(context, sqlName, null, version_code);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table video (_id integer not null primary key autoincrement,user_name varchar,url varchar,create_time varchar)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "drop table if exists video";
		db.execSQL(sql);
	}
}
