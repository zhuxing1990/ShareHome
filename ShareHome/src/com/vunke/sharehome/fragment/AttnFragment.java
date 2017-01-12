package com.vunke.sharehome.fragment;

import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huawei.rcs.call.CallLog;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.Call.CallOut_Activity;
import com.vunke.sharehome.adapter.CallLogAdapter;
import com.vunke.sharehome.adapter.SearchContactAdapter;
import com.vunke.sharehome.base.BaseFragment;
import com.vunke.sharehome.greendao.dao.CallRecordersDao.Properties;
import com.vunke.sharehome.greendao.dao.bean.CallRecorders;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.Rx_ReLogin;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.ActionSheetDialog;
import com.vunke.sharehome.view.ActionSheetDialog.OnSheetItemClickListener;

/**
 * 拨号界面
 * 
 * @date 2016年2月
 */
public class AttnFragment extends BaseFragment {

	private AlertDialog alertDialog;

	/**
	 * 电话号码 输入框
	 */
	private EditText Number;

	/**
	 * 拨号盘显示的号码
	 */
	private String callNumber;

	/**
	 * 手机号码
	 */
	private String PhoneNumber = "";

	/**
	 * 键盘布局
	 */
	private LinearLayout tableLayout1;

	/**
	 * 想家联系人
	 */
	private List<Contact> home_contact;

	/**
	 * 联系人(已弃用)
	 */
	private List<Contact> contact2;

	/**
	 * RXBus 观察者
	 */
	private Subscription subscribe, subscribe2, subscribe3;

	/**
	 * 布局 标题布局
	 */
	private RelativeLayout attn_title;

	/**
	 * 号码显示布局
	 */
	private LinearLayout linearLayout1;
	/**
	 * 没有网络
	 */
	private RelativeLayout attn_setInternet;
	/**
	 * 时间 用于判断用户 快速点击后的时长
	 */
	private long Time = 0;

	/**
	 * 拨号键布局
	 */
	private LinearLayout home_callLayout;

	/**
	 * 适配器 通话记录设配器
	 **/
	private CallLogAdapter adapter;

	/**
	 * 通话记录
	 */
	private List<CallRecorders> callRecorders;

	/**
	 * 没有通话日志
	 */
	private LinearLayout layout_nothing_log;

	/**
	 * 集合 联系人总结 摘要
	 */
	private List<ContactSummary> contact;

	/**
	 * 拨号 . 删除通话记录
	 */
	private Button home_call, delete_calllog;

	/**
	 * 输入号码索引设配器
	 */
	private SearchContactAdapter searchadapter;

	/**
	 * 呼叫回话 华为SDK CallAPI
	 */
	private CallSession callSession;

	/**
	 * 集合<通话日志> 华为SDK CallLogAPI
	 */
	private List<CallLog> callLogs;

	/**
	 * 通话记录 ， 输入号码索引
	 */
	private ListView listview, attn_listview2;

	/**
	 * 拨号键盘上 0 到9的数字 和 *号 #号键
	 */
	private Button Clear, one, two, three, four, five, six, seven, eight, nine,
			zero, xin, jin;

	// private List<ContactCallLog> contactCallLogs;//集合 华为SDK 联系人通话记录 查询群会议记录

