package com.vunke.sharehome.fragment;

import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.UserInfo;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.Call.CallOut_Activity;
import com.vunke.sharehome.activity.AddAttnActivity;
import com.vunke.sharehome.activity.NewFriends;
import com.vunke.sharehome.activity.PhoneContact;
import com.vunke.sharehome.activity.SH_AttnDetailActivity;
import com.vunke.sharehome.adapter.ContactAdapter;
import com.vunke.sharehome.base.BaseFragment;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.ShareHomeContact;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.service.UpdateContactService;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.QuickIndexBar;
import com.vunke.sharehome.view.QuickIndexBar.OnLetterUpdateListener;

/**
 * 联系人界面
 * 
 * @date 2016年2月
 */
public class ContactsFragment extends BaseFragment {
	private AlertDialog alertDialog;
	private PopupWindow popupWindow;

	/**
	 * 国家区号
	 */
	private String countryCode = "+86";

	/**
	 * 搜索联系人的视图
	 */
	private View popupView;

	/**
	 * 标题
	 */
	private TextView contacts_title_text;
	/**
	 * 联系人列表
	 */
	private List<Contact> contacts;

	/**
	 * 添加联系人
	 */
	private Button contacts_addattn;

	/**
	 * 联系人设配器
	 */
	private ContactAdapter adapter;

	/**
	 * 字母索引
	 */
	private QuickIndexBar contacts_bar;

	/**
	 * 搜索结果的集合
	 */
	private List<Contact> searchList;

	/**
	 * 搜索输入框
	 */
	private EditText popupwindow_search;

	/**
	 * 想家联系人列表
	 */
	private ListView contacts_listview;

	/**
	 * 新的朋友个数
	 */
	public TextView contacts_friendsSize;

	/**
	 * RXBUS 更新联系人
	 */
	private Subscription subscription;

	/**
	 * 新的朋友
	 */
	private RelativeLayout contacts_newfriends;

	/**
	 * 搜索结果 listview
	 */
	private ListView popupwindow_listview;

	/**
	 * listview的headerView 布局
	 */
	private View contacts_header;

	/**
	 * Handler 用来显示字母索引触摸后显示字体
	 */
	private Handler handler = new Handler();

	/**
	 * 搜索字体，中间字母
	 */
	private TextView contacts_centerText, contacts_searchtext;

	/**
	 * 关闭popuwindow,清楚字符
	 */
	private Button popuwindow_dismiss, popupwindow_clear;

	/**
	 * listview的headerView子布局, 新朋友，手机通讯录，看家,搜索框
	 */
	private LinearLayout contacts_phonecontacts, contacts_lookhome,
			contacts_search;

	/**
	 * 通过Hanlder发送消息将数据添加到设配器内
	 * 
	 */
	private Handler handler2 = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {

			case Config.SetContact:// 设置联系人到Listview
				if (null != contacts || contacts.size() != 0) {
					adapter = new ContactAdapter(getActivity(), contacts);
					contacts_listview.setAdapter(adapter);
					contacts_searchtext.setText("在" + adapter.getCount()
							+ "人中搜索");
					popupwindow_search.setHint("在" + adapter.getCount()
							+ "人中搜索");
				} else {
					contacts_listview.setAdapter(null);
					contacts_searchtext.setText("在0人中搜索");
					popupwindow_search.setHint("在0人中搜索");
				}
				break;
			case Config.SearchShareHomeContact:// 设置搜索结果到 listview
				if (searchList.size() != 0 || null != searchList) {
					Collections.sort(searchList);
					popupwindow_listview.setVisibility(View.VISIBLE);
					adapter = new ContactAdapter(getActivity(), searchList);
					popupwindow_listview.setAdapter(adapter);
				} else {
					popupwindow_listview.setAdapter(null);
					popupwindow_listview.setVisibility(View.GONE);
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts, null);
		init(view);
		initContact();// 初始化联系人
		getAllContact();// 获取所有想家联系人
		initListener();// 监听事件
		isPrepared = true;
		initRx();// 初始化RXjava 用来更新联系人
		return view;
	}

	// 标志位，标志已经初始化完成。
	private boolean isPrepared;

