package com.vunke.sharehome.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.model.Newfriendsbean;
import com.vunke.sharehome.model.Newfriendsbean.Data;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.utils.UiUtils;

public class NewFriendsAdapter extends DefaultAdapter<Data> {
	private Context context;

	public NewFriendsAdapter(List<Data> list, Context context) {
		super(list);
		this.context = context;
	}

	@Override
	protected BaseHolder getHolder() {
		return new NewFriendsHolder();
	}

	public class NewFriendsHolder extends BaseHolder<Data> {
		private TextView newfriends_name, newfriends_phone;
		private ImageView newfriends_icon;
		private Button newfriends_addfriends;

		@Override
		protected View initView() {
			View view = View.inflate(context,
					R.layout.listview_newfriends_item, null);
			newfriends_name = (TextView) view
					.findViewById(R.id.newfriends_name);
			newfriends_phone = (TextView) view
					.findViewById(R.id.newfriends_phone);
			newfriends_icon = (ImageView) view
					.findViewById(R.id.newfriends_icon);
			newfriends_addfriends = (Button) view
					.findViewById(R.id.newfriends_addfriends);
			return view;
		}

		@Override
		protected void refreshView(final Data data, final int position,
				ViewGroup parent) {
			newfriends_name.setText(data.friendName);
			newfriends_phone.setText(data.friendAccount);
			newfriends_addfriends.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Contact contact = new Contact();
					contact.setContactName(data.friendName);
					contact.setHomePhone(data.friendAccount);
					contact.setGroupId("1");
					long insert = DbCore.getDaoSession().getContactDao()
							.insert(contact);
					if (insert > 0) {
						UiUtils.showToast("添加成功");
						if (list.size() != 0) {
							list.remove(position);
							NewFriendsAdapter.this.notifyDataSetChanged();
						}
					} else {
						UiUtils.showToast("添加失败");
					}
				}
			});
		}

	}
}
