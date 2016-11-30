package com.vunke.sharehome.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DefaultAdapter<T> extends BaseAdapter {
	protected List<T> list;

	protected DefaultAdapter(List<T> list) {
		this.list = list;
	}

	public void setList(List<T> list) {
		this.list = list;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (list == null || list.size() == 0) {
			return 0;
		}
		return list.size();
	}

	@Override
	public T getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		BaseHolder holder;
		if (convertView == null) {
			holder = getHolder();

		} else {
			holder = (BaseHolder) convertView.getTag();
		}
		T data = (T) getItem(position);
		holder.setData(data, position, parent);
		return holder.getContentView();
	}

	protected abstract BaseHolder getHolder();

}
