package com.vunke.sharehome.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vunke.sharehome.R;

public class ContactdetailAdapter extends BaseAdapter {
	public Context context;
	public List<String> list;
	public LayoutInflater inflater;

	public ContactdetailAdapter(Context context, List<String> list) {
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);
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
		ViewHolder holder;
		if (view == null) {
			view = inflater.inflate(R.layout.listview_contactdetail, null);
			holder = new ViewHolder();
			holder.textview = (TextView) view.findViewById(R.id.detail_number);
			view.setTag(holder);
		}else{
			holder=(ViewHolder)view.getTag();
		}
		holder.textview.setText(list.get(position).toString());
		return view;
	}

	public class ViewHolder {
		TextView textview;
	}
}