	/**
	 * 通过handler 接收到消息并将数据设置在ListView 的Adapter中，将数据展示在UI
	 * 
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {

			case Config.SetCallLog:

				if (callRecorders.size() != 0 && callRecorders != null) {
					adapter = new CallLogAdapter(callRecorders, getActivity());
					listview.setAdapter(adapter);
					attn_title.setVisibility(View.VISIBLE);
					listview.setVisibility(View.VISIBLE);
					layout_nothing_log.setVisibility(View.GONE);
				} else {
					listview.setAdapter(null);
					listview.setVisibility(View.GONE);
					layout_nothing_log.setVisibility(View.VISIBLE);
				}

				break;

			case Config.SearchContact:

				if (contact.size() != 0 && null != contact) {
					searchadapter = new SearchContactAdapter(getActivity(),
							contact);// 设置设配器
					attn_listview2.setAdapter(searchadapter);
				} else {
					attn_listview2.setAdapter(null);// 清空设配器
				}
				if (attn_listview2.getAdapter() != null) {
					layout_nothing_log.setVisibility(View.GONE);
				}

				break;
			default:
				break;
			}
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_attn, null);
		init(view);// 初始化控件
		SetCallLog();// 设置通话记录
		isPrepared = true;
		Updata_CallLog();
		RestartRelogin();
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
	 * */
	private void init(View view) {
		Clear = (Button) view.findViewById(R.id.attn_clear);// 清除号码
		one = (Button) view.findViewById(R.id.attn_1);// 拨号键盘 1
		two = (Button) view.findViewById(R.id.attn_2);// 拨号键盘 2
		three = (Button) view.findViewById(R.id.attn_3);// 拨号键盘 3
		four = (Button) view.findViewById(R.id.attn_4);// 拨号键盘 4
		five = (Button) view.findViewById(R.id.attn_5);// 拨号键盘 5
		six = (Button) view.findViewById(R.id.attn_6);// 拨号键盘 6
		seven = (Button) view.findViewById(R.id.attn_7);// 拨号键盘 7
		eight = (Button) view.findViewById(R.id.attn_8);// 拨号键盘 8
		nine = (Button) view.findViewById(R.id.attn_9);// 拨号键盘 9
		zero = (Button) view.findViewById(R.id.attn_0);// 拨号键盘 0
		xin = (Button) view.findViewById(R.id.attn_xin);// 拨号键盘 *
		jin = (Button) view.findViewById(R.id.attn_jin);// 拨号键盘 #
		delete_calllog = (Button) view.findViewById(R.id.delete_calllog);// 清除通话记录
		layout_nothing_log = (LinearLayout) view
				.findViewById(R.id.layout_nothing_log);// 没有通话记录
		attn_title = (RelativeLayout) view.findViewById(R.id.attn_title);// 标题栏
		tableLayout1 = (LinearLayout) view.findViewById(R.id.tableLayout1);// 键盘布局
		linearLayout1 = (LinearLayout) view.findViewById(R.id.linearLayout1);// 号码显示布局
		attn_setInternet = (RelativeLayout) view
				.findViewById(R.id.attn_setInternet);// 没有网络布局
		home_callLayout = (LinearLayout) view
				.findViewById(R.id.home_callLayout);// 拨号键布局
		listview = (ListView) view.findViewById(R.id.attn_listview);// 通话记录
		listview.setOnItemClickListener(new OnItemClickListener() {// 通话记录列表点击事件

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if (UiUtils.isCameraCanUse()) {// 判断摄像头能否被使用
					if (NetUtils.isNetConnected(getActivity())) {// 判断当前网络是否连接
						if (attn_setInternet.getVisibility() == View.VISIBLE) {
							UiUtils.showToast("网络未连接，请稍后再试");
							return;
						}
						StartCall2(position);// 开始通话
						// }
					} else {
						Toast.makeText(getActivity(), "当前网络未连接",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					NoPermission();
				}
			}
		});
		listview.setOnTouchListener(new OnTouchListener() {// 通话记录列表滑动事件

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (tableLayout1.getVisibility() == View.VISIBLE) {// 滑动后隐藏拨号键盘布局
					setLayout(true);
				}
				if (home_call.getVisibility() == View.VISIBLE
						&& tableLayout1.getVisibility() == View.GONE) {// 滑动后隐藏拨号按钮
					// home_call.setVisibility(View.GONE);
				}
				Config.intent = new Intent(Config.CALL_DAIL);
				Config.intent.putExtra("onTouch", true);
				getActivity().sendBroadcast(Config.intent);
				return false;
			}
		});
		attn_listview2 = (ListView) view.findViewById(R.id.attn_listview2);// 号码输入引索
		attn_listview2.setOnItemClickListener(new OnItemClickListener() {// 判断号码输入引索列表的点击事件
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							final int position, long id) {
						if (UiUtils.isCameraCanUse()) {// 判断摄像头能否被使用
							if (NetUtils.isNetConnected(getActivity())) {// 判断当前网络是否连接
								if (attn_setInternet.getVisibility() == View.VISIBLE) {
									UiUtils.showToast("网络未连接，请稍后再试");
									return;
								}
								SearchCall(position);
								/*
								 * intent = new Intent(getActivity(),
								 * CallOut_Activity.class);
								 * intent.putExtra("is_video_call", true);
								 * intent.putExtra( "PhoneNumber",
								 * Config.CALL_BEFORE + Config.EIGHT + contact
								 * .get(position) .getSearchMatchContent()
								 * .toString()); startActivity(intent);
								 */
								// }
							} else {
								Toast.makeText(getActivity(), "当前网络未连接",
										Toast.LENGTH_SHORT).show();
							}
						} else {
							NoPermission();
						}
					}
				});
		attn_listview2.setOnTouchListener(new OnTouchListener() {// 通话记录列表滑动事件

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (tableLayout1.getVisibility() == View.VISIBLE) {// 滑动后隐藏拨号键盘布局
							setLayout(true);
						}
						if (home_call.getVisibility() == View.VISIBLE
								&& tableLayout1.getVisibility() == View.GONE) {// 滑动后隐藏拨号按钮
							// home_call.setVisibility(View.GONE);
						}
						Config.intent = new Intent(Config.CALL_DAIL);
						Config.intent.putExtra("onTouch", true);
						getActivity().sendBroadcast(Config.intent);
						return false;
					}
				});

		home_call = (Button) view.findViewById(R.id.home_call);// 拨号
		Number = (EditText) view.findViewById(R.id.attn_phone);// 电话号码
		Number.setInputType(InputType.TYPE_NULL);// 设置输入框无法输入
		getNumber();
		Clear.setOnLongClickListener(new OnLongClickListener() {// 清除长按事件

			@Override
			public boolean onLongClick(View v) {// 清除号码输入框
				Number.setText("");
				return false;
			}
		});
		/**
		 * 设置点击监听事件
		 * */
		SetOnClickListener(Clear, one, two, three, four, five, six, seven,
				eight, nine, zero, xin, jin, home_call, delete_calllog,
				attn_setInternet);
	}

	/**
	 * 设置通话记录
	 * */
	public void SetCallLog() {
		/*
		 * if (callLogs.size() == 0) { listview.setVisibility(View.GONE);
		 * layout_nothing_log.setVisibility(View.VISIBLE); } else { adapter =
		 * new CallLogAdapter(); listview.setAdapter(adapter);
		 * attn_title.setVisibility(View.VISIBLE);
		 * listview.setVisibility(View.VISIBLE);
		 * layout_nothing_log.setVisibility(View.GONE); }
		 */
		/**
		 * 查询所有通话记录
		 */
		// callRecorders = DbCore.getDaoSession().getCallRecordersDao()
		// .queryBuilder().orderDesc(Properties.CallId).build().list();
		// handler.sendEmptyMessage(Config.SetCallLog);

		subscribe2 = Observable
				.create(new OnSubscribe<List<CallRecorders>>() {

					@Override
					public void call(
							Subscriber<? super List<CallRecorders>> subscriber) {
						callRecorders = DbCore.getDaoSession()
								.getCallRecordersDao().queryBuilder()
								.orderDesc(Properties.CallId).build().list();
						subscriber.onNext(callRecorders);
						subscriber.onCompleted();

					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<List<CallRecorders>>() {

					@Override
					public void onNext(List<CallRecorders> callRecorders) {
						if (callRecorders.size() != 0 && callRecorders != null) {
							adapter = new CallLogAdapter(callRecorders,
									getActivity());
							listview.setAdapter(adapter);
							attn_title.setVisibility(View.VISIBLE);
							listview.setVisibility(View.VISIBLE);
							layout_nothing_log.setVisibility(View.GONE);
						} else {
							listview.setAdapter(null);
							listview.setVisibility(View.GONE);
							layout_nothing_log.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable throwable) {
						Toast.makeText(getActivity(), "Error!",
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	/**
	 * 更新通话日志
	 */
	private void Updata_CallLog() {
		subscribe = RxBus.getInstance().toObservable(Integer.class)
				.filter(new Func1<Integer, Boolean>() {

					@Override
					public Boolean call(Integer arg0) {
						return arg0 == Config.Update_CallLog;
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
						if (arg0 == Config.Update_CallLog) {
							// 更新数据
							SetCallLog();

						}
					}
				});
	}

	/**
	 * 重新登录
	 */
	private void RestartRelogin() {
		subscribe3 = RxBus.getInstance().toObservable(Rx_ReLogin.class)
				.filter(new Func1<Rx_ReLogin, Boolean>() {

					@Override
					public Boolean call(Rx_ReLogin arg0) {

						return arg0.code == Config.RESTART_RELOGIN;
					}
				}).subscribe(new Subscriber<Rx_ReLogin>() {

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable arg0) {
						this.unsubscribe();
					}

					@Override
					public void onNext(Rx_ReLogin arg0) {
						if (arg0.code == Config.RESTART_RELOGIN) {
							attn_setInternet
									.setVisibility(arg0.isConnect ? View.GONE
											: View.VISIBLE);

						}
					}
				});
	}

	/**
	 * 获取ListView下标的号码信息 开始通话
	 * */
	public void StartCall2(final int position) {
		callNumber = callRecorders.get(position).getCallRecordersPhone();
		WorkLog.i("AttnFragment", "列表中的号码:" + callNumber);
		if (callNumber.length() >= 11) {
			callNumber = UiUtils.isMobileNO(callNumber.substring(1)) ? callNumber
					.substring(1) : callNumber;
		} else {
			callNumber = callNumber.substring(1);
		}
		if (TextUtils.isEmpty(callNumber)) {
			return;
		}
		WorkLog.i("AttnFragment", "过滤是不是手机号码:" + callNumber);
		new ActionSheetDialog(getActivity())
				.builder()
				.setTitle(callNumber)
				.setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("拨打电视想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								Config.intent = new Intent(getActivity(),
										CallOut_Activity.class);
								Config.intent.putExtra("is_video_call", true);
								if (callNumber.startsWith(Config.CALL_BEFORE)) {
									Config.intent.putExtra("PhoneNumber",
											callNumber);
								} else {
									if (callNumber.startsWith("8")
											|| callNumber.startsWith("9")) {
										Config.intent
												.putExtra("PhoneNumber",
														Config.CALL_BEFORE
																+ callNumber);
									} else {
										Config.intent.putExtra("PhoneNumber",
												Config.CALL_BEFORE
														+ Config.EIGHT
														+ callNumber);
									}
								}
								startActivity(Config.intent);
							}
						})
				.addSheetItem("拨打手机想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								Config.intent = new Intent(getActivity(),
										CallOut_Activity.class);
								Config.intent.putExtra("is_video_call", true);
								if (callNumber.startsWith(Config.CALL_BEFORE)) {
									Config.intent.putExtra("PhoneNumber",
											callNumber);
								} else {
									if (callNumber.startsWith("8")
											|| callNumber.startsWith("9")) {
										Config.intent
												.putExtra("PhoneNumber",
														Config.CALL_BEFORE
																+ callNumber);
									} else {
										Config.intent.putExtra("PhoneNumber",
												Config.CALL_BEFORE
														+ Config.NINE
														+ callNumber);
									}
								}
								startActivity(Config.intent);
							}
						})
				.addSheetItem("拨打手机", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								UiUtils.CallUserPhone(callNumber,
										getActivity(), 2);
							}
						}).show();
	}

	/**
	 * 搜索结果上面点击拨号
	 * 
	 * @param position
	 */
	private void SearchCall(final int position) {
		new ActionSheetDialog(getActivity())
				.builder()
				.setTitle(
						contact.get(position).getSearchMatchContent()
								.toString())
				.setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("拨打电视想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								Config.intent = new Intent(getActivity(),
										CallOut_Activity.class);
								Config.intent.putExtra("is_video_call", true);
								Config.intent
										.putExtra(
												"PhoneNumber",
												Config.CALL_BEFORE
														+ Config.EIGHT
														+ contact
																.get(position)
																.getSearchMatchContent()
																.toString());
								startActivity(Config.intent);
								removeNumberText();
							}
						})
				.addSheetItem("拨打手机想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								Config.intent = new Intent(getActivity(),
										CallOut_Activity.class);
								Config.intent.putExtra("is_video_call", true);
								Config.intent
										.putExtra(
												"PhoneNumber",
												Config.CALL_BEFORE
														+ Config.NINE
														+ contact
																.get(position)
																.getSearchMatchContent()
																.toString());
								startActivity(Config.intent);
								removeNumberText();
							}
						})
				.addSheetItem("拨打手机", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								UiUtils.CallUserPhone(contact.get(position)
										.getSearchMatchContent().toString(),
										getActivity(), 2);
								removeNumberText();
							}
						}).show();
	}

	public void removeNumberText() {
		if (!TextUtils.isEmpty(Number.getText())) {
			Number.setText("");
		}
	}

	/**
	 * 获取号码输入框的输入改变事件
	 * */
	public void getNumber() {
		if (Number != null) {
			Number.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {// 判断输入框发生改变
					if (!TextUtils.isEmpty(s)) {
						// home_call.setVisibility(View.VISIBLE);// 显示拨号按钮
						// 初始化输入号码索引
						GetContacts();
						listview.setVisibility(View.GONE);
						attn_listview2.setVisibility(View.VISIBLE);

					} else {
						// home_call.setVisibility(View.GONE);// 隐藏拨号按钮
						listview.setVisibility(View.VISIBLE);
						attn_listview2.setVisibility(View.GONE);
						if (adapter == null) {
							layout_nothing_log.setVisibility(View.VISIBLE);
						}
						Config.CallSize = 0;
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		}

	}

	/**
	 * 初始化输入号码索引
	 * */
	public void GetContacts() {
		try {
			String number = Number.getText().toString().trim();// 获取用户输入的号码
			if (!TextUtils.isEmpty(number)) {
				if (number.startsWith("9") || number.startsWith("8")) {
					number = number.substring(1);
				}
				contact = ContactApi.searchContact(number,
						ContactApi.LIST_FILTER_ALL);// 通过SDK在所有系统联系人中进行搜索
				// contact2 = UiUtils.FuzzyQuery(number);//通过greenDao 进行搜索
				handler.sendEmptyMessage(Config.SearchContact);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	/**
	 * 点击事件
	 * */
	@Override
	public void onClick(View v) {
		PhoneNumber = Number.getText().toString().trim();
		super.onClick(v);
		switch (v.getId()) {
		case R.id.attn_clear:// 清除输入框1个号码
			UiUtils.ClearNumber2(Number);
			break;
		case R.id.attn_1:// 拨号键盘 1
			Number.setText(PhoneNumber + "1");
			break;
		case R.id.attn_2:// 拨号键盘 2
			Number.setText(PhoneNumber + "2");
			break;
		case R.id.attn_3:// 拨号键盘 3
			Number.setText(PhoneNumber + "3");
			break;
		case R.id.attn_4:// 拨号键盘 4
			Number.setText(PhoneNumber + "4");
			break;
		case R.id.attn_5:// 拨号键盘 5
			Number.setText(PhoneNumber + "5");
			break;
		case R.id.attn_6:// 拨号键盘 6
			Number.setText(PhoneNumber + "6");
			break;
		case R.id.attn_7:// 拨号键盘7
			Number.setText(PhoneNumber + "7");
			break;
		case R.id.attn_8:// 拨号键盘 8
			Number.setText(PhoneNumber + "8");
			break;
		case R.id.attn_9:// 拨号键盘 9
			Number.setText(PhoneNumber + "9");
			break;
		case R.id.attn_0:// 拨号键盘 0
			Number.setText(PhoneNumber + "0");
			break;
		case R.id.attn_xin:// 拨号键盘 *
			Number.setText(PhoneNumber + "*");
			break;
		case R.id.attn_jin:// 拨号键盘#
			Number.setText(PhoneNumber + "#");
			break;
		case R.id.home_call:// 拨号键
			if (attn_setInternet.getVisibility() == View.VISIBLE) {
				UiUtils.showToast("网络未连接，请稍后再试");
				return;
			}
			if (TextUtils.isEmpty(PhoneNumber)) {
				CallOut2();
				return;
			}
			CallOut();
			break;
		case R.id.delete_calllog:// 删除通话记录
			DeleteCallLog();
			break;
		case R.id.attn_setInternet:
			UiUtils.startToSettings(getActivity());
			break;
		default:
			break;
		}
	}

	/**
	 * 删除通话记录
	 * */
	public void DeleteCallLog() {
		Builder dl = new AlertDialog.Builder(getActivity());
		dl.setTitle("删除所有通话记录");
		dl.setMessage("你确定要删除所有通话记录吗?");
		dl.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DbCore.getDaoSession().getCallRecordersDao().deleteAll();
				RxBus.getInstance().post(Config.Update_CallLog);
				layout_nothing_log.setVisibility(View.VISIBLE);
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});
		dl.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});

		alertDialog = dl.create();
		alertDialog.show();
	}

	/**
	 * 拨号 开始呼叫
	 * */
	public void CallOut() {
		WorkLog.i("AttnFragment", "开始通话");
		if (UiUtils.isCameraCanUse()) {// 判断摄像头能否被使用
			if (NetUtils.isNetConnected(getActivity())) {// 判断当前网络是否连接

				callNumber = Number.getText().toString().trim();
				if (TextUtils.isEmpty(callNumber)) {
					return;
				}
				callNumber = UiUtils.isMobileNO(callNumber.substring(1)) ? callNumber
						.substring(1) : callNumber;
				new ActionSheetDialog(getActivity())
						.builder()
						.setTitle(Number.getText().toString().trim())
						.setCancelable(true)
						.setCanceledOnTouchOutside(true)
						.addSheetItem("拨打电视想家",
								ActionSheetDialog.SheetItemColor.Blue,
								new OnSheetItemClickListener() {

									@Override
									public void onClick(int which) {
										Config.intent = new Intent(
												getActivity(),
												CallOut_Activity.class);
										Config.intent.putExtra("is_video_call",
												true);
										UiUtils.isCallCode(callNumber);
										UiUtils.isCallNumber(Config.CALL_BEFORE
												+ callNumber);
										Config.intent.putExtra("PhoneNumber",
												Config.CALL_BEFORE
														+ Config.EIGHT
														+ callNumber);
										startActivity(Config.intent);
										Number.setText("");
									}
								})
						.addSheetItem("拨打手机想家",
								ActionSheetDialog.SheetItemColor.Blue,
								new OnSheetItemClickListener() {

									@Override
									public void onClick(int which) {
										Config.intent = new Intent(
												getActivity(),
												CallOut_Activity.class);
										Config.intent.putExtra("is_video_call",
												true);
										UiUtils.isCallCode(callNumber);
										UiUtils.isCallNumber(Config.CALL_BEFORE
												+ callNumber);
										Config.intent.putExtra("PhoneNumber",
												Config.CALL_BEFORE
														+ Config.NINE
														+ callNumber);
										startActivity(Config.intent);
										Number.setText("");
									}
								})
						.addSheetItem("拨打手机",
								ActionSheetDialog.SheetItemColor.Blue,
								new OnSheetItemClickListener() {

									@Override
									public void onClick(int which) {
										UiUtils.CallUserPhone(callNumber,
												getActivity(), 2);
										Number.setText("");
									}
								}).show();
			} else {
				Toast.makeText(getActivity(), "当前网络未连接", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			NoPermission();
		}

	}

	/**
	 * 拨号 开始呼叫
	 * */
	public void CallOut2() {
		WorkLog.i("AttnFragment", "开始通话");
		if (callRecorders != null && callRecorders.size() != 0) {

			callNumber = callRecorders.get(0).getCallRecordersPhone();
			if (TextUtils.isEmpty(callNumber)) {
				return;
			}
			WorkLog.i("AttnFragment", "列表中的号码:" + callNumber);
			if (callNumber.length() >= 11) {
				callNumber = UiUtils.isMobileNO(callNumber.substring(1)) ? callNumber
						.substring(1) : callNumber;
			} else {
				callNumber = callNumber.substring(1);
			}
			if (TextUtils.isEmpty(callNumber)) {
				return;
			}
			WorkLog.i("AttnFragment", "过滤是不是手机号码:" + callNumber);
			Config.CallSize++;
			if (Config.CallSize == 1) {
				Number.setText(callNumber);
				return;
			} else if (Config.CallSize == 2) {
				Config.CallSize = 0;
			}
			if (UiUtils.isCameraCanUse()) {// 判断摄像头能否被使用
				if (NetUtils.isNetConnected(getActivity())) {// 判断当前网络是否连接
					new ActionSheetDialog(getActivity())
							.builder()
							.setTitle(callNumber)
							.setCancelable(true)
							.setCanceledOnTouchOutside(true)
							.addSheetItem("拨打电视想家",
									ActionSheetDialog.SheetItemColor.Blue,
									new OnSheetItemClickListener() {

										@Override
										public void onClick(int which) {
											Config.intent = new Intent(
													getActivity(),
													CallOut_Activity.class);
											Config.intent.putExtra(
													"is_video_call", true);
											UiUtils.isCallCode(callNumber);
											UiUtils.isCallNumber(callNumber);
											Config.intent.putExtra(
													"PhoneNumber",
													Config.CALL_BEFORE
															+ Config.EIGHT
															+ callNumber);
											startActivity(Config.intent);
											Number.setText("");
										}
									})
							.addSheetItem("拨打手机想家",
									ActionSheetDialog.SheetItemColor.Blue,
									new OnSheetItemClickListener() {

										@Override
										public void onClick(int which) {
											Config.intent = new Intent(
													getActivity(),
													CallOut_Activity.class);
											Config.intent.putExtra(
													"is_video_call", true);
											UiUtils.isCallCode(callNumber);
											UiUtils.isCallNumber(callNumber);
											Config.intent.putExtra(
													"PhoneNumber",
													Config.CALL_BEFORE
															+ Config.NINE
															+ callNumber);
											startActivity(Config.intent);
											Number.setText("");
										}
									})
							.addSheetItem("拨打手机",
									ActionSheetDialog.SheetItemColor.Blue,
									new OnSheetItemClickListener() {

										@Override
										public void onClick(int which) {
											UiUtils.CallUserPhone(callNumber,
													getActivity(), 2);
											Number.setText("");
										}
									}).show();

				} else {
					Toast.makeText(getActivity(), "当前网络未连接", Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				NoPermission();
			}
		}
	}

	/**
	 * 开始通话
	 * */
	public void StartCall() {
		if ((System.currentTimeMillis() - Time) > 3000) {
			Config.intent = new Intent(getActivity(), CallOut_Activity.class);
			Config.intent.putExtra("is_video_call", true);
			UiUtils.isCallCode(Number.getText().toString().trim());
			UiUtils.isCallNumber(Config.CALL_BEFORE
					+ Number.getText().toString().trim());
			Config.intent.putExtra("PhoneNumber", Config.CALL_BEFORE
					+ Number.getText().toString().trim());
			startActivity(Config.intent);
			Time = System.currentTimeMillis();
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
	 * 根据用户滑动listview 如果是false 隐藏布局 如果是true 显示布局
	 * */
	public void setLayout(boolean visibility) {
		try {
			if (visibility == true) {
				tableLayout1.setVisibility(View.GONE);
				linearLayout1.setVisibility(View.GONE);
				home_callLayout.setVisibility(View.GONE);
				// home_call.setVisibility(View.GONE);
			} else {// 如果是true 显示布局
				tableLayout1.setVisibility(View.VISIBLE);
				linearLayout1.setVisibility(View.VISIBLE);
				home_callLayout.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取拨号键盘的状态 是否隐藏 如果显示 则返回true否则返回false
	 * */
	public boolean getLayout() {
		boolean layout_isVISIBLE = false;
		if (tableLayout1 != null) {
			if (tableLayout1.getVisibility() == View.VISIBLE) {// 如果显示 则返回true
				layout_isVISIBLE = true;
			} else {// 否则返回false
				layout_isVISIBLE = false;
			}
		}
		return layout_isVISIBLE;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (attn_listview2.getVisibility() != View.GONE
				&& attn_listview2.getAdapter() != null) {
			listview.setVisibility(View.GONE);
		} else {
			if (!TextUtils.isEmpty(Number.getText())) {
				listview.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!subscribe.isUnsubscribed()) {
			subscribe.unsubscribe();
		}
		if (!subscribe2.isUnsubscribed()) {
			subscribe2.unsubscribe();
		}
		if (!subscribe3.isUnsubscribed() || subscribe3 != null) {
			subscribe3.unsubscribe();
		}
	}

}
