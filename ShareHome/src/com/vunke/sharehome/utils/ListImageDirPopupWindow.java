package com.vunke.sharehome.utils;

import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vunke.sharehome.R;
import com.vunke.sharehome.adapter.CommonAdapter;
import com.vunke.sharehome.adapter.ContactdetailAdapter.ViewHolder;
import com.vunke.sharehome.base.BasePopupWindowForListView;
import com.vunke.sharehome.model.ImageFloder;

public class ListImageDirPopupWindow extends
		BasePopupWindowForListView<ImageFloder> {
	private ListView mListDir;

	public ListImageDirPopupWindow(int width, int height,
			List<ImageFloder> datas, View convertView) {
		super(convertView, width, height, true, datas);
	}

	@Override
	public void initViews() {
		mListDir = (ListView) findViewById(R.id.id_list_dir);
		mListDir.setAdapter(new CommonAdapter<ImageFloder>(context, mDatas,
				R.layout.listview_dir_item) {

			@Override
			public void convert(com.vunke.sharehome.adapter.ViewHolder helper,
					ImageFloder item) {
				helper.setText(R.id.id_dir_item_name, item.getName());
				helper.setImageByUrl(R.id.id_dir_item_image,
						item.getFirstImagePath());
				helper.setText(R.id.id_dir_item_count, item.getCount() + "张");

			}
		});
	}

	public interface OnImageDirSelected {
		void selected(ImageFloder floder);
	}

	private OnImageDirSelected mImageDirSelected;

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected) {
		this.mImageDirSelected = mImageDirSelected;
	}

	@Override
	public void initEvents() {
		mListDir.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (mImageDirSelected != null) {
					mImageDirSelected.selected(mDatas.get(position));
				}
			}
		});
	}

	@Override
	public void init() {

	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params) {
	}

}
