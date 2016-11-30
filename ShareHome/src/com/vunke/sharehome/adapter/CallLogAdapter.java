package com.vunke.sharehome.adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.R.drawable;
import com.vunke.sharehome.activity.ContactDetailActivity;
import com.vunke.sharehome.activity.SH_AttnDetailActivity;
import com.vunke.sharehome.greendao.dao.bean.CallRecorders;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 通话记录 设配器adapter
 *
 */
public class CallLogAdapter extends DefaultAdapter<CallRecorders> {
	private Context context;
	
	public CallLogAdapter(List<CallRecorders> list, Context context) {
		super(list);
		this.context = context;
	}

	@Override
	public BaseHolder getHolder() {
		return new CallLogHolder();
	}

	class CallLogHolder extends BaseHolder<CallRecorders> {
		public TextView calllog_name, calllog_phoneNumber, calllog_date;
		public ImageView calllog_type, calllog_details;
		public View conterView;
		public TextView calllog_switch_type;
		private String searchPhone;
		@Override
		protected View initView() {
			View view = View.inflate(UiUtils.getContext(),
					R.layout.listview_calllog, null);
			calllog_name = (TextView) view.findViewById(R.id.calllog_name);
			calllog_phoneNumber = (TextView) view
					.findViewById(R.id.calllog_phoneNumber);
			calllog_switch_type = (TextView) view
					.findViewById(R.id.calllog_switch_type);
			calllog_date = (TextView) view.findViewById(R.id.calllog_date);
			calllog_type = (ImageView) view.findViewById(R.id.calllog_type);
			calllog_details = (ImageView) view
					.findViewById(R.id.calllog_details);
			return view;
		}

		@Override
		protected void refreshView(final CallRecorders data,
				final int position, ViewGroup parent) {
			calllog_name.setText(data.getContactName());
			String getNumber = data.getCallRecordersPhone();
//			WorkLog.e("CallLogAdapter", "获取号码"+getNumber);
//			calllog_phoneNumber.setText(data.getCallRecordersPhone());
			calllog_phoneNumber.setText(getNumber.substring(1));
			if (getNumber.startsWith("8")) {
//				WorkLog.e("CallLogAdapter", "截取为呼叫TV");
				calllog_switch_type.setText("电视");
			}else if (getNumber.startsWith("9")){
//				WorkLog.e("CallLogAdapter", "截取为呼叫手机");
				calllog_switch_type.setText("手机");
			}else {
				WorkLog.e("CallLogAdapter", "鬼知道是呼叫什么:"+getNumber);
			}
			Date create_time = data.getCreateTime();
			if (create_time != null) {
				// String localeString = create_time.toLocaleString();
				calllog_date.setText(UiUtils.getDate(create_time.getTime()));
			}
			String type = data.getCallType();
			if (!TextUtils.isEmpty(type)) {
				if (type.equals(Config.CALLRECORDER_TYPE_VIDEO_MISSED)) {
					calllog_type.setBackgroundResource(drawable.type_missing);
				} else if (type.equals(Config.CALLRECORDER_TYPE_VIDEO_RECEIVED)) {
					calllog_type.setBackgroundResource(drawable.type_incoming);
				} else if (type.equals(Config.CALLRECORDER_TYPE_VIDEO_DIAL)) {
					calllog_type.setBackgroundResource(drawable.type_outgoing);
				} else if (type.equals(Config.CALLRECORDER_TYPE_AUDIO_MISSED)) {
					calllog_type.setBackgroundResource(drawable.type_missing2);
				} else if (type.equals(Config.CALLRECORDER_TYPE_AUDIO_RECEIVED)) {
					calllog_type.setBackgroundResource(drawable.type_incoming2);
				} else if (type.equals(Config.CALLRECORDER_TYPE_AUDIO_DIAL)) {
					calllog_type.setBackgroundResource(drawable.type_outgoing2);
				}
			} else {

			}
			calllog_details.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						String phone = data.getCallRecordersPhone();
						if (phone.length() <= 12) {
							if (phone.startsWith("8") || phone.startsWith("9")) {
//								phone = phone.substring(0);
								searchPhone = phone.substring(1);
//								WorkLog.e("CallLogAdapter", "截取后的号码" + phone);
							}
						}else {
							searchPhone = phone;
						}
//						WorkLog.e("CallLogAdapter", "当前号码"+searchPhone);
						List<ContactSummary> contacts = ContactApi
								.searchContact(searchPhone,
										ContactApi.LIST_FILTER_ALL);
						if (contacts.size() != 0 && contacts != null) {
//							WorkLog.e("CallLogAdapter", "contacts");
							for (int i = 0; i < contacts.size(); i++) {
								if (contacts.get(i).getSearchMatchContent()
										.equals(searchPhone)){
									Config.intent = new Intent(context,
											ContactDetailActivity.class);
									long ContactId = contacts.get(0)
											.getContactId();
									Config.intent.putExtra("id", ContactId);
									context.startActivity(Config.intent);
									return;
								}
							}
							GoDetailActivity(data);
							return;
						}
						List<Contact> contact = UiUtils.SearchContact(searchPhone);
						if (contact.size() != 0 && contact != null) {
//							WorkLog.e("CallLogAdapter","contact");
							Config.intent = new Intent(context,
									SH_AttnDetailActivity.class);
							Config.intent.putExtra("PhoneNumber", phone);
							Config.intent.putExtra("Name",
									data.getContactName());
							Config.intent.putExtra("Pid", contact.get(0)
									.getUserId() + "");
							context.startActivity(Config.intent);
							return;
						}
						GoDetailActivity(data);
						return;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				public void GoDetailActivity(final CallRecorders data) {
					Intent intent = new Intent(UiUtils.getContext(),
							SH_AttnDetailActivity.class);
					intent.putExtra("PhoneNumber", data.getCallRecordersPhone());
					intent.putExtra("Name", data.getContactName());
					context.startActivity(intent);
				}
			});
		}
	}
}
