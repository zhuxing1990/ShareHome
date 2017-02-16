package com.vunke.sharehome.activity;

import java.util.List;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.adapter.NewFriendsAdapter;
import com.vunke.sharehome.asynctask.SearchContact;
import com.vunke.sharehome.asynctask.SearchContact.SearchContactCallback;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.model.Newfriendsbean.Data;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.UpdateContactService;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.CircularProgress;

/**
 * 新的朋友
 */
public class NewFriends extends BaseActivity {
	private Button newfriends_back;
	// private List<Contact> shareContacts;
	// private List<Map<String, Object>> list;
	// private List<ContactBean> clientContacts;
	// private String name, phone;
	// private AsyncQueryHandler asyncQueryHandler; // 异步查询数据库类对象
	// private JSONObject json1, json2, json3;
	// private JSONArray jsonarr;
	/**
	 * 新的朋友   列表集合 
	 */
	private List<Data> newfriends;
	/**
	 * 新的朋友ListView控件
	 */
	private ListView newfriends_listview;
	/**
	 * 新的朋友设配器
	 */
	private NewFriendsAdapter adapter;
	/**
	 * 进度条
	 */
	private CircularProgress newfriends_progress;
	/**
	 * 没有新的朋友的提示
	 */
	private TextView not_friends;
	private Handler handler = new Handler() {

	};

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_newfriends);
		init();
		initListener();
		// init2();
		
		initNewFriends();
	}

	/**
	 * 初始化新的朋友
	 */
	private void initNewFriends() {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("当前网络不可用，请检查设置");
			stopProgress();
			not_friends.setVisibility(View.VISIBLE);
			return;
		}
		SearchContact.getInstance(this).get(new SearchContactCallback() {

			@Override
			public void onSuccess(Newfriendsbean newfriendsbean) {
				stopProgress();
				newfriends = newfriendsbean.data;
				if (newfriends.size() != 0 && newfriends != null) {
					adapter = new NewFriendsAdapter(newfriends, mcontext);
					newfriends_listview.setAdapter(adapter);
					not_friends.setVisibility(View.GONE);
				} else {
					not_friends.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onFail(int code) {
				// UiUtils.showToast(""+code);
				stopProgress();
				WorkLog.i("NewFriends", "" + code);
				not_friends.setVisibility(View.VISIBLE);
			}
		});

	}

	/**
	 * 停止旋转动画
	 */
	public void stopProgress() {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				newfriends_progress.setVisibility(View.GONE);
				newfriends_progress.stop();
			}
		}, 500);
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		newfriends_back = (Button) findViewById(R.id.newfriends_back);
		newfriends_listview = (ListView) findViewById(R.id.newfriends_listview);
		not_friends = (TextView) findViewById(R.id.not_friends);
		newfriends_progress = (CircularProgress) findViewById(R.id.newfriends_progress);
	}

	private void initListener() {
		SetOnClickListener(newfriends_back,not_friends);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.newfriends_back:
			finish();
			break;
		case R.id.not_friends:
			initNewFriends();
			break;
		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (newfriends!=null) {
			RxBus.getInstance().post(Config.Update_Contact);
			newfriends=null;
			initData();
		}
	}
	/**
	 * 初始化数据
	 */
	private void initData() {
		Config.intent = new Intent(mcontext, UpdateContactService.class);
		Config.intent.putExtra("param", Config.Upload_Contact);
		startService(Config.intent);
	}
}
