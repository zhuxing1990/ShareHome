package com.kxy.auth;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class UserGroupInfoProvider extends ContentProvider {
	// public static final Uri Group_INFO_USERID = Uri
	// .parse("content://com.vunke.tvlauncher.provider/groupinfo");
	private final static String AUTHORITH = "com.vunke.tvlauncher.provider";
	private final static String PATH = "/groupinfo";
	private final static String PATHS = "/groupinfo/#";

	private final static String TABLE_NAME = "groupinfo";

	private final static UriMatcher mUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	private static final int CODE_DIR = 1;
	private static final int CODE_ITEM = 2;
	static {
		mUriMatcher.addURI(AUTHORITH, PATH, CODE_DIR);
		mUriMatcher.addURI(AUTHORITH, PATHS, CODE_ITEM);
	}
	private GroupInfoHelper dbHelper;
	private SQLiteDatabase db;
	private static final String USER_ID = "user_id";
	private static final String BODY = "body";
	private static final String _ID = "_id";
	private static final String CREATE_TIME = "create_time";

	@Override
	public boolean onCreate() {
		dbHelper = new GroupInfoHelper(getContext());
		return false;
	}

	/**
	 * 查询 (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 *      java.lang.String[], java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] columns, String selection,
			String[] selectionArgs, String orderBy) {
		Cursor cursor = null;
		db = dbHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case 1:
			String sql;
			if (selectionArgs!=null&&selectionArgs.length!=0) {
				sql = "select * from groupinfo where user_id = '" +selectionArgs[0]+"'" ;
			}else {
				sql = "select * from groupinfo ";
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
			return "vnd.android.cursor.dir/groupinfo";
		case CODE_ITEM:
			return "vnd.android.cursor.item/groupinfo";

		default:
			throw new IllegalArgumentException("异常参数");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select user_id from " + TABLE_NAME
				+ " where user_id = '" + values.get("user_id")+"'", null);
		// db.query(true, TABLE_NAME, new String[] {
		// "body", "user_id", "create_time" }, null, null, "user_id", null,
		// null, null, null);
		if (cursor.moveToNext()) {// 有下一个 ，更新
			String userID = values.get("user_id").toString();
			db.update(TABLE_NAME, values, "user_id=?", new String[] { userID });
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

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int number = 0;
		db = dbHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case 1:
			number = db.delete(TABLE_NAME, selection, selectionArgs);
		case 2:
			long id = ContentUris.parseId(uri);
			selection = (selection != null || "".equals(selection.trim()) ? USER_ID
					+ "=" + id
					: selection + "and" + USER_ID + "=" + id);
			number = db.delete(TABLE_NAME, selection, selectionArgs);
		}
		return number;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int number = 0;
		db = dbHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case 1:
			number = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		case 2:
			long id = ContentUris.parseId(uri);
			selection = (selection != null || "".equals(selection.trim()) ? USER_ID
					+ "=" + id
					: selection + "and" + USER_ID + "=" + id);
			number = db.update(TABLE_NAME, values, selection, selectionArgs);
		}
		return number;
	}

}
