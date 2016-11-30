package com.vunke.sharehome.activity;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.asynctask.SearchContact;
import com.vunke.sharehome.asynctask.UpdataContact;
import com.vunke.sharehome.asynctask.SearchContact.SearchContactCallback;
import com.vunke.sharehome.asynctask.UpdataContact.UpdataContactCallback;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.sql.ContactsSqlite;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 修改联系人
 * 
 * @author Administrator
 */
public class ModifyContactActivity extends BaseActivity {
	/**
	 * 返回键 提交
	 */
	private Button modifyattn_back, modifyattn_commit;
	/**
	 * 清除姓名 清除号码
	 */
	private Button modifyattn_clearname, modifyattn_clearnumber;

	/**
	 * 修改联系人姓名 修改联系人号码
	 */
	private EditText modifyattn_name, modifyattn_number;
	/**
	 * 姓名 号码 ID
	 */
	private String name, number, pid;
	/**
	 * 联系人数据库
	 */
	// private ContactsSqlite sqlite;//

	/**
	 * 删除联系人
	 */
	private Button modifyattn_delete;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_modifycontact);
		getExtras();
		init();
	}

	/**
	 * 获取意图
	 */
	private void getExtras() {
		Config.intent = getIntent();
		if (Config.intent.hasExtra("name")) {
			name = Config.intent.getStringExtra("name");
		}
		if (Config.intent.hasExtra("number")) {
			number = Config.intent.getStringExtra("number");
		}
		if (Config.intent.hasExtra("pid")) {
			pid = Config.intent.getStringExtra("pid");
		}
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		modifyattn_back = (Button) findViewById(R.id.modifyattn_back);
		modifyattn_commit = (Button) findViewById(R.id.modifyattn_commit);
		modifyattn_clearname = (Button) findViewById(R.id.modifyattn_clearname);
		modifyattn_clearnumber = (Button) findViewById(R.id.modifyattn_clearnumber);
		modifyattn_name = (EditText) findViewById(R.id.modifyattn_name);
		modifyattn_number = (EditText) findViewById(R.id.modifyattn_number);
		modifyattn_delete = (Button) findViewById(R.id.modifyattn_delete);

		if (!TextUtils.isEmpty(name)) { // 设置姓名
			modifyattn_name.setText(name);
		}
		if (!TextUtils.isEmpty(number)) { // 设置电话号码
			modifyattn_number.setText(number);
		}

		SetOnClickListener(modifyattn_back, modifyattn_commit, modifyattn_clearname, modifyattn_clearnumber,
				modifyattn_delete);

		modifyattn_name.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (TextUtils.isEmpty(s)) {
					modifyattn_clearname.setVisibility(View.GONE);
				} else {
					modifyattn_clearname.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		modifyattn_number.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (TextUtils.isEmpty(s)) {
					modifyattn_clearnumber.setVisibility(View.GONE);
				} else {
					modifyattn_clearnumber.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (TextUtils.isEmpty(UiUtils.GetUserName())) {
			return;
		}
		SharedPreferences sp = getSharedPreferences(Config.UserName, MODE_PRIVATE);
		Long getpid = sp.getLong(Config.LOOKHOME, -1);
		if (!TextUtils.isEmpty(getpid + "") && !getpid.equals("0")) {
			if (Long.parseLong(pid) == getpid) {
				modifyattn_delete.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.modifyattn_back:// 返回
			finish();
			break;
		case R.id.modifyattn_commit:// 提交
			if (TextUtils.isEmpty(modifyattn_name.getText()) && TextUtils.isEmpty(modifyattn_number.getText())) {
				finish();
				break;
			}
			if (TextUtils.isEmpty(modifyattn_name.getText()) && !TextUtils.isEmpty(modifyattn_number.getText())) {
				showToast("姓名为空");
				break;
			}
			if (!TextUtils.isEmpty(modifyattn_name.getText()) && TextUtils.isEmpty(modifyattn_number.getText())) {
				showToast("号码为空");
				break;
			}
			String Name = modifyattn_name.getText().toString();
			String Number = modifyattn_number.getText().toString();
			if (Name.equals(name) && Number.equals(number)) {
				showToast("保存成功");
				finish();
				break;
			}
			if (Number.length() > 11) {
				if (Number.startsWith("9") || Number.startsWith("8")) {
					Number = Number.substring(0);
				}
			}
			if (!UiUtils.isMobileNO(Number)) {
				showToast("温馨提示:请输入正确的手机号码");
				break;
			}
			if (!NetUtils.isNetConnected(mcontext)) {
				showToast("网络出现异常，请检测网络。");
				return;
			}
			// sqlite = new ContactsSqlite(mcontext);
			// sqlite.updateContact(Name, Number, pid);
			ModifyContact(Name, Number);

			break;
		case R.id.modifyattn_clearname:// 清除姓名
			UiUtils.ClearNumber(modifyattn_name);
			break;
		case R.id.modifyattn_clearnumber:// 清楚号码
			UiUtils.ClearNumber(modifyattn_number);
			break;
		case R.id.modifyattn_delete:// 删除联系人
			if (!NetUtils.isNetConnected(mcontext)) {
				showToast("网络出现异常，请检测网络。");
				return;
			}
			DeleteContact();
			break;
		default:
			break;
		}

	}

	/**
	 * 修改联系人 
	 * @param Name  姓名
	 * @param Number 号码
	 */
	private void ModifyContact(String Name, String Number) {
		Contact contact = new Contact(Long.parseLong(pid));
		contact.setHomePhone(Number);
		contact.setContactName(Name);
		DbCore.getDaoSession().getContactDao().update(contact);
		UpdataContact.getInstance(mcontext).initContacts(Name, Number, new UpdataContactCallback() {

			@Override
			public void onSuccess(boolean b, String string, Request request, @Nullable Response response) {
				WorkLog.e("ModifyContact", "response:" + string);
				RxBus.getInstance().post(Config.Update_Contact);
				finish();
			}

			@Override
			public void onFail(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
				WorkLog.e("ModifyContact", "onError");
			}
		});

	}

	/**
	 * 删除联系人
	 */
	public void DeleteContact() {
		SharedPreferences sp = getSharedPreferences(Config.UserName, MODE_PRIVATE);
		Long getpid = sp.getLong(Config.LOOKHOME, -1);
		if (Long.parseLong(pid) == getpid) {
			showToast("不能删除手机看家");
			return;
		}
		// DbCore.getDaoSession().getContactDao().deleteByKey(Long.parseLong(pid));
		// RxBus.getInstance().post(Config.Update_Contact);
		// finish();
		JSONArray jsArray = new JSONArray();
		JSONObject json = new JSONObject();
		try {
			jsArray.put(pid);
			json.put("userName", UiUtils.GetUserName().substring(1));
			json.put("userIds", jsArray);
			WorkLog.e("ModifyContact", "request" + json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.DeleteContact).tag(this).params("json", json.toString())
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean b, String string, Request request, @Nullable Response response) {
						try {
							WorkLog.e("ModifyContact", "response:" + string);
							JSONObject getresponse = new JSONObject(string);
							if (getresponse.has("code")) {
								int code = getresponse.getInt("code");
								switch (code) {
								case 200:
									showToast("删除成功");
									break;
								case 400:
									showToast("删除失败,服务器连接断开");
									break;
								case 500:
									showToast("删除失败，参数错误");
									break;

								default:
									break;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onError(boolean isFromCache, Call call, @Nullable Response response,
							@Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						WorkLog.e("ModifyContact", "OnEror:请求失败");
					}

					@Override
					public void onAfter(boolean isFromCache, @Nullable String t, Call call, @Nullable Response response,
							@Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);
						DbCore.getDaoSession().getContactDao().deleteByKey(Long.parseLong(pid));
						RxBus.getInstance().post(Config.Update_Contact);
						newFriends();
						finish();
					}
				});
	}

	/**
	 * 新的朋友
	 */
	private void newFriends() {
		SearchContact.getInstance(getApplicationContext()).get(new SearchContactCallback() {

			@Override
			public void onSuccess(Newfriendsbean newfriendsbean) {
				Intent intent = new Intent(Config.NEW_FRIENDS_STATUS_CHAGED);

				intent.putExtra("extras", newfriendsbean.data.size());
				sendBroadcast(intent);
			}

			@Override
			public void onFail(int code) {
				Intent intent = new Intent(Config.NEW_FRIENDS_STATUS_CHAGED);
				intent.putExtra("extra", code);
				sendBroadcast(intent);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		OkHttpUtils.getInstance().cancelTag(this);
	}
}
