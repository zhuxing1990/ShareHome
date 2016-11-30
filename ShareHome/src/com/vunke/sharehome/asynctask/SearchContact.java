package com.vunke.sharehome.asynctask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.ContactBean;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.model.Newfriendsbean.Data;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 新的朋友
 * 
 */
public class SearchContact {
	private List<Contact> shareContacts;
	private List<Map<String, Object>> list;
	private List<ContactBean> clientContacts;
	private Map<String, Object> map, map2;
	private String name, phone;
	private AsyncQueryHandler asyncQueryHandler; // 异步查询数据库类对象
	private static final int msgCode = 7758;
	private JSONObject json1, json2, json3;
	private JSONArray jsonarr;
	private List<Data> newfriends;
	private Context context;

	private static SearchContact instance;
	private SearchContactCallback callback;

	public static SearchContact getInstance(Context context) {
//		if (instance == null) {
//			synchronized (SearchContact.class) {
				if (instance == null) {
					instance = new SearchContact(context);
				}
//			}
//		}
		return instance;
	}

	private SearchContact(Context context) {
		this.context = context;

	}

	private void initContacts() throws JSONException {
		json3 = new JSONObject();
		jsonarr = new JSONArray();
		if (clientContacts != null && clientContacts.size() != 0) {
			for (int i = 0; i < clientContacts.size(); i++) {
				name = clientContacts.get(i).getDesplayName();
				phone = clientContacts.get(i).getPhoneNum();
				json2 = new JSONObject();
				json2.put("name", name);
				json2.put("phone", phone);
				jsonarr.put(json2);
			}
			json3.put("clientContacts", jsonarr);
		}
		jsonarr = new JSONArray();
		if (shareContacts != null && shareContacts.size() != 0) {
			for (int i = 0; i < shareContacts.size(); i++) {
				name = shareContacts.get(i).getContactName();
				phone = shareContacts.get(i).getHomePhone();
				// map.put("name", name);
				// map.put("account", phone);
				json2 = new JSONObject();
				json2.put("name", name);
				json2.put("account", phone);
				jsonarr.put(json2);
			}
			json3.put("shareContacts", jsonarr);

		}
		json3.put("userName", UiUtils.GetUserName().substring(1));
		try {
			getUrlRequest(json3.toString(), UrlClient.HttpUrl
					+ UrlClient.GetUserFriend);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getUrlRequest(String json, String url) {
		if (!NetUtils.isNetConnected(context)) {
			WorkLog.e("SearchContact","咦，貌似网络出了点问题");
			return;
		}
//		WorkLog.e("SerachContact", "Request URL:"+url+"\nRequest URL:"+json);
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						try {
//							WorkLog.e("SerachContact", "当前结果" + t);
							JSONObject js = new JSONObject(t);
							int code = js.getInt("code");
							String message = js.getString("message");
							if (js.has("data")) {
								String data = js.getString("data");
							}
							switch (code) {
							case 200:
								Gson gson = new Gson();
								// newfriends = gson.fromJson(
								// js.getString("data"),
								// new TypeToken<List<Newfriendsbean>>() {
								// }.getType());

								Newfriendsbean newfriendsbean = gson.fromJson(
										js.toString(), Newfriendsbean.class);
								if (null != callback)
									callback.onSuccess(newfriendsbean);
								break;
							default:
								if (null != callback)
									callback.onFail(code);
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						callback.onFail(504);
						WorkLog.e("SearchContact", "获取新的朋友失败,发生异常");
					}
				});

	}

	/**
	 * 初始化数据库查询参数
	 */
	public void get(SearchContactCallback callback) {

		this.callback = callback;
		asyncQueryHandler = new MyAsyncQueryHandler(
				context.getContentResolver());
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人Uri；
		// 查询的字段
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
		// 按照sort_key升序查詢
		asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
		shareContacts = DbCore.getDaoSession().getContactDao().loadAll();
	}

	public interface SearchContactCallback {
		void onSuccess(Newfriendsbean newfriendsbean);

		void onFail(int code);
	}

	/**
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie,
				final Cursor cursor) {
			super.onQueryComplete(token, cookie, cursor);
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (cursor != null && cursor.getCount() > 0) {
						clientContacts = new ArrayList<ContactBean>();
						cursor.moveToFirst(); // 游标移动到第一项
						for (int i = 0; i < cursor.getCount(); i++) {
							cursor.moveToPosition(i);
							String name = cursor.getString(1);
							String number = cursor.getString(2);
							String sortKey = cursor.getString(3);
							long contactId = cursor.getLong(4);
							Long photoId = cursor.getLong(5);
							String lookUpKey = cursor.getString(6);

							// 创建联系人对象
							ContactBean contact = new ContactBean();
							contact.setDesplayName(name);
							contact.setPhoneNum(number.replace("-", "").trim());
							contact.setSortKey(sortKey);
							contact.setPhotoId(photoId);
							contact.setLookUpKey(lookUpKey);
							clientContacts.add(contact);
						}
						if (clientContacts.size() > 0) {
							try {
								initContacts();
								if (!cursor.isClosed()) {
									cursor.close();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
	}
	
}
