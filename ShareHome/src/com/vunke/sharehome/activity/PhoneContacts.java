package com.vunke.sharehome.activity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar.LayoutParams;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.lzy.okhttputils.OkHttpUtils;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.adapter.BaseHolder;
import com.vunke.sharehome.adapter.DefaultAdapter;
import com.vunke.sharehome.asynctask.SearchContact;
import com.vunke.sharehome.asynctask.SearchContact.SearchContactCallback;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.UiUtils;

/**
 * 手机联系人  已弃用
 * 
 */
public class PhoneContacts extends BaseActivity {
	private final static int REFRESH_LIST = 1;// 更新 集合
	private final static int INIT_LIST = 0;// 更新 集合
	private final static int INIT_SEARCH = 2;
	private ListView contacts_listview;
	private LinearLayout phonecontacts_search;
	private List<ContactSummary> contacts;
	private LruCache<Long, Bitmap> photoCache;// 头像缓存
	private ContactsListAdapter adapter, adapter2;// 本地联系人适配器
	private BroadcastReceiver mPresenceReceiver;// 广播
	private ContentObserver mContactObserver;// 内容观察者
	private Button phonecontacts_back;
	private TextView contacts_searchtext, not_contacts;
	private PopupWindow popupWindow; // 搜索界面
	private View popupView;
	private Button popuwindow_dismiss, popupwindow_clear;
	private EditText popupwindow_search;
	private ListView popupwindow_listview;
	private List<ContactSummary> searchContact;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_LIST:
				// refresh contact list
				refreshList();
				break;
			case INIT_LIST:
				if (contacts.size() != 0 || contacts != null) {// 判断联系人列表是否不为空
					adapter = new ContactsListAdapter(contacts);
					contacts_listview.setAdapter(adapter);
					contacts_searchtext.setHint("在" + adapter.getCount()
							+ "人中搜索");
					popupwindow_search.setHint("在" + adapter.getCount()
							+ "人中搜索");
					not_contacts.setVisibility(View.GONE);
				} else {
					contacts_listview.setAdapter(null);
					popupwindow_search.setHint("在0人中搜索");
					contacts_searchtext.setHint("在0人中搜索");
					not_contacts.setVisibility(View.VISIBLE);
				}
				break;
			case INIT_SEARCH:
				if (searchContact.size() != 0 || null != searchContact) {
					adapter2 = new ContactsListAdapter(searchContact);
					popupwindow_listview.setVisibility(View.VISIBLE);
					popupwindow_listview.setAdapter(adapter2);
				} else {
					popupwindow_listview.setVisibility(View.GONE);
					popupwindow_listview.setAdapter(null);// 清空设配器
				}
				break;
			default:
				break;
			}
		}

	};

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_phonecontacts);
		init();
		initListener();
		initContact();
	}

	public void init() {
		contacts_listview = (ListView) findViewById(R.id.phonecontacts_listview);
		phonecontacts_back = (Button) findViewById(R.id.phonecontacts_back);
		contacts_searchtext = (TextView) findViewById(R.id.contacts_searchtext);
		phonecontacts_search = (LinearLayout) findViewById(R.id.phonecontacts_search);
		not_contacts = (TextView) findViewById(R.id.not_contacts);

		/* popuwindow 的布局 */
		popupView = View.inflate(mcontext, R.layout.popupwindow_search, null);
		popuwindow_dismiss = (Button) popupView // 撤销
				.findViewById(R.id.popuwindow_dismiss);
		popupwindow_clear = (Button) popupView // 清楚输入框内容
				.findViewById(R.id.popupwindow_clear);
		popupwindow_search = (EditText) popupView // 输入框
				.findViewById(R.id.popupwindow_search);
		popupwindow_listview = (ListView) popupView // listview
				.findViewById(R.id.popupwindow_listview);
		/* popuwindow 的布局 */
	}

	private void initListener() {
		SetOnClickListener(phonecontacts_back, phonecontacts_search,
				not_contacts, popupwindow_clear, popuwindow_dismiss);
		contacts_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Config.intent = new Intent(mcontext,
						ContactDetailActivity.class);
				Config.intent.putExtra("id", contacts.get(position)
						.getContactId());
				startActivity(Config.intent);

			}
		});
		popupwindow_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				popupwindow_listview.setVisibility(TextUtils.isEmpty(s) ? View.VISIBLE
						: View.INVISIBLE);
				popupwindow_clear.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE
						: View.INVISIBLE);
				if (!TextUtils.isEmpty(s)) {
					String search = popupwindow_search.getText().toString();
					searchContact = ContactApi.searchContact(search,
							ContactApi.LIST_FILTER_ALL);
					mHandler.sendEmptyMessage(INIT_SEARCH);
				} else {
					popupwindow_listview.setVisibility(View.GONE);
					popupwindow_listview.setAdapter(null);// 清空设配器
				}
			}
		});
		popupwindow_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Config.intent = new Intent(mcontext,
						ContactDetailActivity.class);
				Config.intent.putExtra("id", searchContact.get(position)
						.getContactId());
				startActivity(Config.intent);

			}
		});
	}

	private void initContact() {
		// 当电话信息发生改变时，注册一个接收器
		registerPhoneChangeReceiver();
		// 注册一个联系人信息时有变化的广播
		registerContactObserver();
		int photoCacheSize = Math.round(0.25f * Runtime.getRuntime()
				.maxMemory() / 1024);
		photoCache = new LruCache<Long, Bitmap>(photoCacheSize) {
			protected int sizeOf(Long key, Bitmap bitmap) {
				final int bitmapSize = getBitmapSize(bitmap) / 1024;
				return bitmapSize == 0 ? 1 : bitmapSize;
			}

			private int getBitmapSize(Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
		contacts = ContactApi.getContactSummaryList(ContactApi.LIST_FILTER_ALL);
		mHandler.sendEmptyMessage(INIT_LIST);
	}

	/**
	 * 更新联系人列表,和干净的图片缓存
	 */
	private void refreshList() {
		if (photoCache != null) {
			photoCache.evictAll();
		}
		contacts = ContactApi.getContactSummaryList(ContactApi.LIST_FILTER_ALL);
		mHandler.sendEmptyMessage(INIT_LIST);
	};

	/**
	 * obtain the portrait icon of a contact.
	 */
	public Bitmap getPhoto(ContactSummary contactSummary) {
		if (photoCache == null)
			return null;
		// get the portrait icon in cache
		Bitmap bmp = photoCache.get(contactSummary.getContactId());
		if (bmp != null) {// didn't find photo in cache
			return bmp;
		} else {// get the photo by sdk interface
			bmp = contactSummary.getPhoto(getApplicationContext());
			if (bmp != null) {
				photoCache.put(contactSummary.getContactId(), bmp);
			}
			return photoCache.get(contactSummary.getContactId());
		}
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.phonecontacts_back:
			finish();
			break;
		case R.id.not_contacts:
			refreshList();
			break;
		case R.id.phonecontacts_search:
			showPopupWindow(phonecontacts_search);
			break;
		case R.id.popuwindow_dismiss:
			if (popupWindow != null) {
				popupWindow.dismiss();
			}
			break;
		case R.id.popupwindow_clear:
//			UiUtils.ClearNumber(popupwindow_search); //有卡顿现象，更换删除方式
//			ClearNumber();
			UiUtils.ClearNumber2(popupwindow_search);
			break;
		default:
			break;
		}
	}
	
	private void showPopupWindow(View view) {
		if (popupWindow != null) {
			popupWindow.dismiss();
		}

		popupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		popupWindow.setTouchable(true); // 设置PopupWindow可触摸
		popupWindow.setFocusable(true); // 设置PopupWindow可获得焦点
		popupWindow.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
		// 在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
		popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		popupWindow
				.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		backgroundAlpha(0.7f);
		// 添加pop窗口关闭事件
		popupWindow.setOnDismissListener(new poponDismissListener());
		popupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				return false;
			}
		});
		// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
		// 我觉得这里是API的一个bug
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
				location[1] - popupWindow.getHeight());

		// popupWindow.showAsDropDown(view);
		popupInputMethodWindow();
	}

	private void popupInputMethodWindow() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) popupwindow_search
						.getContext().getSystemService(
								Service.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 0);
	}

	/**
	 * 添加新笔记时弹出的popWin关闭的事件，主要是为了将背景透明度改回来
	 * 
	 */
	class poponDismissListener implements PopupWindow.OnDismissListener {

		@Override
		public void onDismiss() {
			// Log.v("List_noteTypeActivity:", "我是关闭事件");
			popupwindow_listview.setAdapter(null);
			backgroundAlpha(1f);
		}

	}

	/**
	 * 设置添加屏幕的背景透明度
	 * 
	 * @param bgAlpha
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}

	/**
	 * 注册电话号码发生改变
	 * */
	private void registerPhoneChangeReceiver() {
		mPresenceReceiver = new BroadcastReceiver() {
			private boolean isIdleRefresh = true;

			public void onReceive(Context context, Intent intent) {
				// refresh contact list after 3 second
				if (isIdleRefresh) {
					isIdleRefresh = false;
					new Timer().schedule(new TimerTask() {
						public void run() {
							mHandler.sendEmptyMessage(REFRESH_LIST);
							isIdleRefresh = true;
						}
					}, 3000);
				}
			}
		};
		IntentFilter phoneInfoChangeFilter = new IntentFilter(
				ContactApi.EVENT_PHONEINFO_CHANGED);

		LocalBroadcastManager.getInstance(mcontext).registerReceiver(
				mPresenceReceiver, phoneInfoChangeFilter);
	}

	/**
	 * 注册观察联系人
	 */
	private void registerContactObserver() {
		mContactObserver = new ContentObserver(new Handler()) {
			public void onChange(boolean selfChange) {
				mHandler.sendEmptyMessage(REFRESH_LIST);
			}
		};
		getContentResolver().registerContentObserver(Contacts.CONTENT_URI,
				true, mContactObserver);

	}

	/**
	 *  注销广播
	 */
	private void unregisterPresenceReceiver() {
		LocalBroadcastManager.getInstance(mcontext).unregisterReceiver(
				mPresenceReceiver);
		if (mContactObserver != null) {
			getContentResolver().unregisterContentObserver(mContactObserver);
		}
		mPresenceReceiver = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterPresenceReceiver();
		if (contacts != null) {
			contacts.clear();
		}
		if (photoCache != null) {
			photoCache.evictAll();
		}
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		if (searchContact != null) {
			searchContact.clear();
		}
	}

	public class ContactsListAdapter extends DefaultAdapter<ContactSummary> {
		private ContactSummary mContact;

		protected ContactsListAdapter(List<ContactSummary> list) {
			super(list);

		}

		@Override
		protected BaseHolder getHolder() {
			return new ContactsListHodler();
		}

	}

	public class ContactsListHodler extends BaseHolder<ContactSummary> {
		private long id;
		private ImageView portrait;
		private TextView name, mood, notReadNum, rcsOnline, usertype;

		@Override
		protected View initView() {
			View view = View
					.inflate(mcontext, R.layout.listview_contacts, null);
			portrait = (ImageView) view.findViewById(R.id.contacts_portrait);
			name = (TextView) view.findViewById(R.id.contacts_name);
			mood = (TextView) view.findViewById(R.id.contacts_mood);
			rcsOnline = (TextView) view.findViewById(R.id.rcs_online);
			usertype = (TextView) view.findViewById(R.id.contacts_usertype);
			return view;
		}

		@Override
		protected void refreshView(ContactSummary data, int position,
				ViewGroup parent) {
			// load contact's information
			id = data.getContactId();
			name.setText(data.getDisplayName());
			showStatus(data, rcsOnline);
			mood.setText(data.getNote());
			// load contact's portrait
			Bitmap bmp = getPhoto(data);
			if (bmp == null) {
				portrait.setImageResource(R.drawable.call_icon3);
			} else {
				portrait.setImageBitmap(bmp);
			}
		}

		/**
		 * show contact's status
		 */
		private void showStatus(ContactSummary contact, TextView statusView) {
			if (ContactApi.STAT_ONLINE == contact.getStatus()) {
				statusView.setVisibility(View.VISIBLE);
				statusView.setText("online");
			} else if (ContactApi.STAT_OFFLINE == contact.getStatus()) {
				statusView.setVisibility(View.VISIBLE);
				statusView.setText("offline");
			} else {
				statusView.setVisibility(View.GONE);
			}
		}
	}
}
