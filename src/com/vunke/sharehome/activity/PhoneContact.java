package com.vunke.sharehome.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.Call.CallOut_Activity;
import com.vunke.sharehome.adapter.SearchContact2Adapter;
import com.vunke.sharehome.adapter.SearchContactAdapter;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.model.ContactBean;
import com.vunke.sharehome.model.ShareContactsBean;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.ActionSheetDialog;
import com.vunke.sharehome.view.ActionSheetDialog.OnSheetItemClickListener;

/**
 * 手机联系人
 * 
 * @author Administrator
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class PhoneContact extends BaseActivity {
	private static final int InItContacts = 748630;
	private final static int REFRESH_LIST = 0x1000;// 更新 集合
	private final static int INIT_SEARCH = 0x1001;// 更新搜索
	private ListView contacts_listview;
	private LinearLayout phonecontacts_search;
	private Button phonecontacts_back;
	private TextView contacts_searchtext, not_contacts;
	private PopupWindow popupWindow; // 搜索界面
	private View popupView;
	private Button popuwindow_dismiss, popupwindow_clear;
	private ListView popupwindow_listview;
	private EditText popupwindow_search;
	private List<ContactBean> clientContacts;// 联系人集合
	private AsyncQueryHandler asyncQueryHandler; // 异步查询数据库类对象
	// private List<ShareContactsBean> contactsList = new
	// ArrayList<ShareContactsBean>();
	private ShareContactAdapter contactsadapter;
	private BroadcastReceiver mPresenceReceiver;// 广播
	private ContentObserver mContactObserver;// 内容观察者
	private List<ContactSummary> searchContact;
	private SearchContact2Adapter Searchadapter;
	private ShareContactsBean bean;
	private Map<Integer, Boolean> map;
	private AlertDialog alertDialog;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case InItContacts:
				if (clientContacts != null && clientContacts.size() != 0) {
					if (map == null) {
						map = new HashMap<>();
					}
					contactsadapter = new ShareContactAdapter(mcontext,
							clientContacts, map);
					contacts_listview.setAdapter(contactsadapter);
					not_contacts.setVisibility(View.GONE);
					popupwindow_search.setHint("在" + contactsadapter.getCount()
							+ "人中搜索");
					contacts_searchtext.setHint("在"
							+ contactsadapter.getCount() + "人中搜索");
				} else {
					popupwindow_search.setHint("在0人中搜索");
					contacts_searchtext.setHint("在0人中搜索");
					contacts_listview.setAdapter(null);
					not_contacts.setVisibility(View.VISIBLE);
				}
				break;
			case REFRESH_LIST:
				initContact();
				if (!TextUtils.isEmpty(popupwindow_search.getText())) {
					String search = popupwindow_search.getText().toString();
					searchContact = ContactApi.searchContact(search,
							ContactApi.LIST_FILTER_ALL);
					handler.sendEmptyMessage(INIT_SEARCH);
				}
				break;
			case INIT_SEARCH:
				if (searchContact != null && searchContact.size() != 0) {
					Searchadapter = new SearchContact2Adapter(mcontext,
							searchContact);
					// Searchadapter = new ContactsListAdapter(searchContact);
					popupwindow_listview.setVisibility(View.VISIBLE);
					popupwindow_listview.setAdapter(Searchadapter);
				} else {
					popupwindow_listview.setVisibility(View.GONE);
					popupwindow_listview.setAdapter(null);// 清空设配器
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_phonecontacts);
		init();
		initListener();
		initContact();
	}

	/**
	 * 初始化控件
	 */
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
		// 当电话信息发生改变时，注册一个接收器
		registerPhoneChangeReceiver();
		// 注册一个联系人信息时有变化的广播
		registerContactObserver();
		SetOnClickListener(phonecontacts_back, phonecontacts_search,
				not_contacts, popupwindow_clear, popuwindow_dismiss);

		contacts_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				Config.intent = new Intent(mcontext,
						ContactDetailActivity.class);
				// WorkLog.i("PhoneContact",
				// "联系人Id>>"+clientContacts.get(position).getContactId());
				Config.intent.putExtra("id", clientContacts.get(position)
						.getContactId());
				startActivity(Config.intent);
			}
		});
		
		 registerForContextMenu(popupwindow_search);
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
					handler.sendEmptyMessage(INIT_SEARCH);
				} else {
					popupwindow_listview.setVisibility(View.GONE);
					popupwindow_listview.setAdapter(null);// 清空设配器
				}
			}
		});
		popupwindow_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				CallNumber(position);
			}
		});
	}

	/**
	 * 拨号
	 * 
	 * @param position
	 */
	private void CallNumber(int position) {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		if (UiUtils.isCameraCanUse() == false) {
			NoPermission();
			return;
		}
		final String PhoneNumber = searchContact.get(position)
				.getSearchMatchContent().toString();
		new ActionSheetDialog(mcontext)
				.builder()
				.setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.setTitle(PhoneNumber)
				.addSheetItem("拨打电视想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								Config.intent = new Intent(mcontext,
										CallOut_Activity.class);
								Config.intent.putExtra("is_video_call", true);
								Config.intent.putExtra("PhoneNumber",
										Config.CALL_BEFORE + Config.EIGHT
												+ PhoneNumber);
								startActivity(Config.intent);
							}
						})
				.addSheetItem("拨打手机想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								Config.intent = new Intent(mcontext,
										CallOut_Activity.class);
								Config.intent.putExtra("is_video_call", true);
								Config.intent.putExtra("PhoneNumber",
										Config.CALL_BEFORE + Config.NINE
												+ PhoneNumber);
								startActivity(Config.intent);
							}
						})
				.addSheetItem("拨打手机", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								UiUtils.CallUserPhone(PhoneNumber, mcontext, 2);
							}
						}).show();

	}

	/**
	 * 提示 摄像头不可用
	 * */
	public void NoPermission() {
		Builder builder = new AlertDialog.Builder(mcontext);
		builder.setTitle("温馨提示");
		builder.setMessage("想家没有权限打开你的摄像头 \n建议设置如下:\n1、请到“设置 - 权限管理”中打开想家权限\n2、其他应用程序正在占用摄像头,请先将摄像头关闭");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});
		builder.setCancelable(false);
		alertDialog = builder.create();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	/**
	 * 初始化联系人
	 */
	private void initContact() {
		asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
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
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.phonecontacts_back:
			finish();
			break;
		case R.id.not_contacts:
			initContact();
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
			UiUtils.ClearNumber(popupwindow_search);
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
		handler.postDelayed(new Runnable() {
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

	public class ShareContactAdapter extends BaseAdapter {
		private Context context;
		private List<ContactBean> list;
		private Map<Integer, Boolean> map;
		private Thread thread;

		public ShareContactAdapter(Context context, List<ContactBean> list,
				Map<Integer, Boolean> map) {
			this.list = list;
			this.context = context;
			this.map = map;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ShareContactHolder holder;
			ContactBean data = list.get(position);
			if (view == null) {
				holder = new ShareContactHolder();
				view = View.inflate(context, R.layout.listview_contacts, null);
				holder.portrait = (ImageView) view
						.findViewById(R.id.contacts_portrait);
				holder.name = (TextView) view.findViewById(R.id.contacts_name);
				holder.mood = (TextView) view.findViewById(R.id.contacts_mood);
				holder.rcsOnline = (TextView) view
						.findViewById(R.id.rcs_online);
				holder.usertype = (TextView) view
						.findViewById(R.id.contacts_usertype);
				view.setTag(holder);
			} else {
				holder = (ShareContactHolder) view.getTag();
			}
			holder.name.setText(data.getDesplayName());
			holder.mood.setText(data.getPhoneNum());
			// SetUserType(position, holder);
			if (map.size() > 0) {
				holder.usertype.setText(map.get(position) ? "想家用户" : "");
				holder.usertype
						.setBackgroundResource(map.get(position) ? R.drawable.textview_share_user_shape
								: R.drawable.textview_share_user_shape2);
			}
			return view;

		}
	}

	public class ShareContactHolder {
		public long id;
		public ImageView portrait;
		public TextView name, mood, notReadNum, rcsOnline, usertype;

	}

	/**
	 * 异步数据库查询类
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
							// long id = cursor.getLong(0);
							String name = cursor.getString(1);
							String number = cursor.getString(2);
							// String sortKey = cursor.getString(3);
							long contactId = cursor.getLong(4);
							// Long photoId = cursor.getLong(5);
							// String lookUpKey = cursor.getString(6);

							// 创建联系人对象
							ContactBean contact = new ContactBean();
							contact.setDesplayName(name);
							contact.setPhoneNum(number);
							contact.setContactId(contactId);
							// contact.setSortKey(sortKey);
							// contact.setPhotoId(photoId);
							// contact.setLookUpKey(lookUpKey);
							clientContacts.add(contact);
						}
						if (clientContacts.size() > 0) {
							handler.sendEmptyMessage(InItContacts);
							initListView(clientContacts);
							if (!cursor.isClosed()) {
								cursor.close();
							}
						}
					} else {
						handler.sendEmptyMessage(InItContacts);
					}
				}
			}).start();
		}
	}

	private void initListView(List<ContactBean> clientContacts) {
		try {
			JSONObject json = new JSONObject();
			JSONArray array = new JSONArray();
			for (ContactBean contactBean : clientContacts) {
				String number = contactBean.getPhoneNum();
				JSONObject JsonNumber = new JSONObject();
				JsonNumber.put("phone", number);
				array.put(JsonNumber);
			}
			json.put("clientContacts", array);
			getUrlRequest(UrlClient.HttpUrl + UrlClient.GetShareContacts,
					json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取联系人中的想家用户
	 * 
	 * @param url
	 * @param json
	 */
	private void getUrlRequest(String url, String json) {
		// WorkLog.i("PhoneContact", "发送数据" + json);
		if (!NetUtils.isNetConnected(mcontext)) {
			WorkLog.i("PhoneContact", "咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.post(url).tag(this).params("json", json)
				.execute(new StringCallback() {
					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						try {
							WorkLog.i("PhoneContact", "data:" + t);
							JSONObject js = new JSONObject(t);
							int code = js.getInt("code");
							switch (code) {
							case 200:
								Gson gson = new Gson();
								bean = gson.fromJson(js.toString(),
										ShareContactsBean.class);
								if (bean != null) {
									map = new HashMap<Integer, Boolean>();
									for (int i = 0; i < clientContacts.size(); i++) {
										map.put(i, false);
										String phoneNumber = clientContacts
												.get(i).getPhoneNum();
										if (bean.shareContacts.size() > 0) {
											for (int j = 0; j < bean.shareContacts
													.size(); j++) {
												String getNum = bean.shareContacts
														.get(j).userMobile;
												if (phoneNumber.equals(getNum)) {
													map.put(i, true);
												}
											}
										}
									}
									if (map.size() > 0) {
										handler.sendEmptyMessage(InItContacts);
									}
								}
								break;
							case 400:
								WorkLog.i("PhoneContact",
										"请求失败" + js.getString("message"));
								break;
							case 500:
								WorkLog.i("PhoneContact",
										"请求错误" + js.getString("message"));
								break;

							default:
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
						WorkLog.i("PhoneContacts", "获取想家用户失败，请求发生异常");
					}
				});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterPresenceReceiver();
		OkHttpUtils.getInstance().cancelTag(this);
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}

   
	 //定义ContextMenu中每个菜单选项的Id
    final int Menu_1 = Menu.FIRST;
    final int Menu_2 = Menu.FIRST + 1;
    final int Menu_3 = Menu.FIRST + 2;
    private ClipboardManager mClipboard = null;

 //创建ContextMenu菜单的回调方法
 public void onCreateContextMenu(ContextMenu m, View v,
                                 ContextMenuInfo menuInfo) {
     super.onCreateContextMenu(m,v,menuInfo);

     //在上下文菜单选项中添加选项内容
     //add方法的参数：add(分组id,itemid, 排序, 菜单文字)
     m.add(0, Menu_1, 0, "复制文字");
     m.add(0, Menu_2, 0, "粘贴文字");
     m.add(0, Menu_3, 0, "全选文字");
 }
 @SuppressLint("NewApi")
private void copyFromEditText1() {

     // Gets a handle to the clipboard service.
     if (null == mClipboard) {
         mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

     }

     // Creates a new text clip to put on the clipboard
     ClipData clip = ClipData.newPlainText("simple text",popupwindow_search.getText());

     // Set the clipboard's primary clip.
     mClipboard.setPrimaryClip(clip);
 }
 
private void pasteToResult() {
     // Gets a handle to the clipboard service.
     if (null == mClipboard) {
         mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
     }

     String resultString = "";
     // 检查剪贴板是否有内容
     if (!mClipboard.hasPrimaryClip()) {
         Toast.makeText(PhoneContact.this,
                 "Clipboard is empty", Toast.LENGTH_SHORT).show();
     }
     else {
         ClipData clipData = mClipboard.getPrimaryClip();
         int count = clipData.getItemCount();

         for (int i = 0; i < count; ++i) {

             ClipData.Item item = clipData.getItemAt(i);
             CharSequence str = item
                     .coerceToText(PhoneContact.this);
             Log.i("mengdd", "item : " + i + ": " + str);

             resultString += str;
         }

     }
     Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
     popupwindow_search.setText(resultString);
 }


 //ContextMenu菜单选项的选项选择的回调事件
 public boolean onContextItemSelected(MenuItem item) {
     //参数为用户选择的菜单选项对象
     //根据菜单选项的id来执行相应的功能
     switch (item.getItemId()) {
         case 1:
             Toast.makeText(this, "复制文字", Toast.LENGTH_SHORT).show();
             copyFromEditText1();
             break;
         case 2:
             Toast.makeText(this, "粘贴文字", Toast.LENGTH_SHORT).show();
             pasteToResult();
             break;
         case 3:
             Toast.makeText(this, "全选文字", Toast.LENGTH_SHORT).show();
             popupwindow_search.selectAll();
             break;
     }
     return super.onOptionsItemSelected(item);
 }
	
	
	private void registerPhoneChangeReceiver() {
		mPresenceReceiver = new BroadcastReceiver() {
			private boolean isIdleRefresh = true;

			public void onReceive(Context context, Intent intent) {
				// refresh contact list after 3 second
				if (isIdleRefresh) {
					isIdleRefresh = false;
					new Timer().schedule(new TimerTask() {
						public void run() {
							handler.sendEmptyMessage(REFRESH_LIST);
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

	private void registerContactObserver() {
		mContactObserver = new ContentObserver(new Handler()) {
			public void onChange(boolean selfChange) {
				handler.sendEmptyMessage(REFRESH_LIST);
			}
		};
		getContentResolver().registerContentObserver(Contacts.CONTENT_URI,
				true, mContactObserver);
	}

	private void unregisterPresenceReceiver() {
		LocalBroadcastManager.getInstance(mcontext).unregisterReceiver(
				mPresenceReceiver);
		if (mContactObserver != null) {
			getContentResolver().unregisterContentObserver(mContactObserver);
		}
		mPresenceReceiver = null;
	}

}
