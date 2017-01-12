package com.vunke.sharehome.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.rcs.contact.ContactSummary;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.ContactDetailActivity;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
/**
 * 拨号键盘搜索设配器
 * @author Administrator
 */
public class SearchContactAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<ContactSummary> contact;
	private Context context;

	public SearchContactAdapter(Context context, List<ContactSummary> contact) {
		this.context = context;
		this.contact = contact;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return contact.size();
	}

	@Override
	public Object getItem(int position) {
		return contact.get(position);
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
			view = inflater.inflate(R.layout.listview_calllog2, null);
			holder.calllog_name = (TextView) view
					.findViewById(R.id.calllog_name);
			holder.calllog_phoneNumber = (TextView) view
					.findViewById(R.id.calllog_phoneNumber);
			holder.calllog_date = (TextView) view
					.findViewById(R.id.calllog_date);

			holder.calllog_details = (ImageView) view
					.findViewById(R.id.calllog_details);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
//		WorkLog.a("搜索匹配号码"+contact.get(position).getSearchMatchContent().toString());
//		WorkLog.a("匹配关键字"+contact.get(position).getSortkey().toString());
//		WorkLog.a("显示姓名"+contact.get(position).getDisplayName().toString());
//		WorkLog.a("状态"+contact.get(position).getStatus());
//		WorkLog.a("联系人Id"+contact.get(position).getContactId());
//		WorkLog.a("头像"+contact.get(position).getPhotoId());
		try {  //这地方都报错，我就百思不得其解了      数组越界
			if (contact.size() == 0 ) {
				return view;
			}
			if (UiUtils.hasChinese(contact.get(position).getSearchMatchContent().toString())) {
				contact.remove(position);
				SearchContactAdapter.this.notifyDataSetChanged();
			}
			holder.calllog_name.setText(contact.get(position).getDisplayName().toString());
			holder.calllog_phoneNumber.setText(contact.get(position).getSearchMatchContent().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		holder.calllog_details.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Config.intent = new Intent(context,ContactDetailActivity.class);
				long ContactId = contact.get(position).getContactId();
				Config.intent.putExtra("id", ContactId);
				context.startActivity(Config.intent);
			}
		});
		return view;
	}
	static class ViewHolder {
		public TextView calllog_name, calllog_phoneNumber, calllog_date;
		public ImageView  calllog_details;
		public View conterView;
	}

}
