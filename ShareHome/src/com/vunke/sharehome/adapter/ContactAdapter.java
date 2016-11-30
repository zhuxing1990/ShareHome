package com.vunke.sharehome.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hp.hpl.sparta.Text;
import com.vunke.sharehome.R;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.utils.PinyinUtils;
import com.vunke.sharehome.utils.UiUtils;

public class ContactAdapter extends DefaultAdapter<Contact> {
	private Context context;
	private List<Contact> list;

	public ContactAdapter(Context context, List<Contact> list) {
		super(list);
		this.context = context;
		this.list = list;
	}

	@Override
	public BaseHolder getHolder() {
		return new ContactHolder();
	}

	public class ContactHolder extends BaseHolder<Contact> {
		private TextView contact_index;
		private TextView contact_name;
		private String currentLetter;

		@Override
		public View initView() {
			View view = View.inflate(context, R.layout.listview_letter_item,
					null);
			contact_index = (TextView) view.findViewById(R.id.contact_index);
			contact_name = (TextView) view.findViewById(R.id.contact_name);
			return view;
		}

		@Override
		public void refreshView(Contact data, final int position,
				ViewGroup parent) {
			String str = null;
			if (UiUtils.hasChinese(data.getContactName())) {
				currentLetter = PinyinUtils.getPinyin(data.getContactName())
						.charAt(0) + "";
//				WorkLog.e("ContactHolder", "中文" + currentLetter);
			} else {
				try {
					currentLetter = PinyinUtils
							.getPinyin(data.getContactName());
//					WorkLog.e("ContactHolder",  "非中文" + currentLetter);
				} catch (Exception e) {
					currentLetter = "#";
//					WorkLog.e("ContactHolder",  "非中文报错" + currentLetter);
					e.printStackTrace();
				}
			}
			// String currentLetter = data.getPinyin().charAt(0) + "";
			// String currentLetter =
			// PinyinUtils.getSortLetterBySortKey(data.getContactName());
			// String currentLetter =
			// PinyinUtils.getFirstSpell(data.getContactName());
			if (position == 0) {
				str = currentLetter;
			} else {
				// 上一个人的拼音的首字母
				try {
					String pinyin = list.get(position - 1).getPinyin();
					if (TextUtils.isEmpty(pinyin)) {
						pinyin = "#";
					}
					String preLetter = "";
					if (TextUtils.isEmpty(pinyin)) {
//						preLetter = "#";
					} else {
						preLetter = pinyin.charAt(0) + "";
					}

					if (!TextUtils.equals(preLetter, currentLetter)) {
						str = currentLetter;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			contact_index.setVisibility(str == null ? View.GONE : View.VISIBLE);
			contact_index.setText(currentLetter);
			contact_name.setText(data.getContactName());
		}
	}
}