	@Override
	protected void lazyLoad() {
		if (!isPrepared || !isVisible) {
			return;
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @param view
	 */
	public void init(View view) {
		contacts_addattn = (Button) view.findViewById(R.id.contacts_addattn);
		contacts_centerText = (TextView) view
				.findViewById(R.id.contacts_centerText);
		contacts_title_text = (TextView) view
				.findViewById(R.id.contacts_title_text);

		/* listview 的header 布局 */
		contacts_header = View.inflate(getActivity(),
				R.layout.listview_item_header, null);
		contacts_search = (LinearLayout) contacts_header
				.findViewById(R.id.contacts_search);
		contacts_searchtext = (TextView) contacts_header
				.findViewById(R.id.contacts_searchtext);
		contacts_newfriends = (RelativeLayout) contacts_header
				.findViewById(R.id.contacts_newfriends);
		contacts_phonecontacts = (LinearLayout) contacts_header
				.findViewById(R.id.contacts_phonecontacts);
		contacts_lookhome = (LinearLayout) contacts_header
				.findViewById(R.id.contacts_lookhome);
		contacts_friendsSize = (TextView) contacts_header
				.findViewById(R.id.contacts_friendsSize);
		/* listview 的header 布局 */

		contacts_listview = (ListView) view
				.findViewById(R.id.contacts_listview);
		contacts_listview.addHeaderView(contacts_header);

		contacts_bar = (QuickIndexBar) view.findViewById(R.id.contacts_bar);

		/* popuwindow 的布局 */
		popupView = View.inflate(getActivity(), R.layout.popupwindow_search,
				null);
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

	/**
	 * 初始化想家联系人
	 */
	public void initContact() {
		contacts = DbCore.getDaoSession().getContactDao().loadAll();
		if (0 == contacts.size() || null == contacts) {
			handler2.sendEmptyMessage(Config.SetContact);
		} else {
			Collections.sort(contacts);
			handler2.sendEmptyMessage(Config.SetContact);
		}

	}

	/**
	 * 获取所有联系人
	 */
	private void getAllContact() {
		if (!NetUtils.isNetConnected(getActivity())) {
			UiUtils.showToast("网络异常,无法获取想家联系人");
		}
		JSONObject params = new JSONObject();
		try {
			params.put("userName", UiUtils.GetUserName(getActivity()).substring(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		 WorkLog.i("ContactsFragment", "请求链接:"+UrlClient.HttpUrl +
//		 UrlClient.queryContactAll);
		 WorkLog.i("ContactsFragment", "Request data"+params.toString());
		OkHttpUtils.post(UrlClient.HttpUrl + UrlClient.GetAllContacts)
				.tag(this).params("json", params.toString())
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						 WorkLog.i("ContactsFragment","data:"+t);
						try {
							JSONObject getResponse = new JSONObject(t);
							if (getResponse.has("code")) {
								int code = getResponse.getInt("code");
								switch (code) {
								case 200:
									Gson gson = new Gson();
									ShareHomeContact SHcontact = gson.fromJson(
											t, ShareHomeContact.class);
									// WorkLog.i("ContactsFragment",
									// SHcontact.toString());
									if (SHcontact.getContacts() != null
											&& SHcontact.getContacts().size() != 0) {
										DbCore.getDaoSession()
												.getContactDao()
												.insertOrReplaceInTx(
														SHcontact.getContacts());
										RxBus.getInstance().post(
												Config.Update_Contact);
									}
									break;
								case 400:

									break;
								case 500:

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
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						WorkLog.i("UpdateContactService", "获取失败,请求错误,网络发送异常");
					}

					@Override
					public void onAfter(boolean isFromCache,
							@Nullable String t, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onAfter(isFromCache, t, call, response, e);

					}
				});
	}

	/**
	 * 初始化监听事件
	 */
	public void initListener() {
		SetOnClickListener(contacts_addattn, contacts_newfriends,
				contacts_phonecontacts, contacts_lookhome, contacts_search,
				popupwindow_clear, popuwindow_dismiss);
		contacts_bar.setListener(new OnLetterUpdateListener() {

			@Override
			public void onLetterUpdate(String letter) {
				showLetter(letter);
				for (int i = 0; i < contacts.size(); i++) {
					Contact mcontact = contacts.get(i);
					if (!TextUtils.isEmpty(mcontact.getPinyin())) {
						String mletter = mcontact.getPinyin().charAt(0) + "";
						if (TextUtils.equals(letter, mletter)) {
							// 匹配成功
							contacts_listview.setSelection(i);
							break;
						}
					}
				}
			}

		});
		contacts_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if ((position - 1) == -1) {
					return;
				}
				Config.intent = new Intent(getActivity(),
						SH_AttnDetailActivity.class);
				Config.intent.putExtra("Name", contacts.get(position - 1)
						.getContactName());
				Config.intent.putExtra("PhoneNumber", contacts
						.get(position - 1).getHomePhone() + "");
				Config.intent.putExtra("Pid", contacts.get(position - 1)
						.getUserId() + "");
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
						: View.GONE);
				popupwindow_clear.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE
						: View.INVISIBLE);
				if (!TextUtils.isEmpty(s)) {
					searchList = UiUtils.FuzzyQuery(s.toString().trim());
					handler2.sendEmptyMessage(Config.SearchShareHomeContact);
				} else {
					handler2.sendEmptyMessage(Config.SearchShareHomeContact);
				}
			}
		});
		popupwindow_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Config.intent = new Intent(getActivity(),
						SH_AttnDetailActivity.class);
				Config.intent.putExtra("Name", searchList.get(position)
						.getContactName());
				Config.intent.putExtra("PhoneNumber", searchList.get(position)
						.getHomePhone() + "");
				Config.intent.putExtra("Pid", searchList.get(position)
						.getUserId() + "");
				startActivity(Config.intent);

			}
		});
	}

	/**
	 * 滑动字母索引后显示当前字母
	 * 
	 * @param letter
	 */
	public void showLetter(String letter) {
		contacts_centerText.setVisibility(View.VISIBLE);
		contacts_centerText.setText(letter);
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				contacts_centerText.setVisibility(View.GONE);
			}
		}, 1500);
	}

	/**
	 * 更新联系人
	 */
	private void initRx() {
		subscription = RxBus.getInstance().toObservable(Integer.class)
				.filter(new Func1<Integer, Boolean>() {
					@Override
					public Boolean call(Integer arg0) {
						return arg0 == Config.Update_Contact;
					}
				}).subscribe(new Subscriber<Integer>() {

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable arg0) {
						this.unsubscribe();
					}

					@Override
					public void onNext(Integer arg0) {
						if (arg0 == Config.Update_Contact) {
							initContact();
							// initData();
						}
					}
				});
	}

	/**
	 * 初始化新的朋友的数据
	 */
	private void initData() {
		Config.intent = new Intent(getActivity(), UpdateContactService.class);
		Config.intent.putExtra("param", Config.Upload_Contact);
		getActivity().startService(Config.intent);
	}

	/*
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.contacts_addattn:
			Config.intent = new Intent(getActivity(), AddAttnActivity.class);
			startActivity(Config.intent);
			break;
		case R.id.contacts_newfriends:
			Config.intent = new Intent(getActivity(), NewFriends.class);
			startActivity(Config.intent);
			break;
		case R.id.contacts_phonecontacts:
			Config.intent = new Intent(getActivity(), PhoneContact.class);
			startActivity(Config.intent);
			break;
		case R.id.contacts_search:
			showPopupWindow(contacts_search);
			break;
		case R.id.contacts_lookhome:
			LookHome();
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

	/**
	 * 手机看家
	 */
	public void LookHome() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				Config.UserName, getActivity().MODE_PRIVATE);
		// 通过 UserId来查询手机看家
		Long pid = sp.getLong(Config.LOOKHOME, -1);
		if (!TextUtils.isEmpty(pid + "") && pid != -1) {// 判断 UserId是否存在
			WorkLog.i("ContactsFragment", "pid:" + pid); // UserId存在
			List<Contact> getContact = UiUtils.SearchContact(pid);
			if (null != getContact && 0 != getContact.size()) {// 判断查询数据库的数据是否为空
				for (int i = 0; i < getContact.size(); i++) {
					Config.intent = new Intent(getActivity(),
							SH_AttnDetailActivity.class);
					Config.intent.putExtra("Name", getContact.get(i)
							.getContactName() + "");
					Config.intent.putExtra("PhoneNumber", getContact.get(i)
							.getHomePhone() + "");
					Config.intent.putExtra("Pid", getContact.get(i).getUserId()
							+ "");
					startActivity(Config.intent);
				}
			} else {
				WorkLog.i("ContactsFragment", "通过UserId查询数据库失败，,重新添加手机看家");
			}
		} else {// UserId不存在
			WorkLog.i("ContactsFragment", "pid不正确,通过号码查询数据库");
			List<Contact> getContact = UiUtils.SearchContact(UiUtils
					.GetUserName(getActivity()).substring(1));
			if (null != getContact && 0 != getContact.size()) {// 判断查询数据库的数据是否为空
				if (getContact.size() > 1) {// 判断数据库中是否有重复
					for (int i = 0; i < getContact.size(); i++) {
						if (i != getContact.size()) {// 删除多条重复的联系人
							DbCore.getDaoSession().getContactDao()
									.deleteByKey(getContact.get(i).getUserId());
							RxBus.getInstance().post(Config.Update_Contact);
						}
					}
				}
				for (int i = 0; i < getContact.size(); i++) {
					Config.intent = new Intent(getActivity(),
							SH_AttnDetailActivity.class);
					Config.intent.putExtra("Name", getContact.get(i)
							.getContactName() + "");
					Config.intent.putExtra("PhoneNumber", getContact.get(i)
							.getHomePhone() + "");
					Config.intent.putExtra("Pid", getContact.get(i).getUserId()
							+ "");
					startActivity(Config.intent);
				}
			} else {
				WorkLog.i("ContactsFragment", "通过查询数据库失败,重新添加手机看家");
			}
		}
	}

	/**
	 * 显示popupWindow
	 * 
	 * @param view
	 */
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
		popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED
				| PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
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
		popupInputMethodWindow();
		// popupWindow.showAsDropDown(view);
	}

	/**
	 * 当输入框获取到焦点就弹出输入法
	 */
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
	 * 开始拨号
	 */
	private void CallOut() {
		if (UiUtils.isCameraCanUse()) {// 判断摄像头能否被使用
			if (NetUtils.isNetConnected(getActivity())) {// 判断当前网络是否连接
				/*
				 * if (NetUtils.is3GConnected(getActivity())) { Builder builder
				 * = new AlertDialog.Builder(getActivity());
				 * builder.setTitle("温馨提示"); builder.setMessage(
				 * "您当前正在使用 2G/3G/4G 网络,非WiFi环境下使用会产生手机流量，建议改用WiFi接入，是否继续?");
				 * builder.setPositiveButton("继续使用", new
				 * DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface dialog, int
				 * which) { StartCall(); if (alertDialog != null) {
				 * alertDialog.dismiss(); alertDialog = null; } } });
				 * builder.setNegativeButton("取消", new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int whichButton) if
				 * (alertDialog != null) { alertDialog.dismiss(); alertDialog =
				 * null; } } }); builder.setCancelable(false); alertDialog =
				 * builder.create(); alertDialog.setCancelable(false);
				 * alertDialog.setCanceledOnTouchOutside(false);
				 * alertDialog.show(); } else {
				 */
				StartCall();
				// }
			} else {
				Toast.makeText(getActivity(), "当前网络未连接", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			NoPermission();
		}

	}

	/**
	 * 提示 摄像头不可用
	 * */
	public void NoPermission() {
		Builder builder = new AlertDialog.Builder(getActivity());
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
	 * 开始通话
	 * */
	public void StartCall() {
		UserInfo mLastUserInfo = LoginApi.getUserInfo(LoginApi
				.getLastUserName());
		String Name = null;
		if (mLastUserInfo != null) {
			if (!TextUtils.isEmpty(mLastUserInfo.countryCode)) {// 自动写入帐号
				countryCode = mLastUserInfo.countryCode;
				if ((null != mLastUserInfo)
						&& (null != mLastUserInfo.countryCode)
						&& (null != mLastUserInfo.username)) {

					if (mLastUserInfo.username.startsWith("+")) {
						int length = mLastUserInfo.countryCode.length();
						if (!mLastUserInfo.countryCode.startsWith("+")) {
							length++;
						}
						String userName = mLastUserInfo.username
								.substring(length);
						if (userName.contains(Config.CALL_BEFORE)) {
							userName = userName.substring(8, userName.length());
						}

						Name = userName;
					} else {
						if (mLastUserInfo.username.contains(Config.CALL_BEFORE)) {
							mLastUserInfo.username = mLastUserInfo.username
									.substring(8,
											mLastUserInfo.username.length());
						}
						Name = mLastUserInfo.username;

					}
				}
			}
			Config.intent = new Intent(getActivity(), CallOut_Activity.class);
			Config.intent.putExtra("is_video_call", true);
			Config.intent.putExtra("PhoneNumber", Config.CALL_BEFORE
					+ Config.EIGHT + Name.substring(1));
			startActivity(Config.intent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!subscription.isUnsubscribed()) {
			subscription.unsubscribe();
		}
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}

	/**
	 * 设置添加屏幕的背景透明度
	 * 
	 * @param bgAlpha
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getActivity().getWindow()
				.getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getActivity().getWindow().setAttributes(lp);
	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			// listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			// 计算子项View 的宽高
			listItem.measure(0, 0);
			// 统计所有子项的总高度
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
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

}