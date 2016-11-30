package com.vunke.sharehome.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.rcs.call.CallLog;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.R.drawable;
import com.vunke.sharehome.activity.ContactDetailActivity;
import com.vunke.sharehome.activity.SH_AttnDetailActivity;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.utils.UiUtils;

/**
 * 通话日志（ 已弃用）
 * @date 2016年2月
 */
public class CallLogAdapter2 extends BaseAdapter {
	private LayoutInflater inflater;
	private List<ContactSummary> contacts;
	private String phonenumber;
	private String numberNames;
	private String numberdetail;
	private Context context;
	/**
	 * 集合<通话日志> 华为SDK CallLogAPI
	 */
	private List<CallLog> callLogs;

	/**
	 * 想家联系人
	 */
	private List<Contact> home_contact;

	public CallLogAdapter2(Context context, List<CallLog> callLogs) {
		this.context = context;
		this.callLogs = callLogs;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return callLogs.size();
	}

	@Override
	public CallLog getItem(int position) {
		return callLogs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.listview_calllog, parent, false);
			holder.calllog_name = (TextView) view
					.findViewById(R.id.calllog_name);
			holder.calllog_phoneNumber = (TextView) view
					.findViewById(R.id.calllog_phoneNumber);
			holder.calllog_date = (TextView) view
					.findViewById(R.id.calllog_date);

