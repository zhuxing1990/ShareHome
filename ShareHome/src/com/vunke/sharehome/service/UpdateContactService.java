package com.vunke.sharehome.service;

import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.asynctask.SearchContact;
import com.vunke.sharehome.asynctask.SearchContact.SearchContactCallback;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

public class UpdateContactService extends IntentService {
	private JSONObject updateJson, updateJsonAll;

	public UpdateContactService() {
		super("UpdateContactService");

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			if (intent.hasExtra("param")) {
				String param = intent.getExtras().getString("param");
				if (param.equals(Config.Upload_Contact)) {
					UploadContacts();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 上传并更新想家联系人
	 * 
	 * @throws JSONException
	 */
	public void UploadContacts() throws JSONException {
		List<Contact> loadAll = DbCore.getDaoSession().getContactDao()
				.loadAll();
		if (loadAll != null && loadAll.size() != 0) {
			JSONArray array = new JSONArray();
			for (int i = 0; i < loadAll.size(); i++) {
				String phone = loadAll.get(i).getHomePhone();
				String tvuser = UiUtils.GetTVUserName(getApplicationContext());
				if (phone.equals(tvuser)) {
					continue;
				}
				updateJson = new JSONObject();
				updateJson
						.put("companyPhone", loadAll.get(i).getCompanyPhone());
				updateJson.put("contactName", loadAll.get(i).getContactName());
				updateJson.put("email", loadAll.get(i).getEmail());
				updateJson.put("face", loadAll.get(i).getFace());
				updateJson.put("firstName", loadAll.get(i).getFirstName());
				updateJson.put("lastName", loadAll.get(i).getLastName());
				updateJson.put("groupId", loadAll.get(i).getGroupId());
				updateJson.put("homePhone", loadAll.get(i).getHomePhone());
				updateJson.put("userId", loadAll.get(i).getUserId());
				updateJson.put("userName", loadAll.get(i).getUserName());
				array.put(updateJson);
			}
			
			updateJsonAll = new JSONObject();
			updateJsonAll.put("contacts", array);
			updateJsonAll.put("userName", UiUtils.GetUserName(getApplicationContext()).substring(1));
//			WorkLog.i("UpdateContactService",  "Request URL:" + UrlClient.HttpUrl
//					 + UrlClient.UpdateContact );
			 WorkLog.i("UpdateContactService",  "Request data:"
			 + updateJsonAll.toString());
			OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.UpdateContact)
					.tag(this).params("json", updateJsonAll.toString())
					.execute(new StringCallback() {

						@Override
						public void onResponse(boolean isFromCache, String t,
								Request request, @Nullable Response response) {
							try {
//								WorkLog.i("UpdateContactService", "data:" + t);
								JSONObject json = new JSONObject(t);
								int code = json.getInt("code");
								switch (code) {
								case 200:
									WorkLog.i(
											"UpdateContactService",
											"UpdateContact:"
													+ json.getString("upcount")
													+ "UploadContact:"
													+ json.getString("incount"));
									break;
								case 400:
									WorkLog.i("UpdateContactService", "code:"
											+ code);
									break;
								case 500:
									WorkLog.i("UpdateContactService", "code:"
											+ code);
									break;

								default:
									break;
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onError(boolean isFromCache, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onError(isFromCache, call, response, e);
							OkHttpUtils.getInstance().cancelTag(this);
							WorkLog.i("UpdateContactService", "Request error");
						}

						@Override
						public void onAfter(boolean isFromCache,
								@Nullable String t, Call call,
								@Nullable Response response,
								@Nullable Exception e) {
							super.onAfter(isFromCache, t, call, response, e);
							newFriends() ;
						}

					});
		}
	}

	/**
	 * 新的朋友
	 */
	private void newFriends() {
		SearchContact.getInstance(getApplicationContext()).get(
				new SearchContactCallback() {

					@Override
					public void onSuccess(Newfriendsbean newfriendsbean) {
						Intent intent = new Intent(
								Config.NEW_FRIENDS_STATUS_CHAGED);

						intent.putExtra("extras", newfriendsbean.data.size());
						sendBroadcast(intent);
					}

					@Override
					public void onFail(int code) {
						Intent intent = new Intent(
								Config.NEW_FRIENDS_STATUS_CHAGED);
						intent.putExtra("extra", code);
						sendBroadcast(intent);
					}
				});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
