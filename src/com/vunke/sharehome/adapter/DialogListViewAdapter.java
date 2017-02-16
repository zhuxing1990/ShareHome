package com.vunke.sharehome.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vunke.sharehome.R;

public class DialogListViewAdapter extends BaseAdapter{
	private Context context;
	private List<String> list=new ArrayList<String>();
	public DialogListViewAdapter(Context context, List<String> list){
		this.context=context;
		this.list=list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder =null;
		if(convertView==null){
			convertView=LayoutInflater.from(context).inflate(R.layout.dialog_list_item, null);
			holder=new Holder();
			holder.textview=(TextView) convertView.findViewById(R.id.dialog_list_item_textview);
			
			convertView.setTag(holder);
		}else{
			holder=(Holder) convertView.getTag();
		}
		holder.textview.setText(list.get(position));
		
		
		return convertView;
	}
	class Holder{
		TextView textview;

	}

}
