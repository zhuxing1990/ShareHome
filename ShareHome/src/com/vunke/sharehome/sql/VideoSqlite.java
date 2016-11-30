package com.vunke.sharehome.sql;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class VideoSqlite extends ContentProvider {
	private final static String AUTHORITH = "com.vunke.sharehome.provider";
	private final static String PATH = "/video";
	private final static String TABLE_NAME = "video";
	private final static UriMatcher mUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	private static final int CODE_DIR = 1;
	static {
		mUriMatcher.addURI(AUTHORITH, PATH, CODE_DIR);
	}
	private VideoSql sql;
	private SQLiteDatabase db;
	private static final String USER_NAME = "user_name";
	private static final String CREATE_TIME = "create_time";
	private static final String _ID = "_id";

	@Override
	public boolean onCreate() {
		sql = new VideoSql(getContext());
		return false;
	}

	/**
	 * 
	 * 查询
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		db = sql.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case 1:
			String sql;
			if (selectionArgs!=null&&selectionArgs.length!=0) {
				sql = "select * from "+TABLE_NAME+" where "+USER_NAME+" = '" +selectionArgs[0]+"'" ;
			}else {
				sql = "select * from "+TABLE_NAME;
			}
			sql = sql + " order by create_time desc";
			cursor = db.rawQuery(sql , null);
			/*cursor = db.query(TABLE_NAME, null, sql, null,
					null, null, "create_time des ");*/
			break;
			default :
			
			break;
		}
	
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case CODE_DIR:
			return "vnd.android.cursor.dir/video";

		default:
			throw new IllegalArgumentException("异常参数");
		}
	}

	/**
	 * 
	 * 增加
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db= sql.getWritableDatabase();
		Cursor cursor = db.rawQuery("select "+USER_NAME+" from " + TABLE_NAME
				+ " where "+USER_NAME+" = '" + values.get(USER_NAME)+"'", null);
		// db.query(true, TABLE_NAME, new String[] {
		// "body", "user_id", "create_time" }, null, null, "user_id", null,
		// null, null, null);
		if (cursor.moveToNext()) {// 有下一个 ，更新
			String userName = values.get(USER_NAME).toString();
			db.update(TABLE_NAME, values, USER_NAME+"=?", new String[] { userName });
		} else {// 否则 插入数据
			switch (mUriMatcher.match(uri)) {
			case 1:
				db.insert(TABLE_NAME, null, values);
				break;

			}
		}
		// db.execSQL("delete from groupinfo where rowid not in(select max(rowid) from groupinfo group by user_id)");
		return uri;
	}

	/**
	 * 
	 * 删除
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int number = 0;
		db = sql.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case 1:
			number = db.delete(TABLE_NAME, selection, selectionArgs);
//		case 2:
//			long id = ContentUris.parseId(uri);
//			selection = (selection != null || "".equals(selection.trim()) ? USER_NAME
//					+ "=" + id
//					: selection + "and" + USER_NAME + "=" + id);
//			number = db.delete(TABLE_NAME, selection, selectionArgs);
		}
		return number;
	}

	/**
	 * 
	 * 更新
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int number = 0;
		db = sql.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case 1:
			number = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
//		case 2:
//			long id = ContentUris.parseId(uri);
//			selection = (selection != null || "".equals(selection.trim()) ? USER_NAME
//					+ "=" + id
//					: selection + "and" + USER_NAME + "=" + id);
//			number = db.update(TABLE_NAME, values, selection, selectionArgs);
		}
		return number;
	}

}
