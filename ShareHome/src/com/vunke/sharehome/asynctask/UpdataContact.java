package com.vunke.sharehome.asynctask;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.asynctask.SearchContact.SearchContactCallback;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class UpdataContact {

	private static UpdataContact instance;
	private static Activity context;
	private static List<Contact> addContact;
	private static JSONObject updateJson;
	private static JSONObject updateJsonAll;

	public static UpdataContact getInstance(Activity context) {
		if (instance == null) {
			instance = new UpdataContact(context);
		}
		return instance;
	}

	private UpdataContact(Activity context) {
		this.context = context;
	}

	public  void initContacts(String Name, String phoneNumber,
			final UpdataContactCallback callback) {
		try {
			addContact = UiUtils.PreciseQuery(Name, phoneNumber);
			if (addContact != null && addContact.size() != 0) {
				updateJson = new JSONObject();
				for (int i = 0; i < addContact.size(); i++) {
					updateJson.put("companyPhone", addContact.get(i)
							.getCompanyPhone());
					updateJson.put("contactName", addContact.get(i)
							.getContactName());
					updateJson.put("email", addContact.get(i).getEmail());
					updateJson.put("face", addContact.get(i).getFace());
					updateJson.put("firstName", addContact.get(i)
							.getFirstName());
					updateJson.put("lastName", addContact.get(i).getLastName());
					updateJson.put("groupId", addContact.get(i).getGroupId());
					updateJson.put("homePhone", addContact.get(i)
							.getHomePhone());
					updateJson.put("userId", addContact.get(i).getUserId());
					updateJson.put("userName", addContact.get(i).getUserName());
				}
				JSONArray array = new JSONArray();
				array.put(updateJson);
				updateJsonAll = new JSONObject();
				updateJsonAll.put("contacts", array);
				updateJsonAll.put("userName", UiUtils.GetUserName().substring(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		WorkLog.e("AddAttn", "request:" + updateJsonAll.toString());
		OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.UpdateContact)
				.tag(context).params("json", updateJsonAll.toString())
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean b, String string,
							Request request, @Nullable Response response) {

						if (callback != null) {
							callback.onSuccess(b, string, request, response);
						}
					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						// TODO Auto-generated method stub
						super.onError(isFromCache, call, response, e);
						if (callback != null) {
							callback.onFail(isFromCache, call, response, e);
						}
					}

					@Override
					public void onAfter(boolean isFromCache,
							@Nullable String t, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						newFriends();
//						OkHttpUtils.getInstance().cancelTag(context);
					}
				});
	}
	
	/**
	 * 新的朋友
	 */
	private void newFriends() {
		SearchContact.getInstance(context).get(
				new SearchContactCallback() {

					@Override
					public void onSuccess(Newfriendsbean newfriendsbean) {
						Intent intent = new Intent(
								Config.NEW_FRIENDS_STATUS_CHAGED);

						intent.putExtra("extras", newfriendsbean.data.size());
						context.sendBroadcast(intent);
					}

					@Override
					public void onFail(int code) {
						Intent intent = new Intent(
								Config.NEW_FRIENDS_STATUS_CHAGED);
						intent.putExtra("extra", code);
						context.sendBroadcast(intent);
					}
				});
	}
	
	public interface UpdataContactCallback {
		void onSuccess(boolean b, String string, Request request,
				@Nullable Response response);

		void onFail(boolean isFromCache, Call call,
				@Nullable Response response, @Nullable Exception e);
	}
}