			holder.calllog_type = (ImageView) view
					.findViewById(R.id.calllog_type);
			holder.calllog_details = (ImageView) view
					.findViewById(R.id.calllog_details);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final CallLog callLog = callLogs.get(position);
		final String number = callLog.getPeerInfo().getNumber();// 获取电话号码
		// WorkLog.e("AttnFragment", "获取的号码"+number);
		if (number.contains(Config.CALL_BEFORE)) {// 判断号码是否包含 Callnum
			if (number.length() > 8) {
				phonenumber = number.substring(8, number.length());
			}
		} else {
			phonenumber = number;
		}
		holder.calllog_phoneNumber.setText(phonenumber);
		String date = UiUtils.getDate(context,
				Long.valueOf(callLog.getBeginTime()).longValue()); // 时间
		holder.calllog_date.setText(date);// 设置时间
		switch (callLog.getType()) {// 设置通话类型
		case CallLog.TYPE_AUDIO_INCOMING:
			holder.calllog_type.setBackgroundResource(drawable.type_incoming2);
			break;
		case CallLog.TYPE_AUDIO_OUTGOING:
			holder.calllog_type.setBackgroundResource(drawable.type_outgoing2);
			break;
		case CallLog.TYPE_AUDIO_MISSING:
			holder.calllog_type.setBackgroundResource(drawable.type_missing2);
			break;
		case CallLog.TYPE_VIDEO_INCOMING:
			holder.calllog_type.setBackgroundResource(drawable.type_incoming);
			break;
		case CallLog.TYPE_VIDEO_OUTGOING:
			holder.calllog_type.setBackgroundResource(drawable.type_outgoing);
			break;
		case CallLog.TYPE_VIDEO_MISSING:
			holder.calllog_type.setBackgroundResource(drawable.type_missing);
			break;
		default:
			break;
		}
		try {
			if (number.contains(Config.CALL_BEFORE)) {// 如果电话号码包含了11831726
				if (number.contains(Config.CALL_BEFORE + Config.NINE)
						|| number.contains(Config.CALL_BEFORE + Config.EIGHT)) {// 如果电话号码包含了
																				// 11831726
																				// 加8
																				// 或者
																				// 加
																				// 9
					if (number.length() > 9) {// 如果号码长度大于9
						numberNames = number.substring(9, number.length());// 截取前面字段后
																			// 赋值号码
																			// 给
																			// numberNames
					} else {// 否则 把number赋值给 numberNames
						numberNames = number;
					}
				} else {// 如果没有包含11831726 加8 或者 加 9 截取前面字段后 赋值号码 给
						// numberNames
					if (number.length() > 8) {
						numberNames = number.substring(8, number.length());
					}
				}
			} else {// 如果电话号码不包含了11831726
				numberNames = number;
			}
			contacts = ContactApi.searchContact(numberNames,
					ContactApi.LIST_FILTER_ALL);// 通过SDK在所有系统联系人中进行搜索
			if (contacts.size() == 0 || contacts == null) {// 查询完系统联系人后，后续需要查询想家联系人，暂时没写
				List<Contact> contacts = UiUtils.SearchContact(numberNames);
				if (contacts != null && contacts.size() != 0) {
					for (int i = 0; i < contacts.size(); i++) {
						holder.calllog_name.setText(contacts.get(i)
								.getContactName());
					}
				} else {
					holder.calllog_name.setText("未知号码");
				}
			} else {
				// for (int i = 0; i < contacts.size(); i++) {
				// WorkLog.e("AttnFragment","联系人号码"+contacts.get(i).getDisplayName());
				// WorkLog.e("AttnFragment",
				// "联系人ID"+contacts.get(i).getContactId());
				// }
				String numberName = contacts.get(0).getDisplayName();
				if (contacts.size() > 1) {
					holder.calllog_name.setText("未知号码");
				} else {
					holder.calllog_name.setText(numberName);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		holder.calllog_details.setOnClickListener(new OnClickListener() {// 点击跳转到号码详情页面

					@Override
					public void onClick(View v) {
						try {
							// 如果电话号码包含了11831726
							if (number.contains(Config.CALL_BEFORE)) {
								if (number.contains(Config.CALL_BEFORE
										+ Config.NINE)
										|| number.contains(Config.CALL_BEFORE
												+ Config.EIGHT)) {// 如果电话号码包含了//
																	// 11831726加8或者加9
									if (number.length() > 9) {// 如果号码长度大于9
										numberdetail = number.substring(9,
												number.length());
									} else {// 否则 把number赋值给 numberNames
										numberdetail = number;
									}
								} else {// 如果没有包含11831726 加8 或者 加 9 截取前面字段后
										// 赋值号码 给
										// numberNames
									if (number.length() > 8) {
										numberdetail = number.substring(8,
												number.length());
									}
								}
							} else {// 如果电话号码不包含了11831726
								if (number.startsWith("+86")) {
									numberdetail = number.substring(3);
								} else {
									numberdetail = number;
								}
							}
							if (numberdetail.equals("")) {
								Toast.makeText(context, "号码错误",
										Toast.LENGTH_SHORT).show();
							} else {
								contacts = ContactApi.searchContact(
										numberdetail,
										ContactApi.LIST_FILTER_ALL);
								if (contacts.size() != 0 && contacts != null) {
									/*
									 * WorkLog.e("AttnFragment", contacts.size()
									 * + "contacts.size()");
									 */
									for (int i = 0; i < contacts.size(); i++) {
										if (contacts.get(i)
												.getSearchMatchContent()
												.equals(numberdetail)) {
											Intent intent = new Intent(context,
													ContactDetailActivity.class);
											long ContactId = contacts.get(0)
													.getContactId();
											intent.putExtra("id", ContactId);
											context.startActivity(intent);
											return;
										}
									}
									GotoContactsDetali(position);
								} else {
									/*
									 * Query<Contact> query = DbCore
									 * .getDaoSession() .getContactDao()
									 * .queryBuilder()
									 * .where(Properties.Home_phone.eq(Long
									 * .parseLong(numberdetail))) .build();
									 * home_contact = query.list();
									 */
									home_contact = UiUtils
											.SearchContact(numberdetail);
									if (home_contact != null
											&& home_contact.size() != 0) {
										for (int i = 0; i < home_contact.size(); i++) {
											Config.intent = new Intent(context,
													SH_AttnDetailActivity.class);
											Config.intent.putExtra(
													"PhoneNumber", home_contact
															.get(i)
															.getHomePhone()
															+ "");
											Config.intent.putExtra("Name", ""
													+ home_contact.get(i)
															.getContactName());
											Config.intent.putExtra("Pid", ""
													+ home_contact.get(i)
															.getUserId());
											context.startActivity(Config.intent);
										}
									} else {
										GotoContactsDetali(position);
									}

								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
		return view;
	}

	public void GotoContactsDetali(final int position) {
		Config.intent = new Intent(context, SH_AttnDetailActivity.class);
		Config.intent.putExtra("PhoneNumber", callLogs.get(position)
				.getPeerInfo().getNumber());
		Config.intent.putExtra("Name", "未知号码");
		context.startActivity(Config.intent);
	}

	public static class ViewHolder {
		public TextView calllog_name, calllog_phoneNumber, calllog_date;
		public ImageView calllog_type, calllog_details;
		public View conterView;
	}
}
