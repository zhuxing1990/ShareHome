package com.vunke.sharehome.activity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.asynctask.UpdataContact;
import com.vunke.sharehome.asynctask.UpdataContact.UpdataContactCallback;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.sql.ContactsSqlite;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.PinyinUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 添加联系人
 * */
public class AddAttnActivity extends BaseActivity {
	/**
	 *  返回键，提交，清除姓名，清除号码
	 */
	private Button addattn_back, addattn_commit, addattn_clearname,
			addattn_clearnumber;
	/**
	 *  号码输入框
	 */
	private EditText addattn_number;
	/**
	 *  姓名输入框
	 */
	private EditText addattn_name;
	/**
	 *  姓名，号码
	 */
	private String name, number;
	/**
	 *  想家联系人Bean
	 */
	private Contact contact;
	/**
	 * 搜索联系人
	 */
	private List<ContactSummary> searchContact;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_addattn);
		init();
		getExtra();
	}

	/**
	 * 获取意图
	 */
	private void getExtra() {
		Config.intent = getIntent();

		if (Config.intent.hasExtra("number")) {
			addattn_number.setText(Config.intent.getStringExtra("number"));
			String number = Config.intent.getStringExtra("number");
//			WorkLog.e("AddAttnActivity", "获取当前号码" + number);
			searchContact = ContactApi.searchContact(number,
					ContactApi.LIST_FILTER_ALL);
			if (searchContact != null && searchContact.size() != 0) {
				for (int i = 0; i < searchContact.size(); i++) {
					ContactSummary position = searchContact.get(i);
//					WorkLog.e("AddAttnActivity",
//							"当前名字" + position.getDisplayName());
//					WorkLog.e("AddAttnActivity",
//							"当前搜索的号码" + position.getSearchMatchContent());
					if (number.equals(position.getSearchMatchContent())) {
						addattn_name.setText(position.getDisplayName());
						return;
					}
				}
			}
		}
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		addattn_back = (Button) findViewById(R.id.addattn_back);// 返回键
		addattn_commit = (Button) findViewById(R.id.addattn_commit);// 提交
		addattn_clearname = (Button) findViewById(R.id.addattn_clearname);// 清除姓名
		addattn_clearnumber = (Button) findViewById(R.id.addattn_clearnumber);// 清除号码

		addattn_name = (EditText) findViewById(R.id.addattn_name);// 输入框姓名
		addattn_number = (EditText) findViewById(R.id.addattn_number);// 输入框 号码
		SetOnClickListener(addattn_back, addattn_commit, addattn_clearname,
				addattn_clearnumber);// 点击事件

		addattn_name.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(s)) {
					addattn_clearname.setVisibility(View.VISIBLE);// 显示 清除姓名
				} else {
					addattn_clearname.setVisibility(View.INVISIBLE);// 隐藏 清除姓名
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		addattn_number.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(s)) {
					addattn_clearnumber.setVisibility(View.VISIBLE);// 显示 清除号码
					if (UiUtils.isMobileNO(addattn_number.getText().toString())) {
						// WorkLog.e("AddAttnActivity", "是手机号码");
						if (TextUtils.isEmpty(addattn_name.getText())) {
							List<ContactSummary> searchContact2 = ContactApi
									.searchContact(addattn_number.getText()
											.toString(),
											ContactApi.LIST_FILTER_ALL);
							if (searchContact2 != null
									&& searchContact2.size() != 0) {
								for (int i = 0; i < searchContact2.size(); i++) {
									ContactSummary position = searchContact2
											.get(i);
//									WorkLog.e("AddAttnActivity", "当前名字"
//											+ position.getDisplayName());
//									WorkLog.e("AddAttnActivity", "当前搜索的号码"
//											+ position.getSearchMatchContent());
									if (addattn_number
											.getText()
											.toString()
											.equals(position
													.getSearchMatchContent())) {
										addattn_name.setText(position
												.getDisplayName());
										return;
									}
								}
							}
						} else {
//							WorkLog.e("AddAttnActivity", "已经填写了姓名");
						}
					}
				} else {
					addattn_clearnumber.setVisibility(View.INVISIBLE);// 隐藏 清除号码
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		// 长按清空姓名
		addattn_clearname.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				addattn_name.setText("");
				return false;
			}
		});
		// 长按清空号码
		addattn_clearnumber.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				addattn_number.setText("");
				return false;
			}
		});

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.addattn_back:// 返回键
			finish();
			break;
		case R.id.addattn_commit:// 提交
			// judge();
			judge2();
			break;
		case R.id.addattn_clearname:// 清除姓名
			if (!TextUtils.isEmpty(addattn_name.getText())) {
				UiUtils.ClearNumber(addattn_name);
			}
			break;
		case R.id.addattn_clearnumber:// 清除号码
			if (!TextUtils.isEmpty(addattn_number.getText())) {
				UiUtils.ClearNumber(addattn_number);
			}
			break;

		default:
			break;
		}

	}

	/**
	 * 添加联系人
	 */
	private void judge2() {
		name = addattn_name.getText().toString().trim();
		number = addattn_number.getText().toString().trim();
		if (TextUtils.isEmpty(addattn_name.getText())
				&& TextUtils.isEmpty(addattn_number.getText())) {
			finish();
			return;
		}
		if (TextUtils.isEmpty(addattn_name.getText())
				&& !TextUtils.isEmpty(addattn_number.getText())) {
			showToast("姓名不能为空 ");
			return;
		}
		if (TextUtils.isEmpty(addattn_number.getText())
				&& !TextUtils.isEmpty(addattn_name.getText())) {
			showToast("号码不能为空 ");
			return;
		}
		if (!UiUtils.isMobileNO(addattn_number.getText().toString().trim())) {
			showToast("温馨提示:请输入正确的手机号码");
			return;
		}
		if (TextUtils.isEmpty(name)) {
			showToast("输入姓名格式不正确");
			addattn_name.setText("");
			return;
		}
		if (TextUtils.isDigitsOnly(name)) {
			showToast("姓名不能为数字");
			addattn_name.setText("");
			return;
		}
		try {
			String getPinyin = PinyinUtils.getPinyin(name);
			// WorkLog.e("AddAttnActivity", getPinyin+"getPinyin");
		} catch (Exception e) {
			e.printStackTrace();
			showToast("您输入的姓名有误，请重新输入");
			addattn_name.setText("");
			return;
		}
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("网络出现异常，请检测网络。");
			return;
		}
		// WorkLog.e("AddAttnActivity", "name" + name);
		if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {
			contact = new Contact();
			contact.setContactName(name);
			contact.setHomePhone(number);
			contact.setGroupId("1");
			long insert = DbCore.getDaoSession().getContactDao()
					.insert(contact);
			if (insert > 0) {
				UpdataContact();
				
			} else {
				showToast("添加失败");
			}
		} else {
			showToast("添加失败，请重新输入");
			addattn_name.setText("");
		}

	}

	/**
	 * 添加成功 更新联系人
	 */
	private void UpdataContact() {
		UpdataContact.getInstance(mcontext).initContacts(name, number, new UpdataContactCallback() {
			
			@Override
			public void onSuccess(boolean b, String string, Request request,
					@Nullable Response response) {
//				WorkLog.e("AddAttn", "response:"+string);
				showToast("保存成功");
				RxBus.getInstance().post(Config.Update_Contact);
				finish();
			}
			
			@Override
			public void onFail(boolean isFromCache, Call call,
					@Nullable Response response, @Nullable Exception e) {
				WorkLog.e("AddAttn", "onError");
				showToast("同步联系人失败");
			}
			
		});
	}

}
