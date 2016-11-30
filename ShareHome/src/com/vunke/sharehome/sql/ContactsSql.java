package com.vunke.sharehome.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactsSql extends SQLiteOpenHelper {

	public ContactsSql(Context context) {
		super(context,"contacts.db",null,1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table contacts (_id integer not null primary key autoincrement,name varchar(50),number varchar(15))");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "drop table if exists contacts";
		db.execSQL(sql);
	}

}
