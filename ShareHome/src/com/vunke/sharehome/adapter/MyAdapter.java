package com.vunke.sharehome.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.vunke.sharehome.R;
import com.vunke.sharehome.model.RX_Photo;
import com.vunke.sharehome.rx.RxBus;

public class MyAdapter extends CommonAdapter<String> {

	/**
	 * 文件夹路径
	 */
	private String mDirPath;
	private Context context;

	public MyAdapter(Context context, List<String> mDatas, int itemLayoutId,
			String dirPath) {
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
		this.context = context;
	}

	@Override
	public void convert(final ViewHolder helper, final String item) {
		// 设置no_pic
		helper.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
		// 设置图片
		helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);

		final ImageView mImageView = helper.getView(R.id.id_item_image);

		mImageView.setColorFilter(null);
		// 设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener() {
			// 选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v) {
				mImageView.setColorFilter(Color.parseColor("#77000000"));
				RX_Photo rx_Photo = new RX_Photo();
				rx_Photo.code = 5211624;
				rx_Photo.uri = Uri.parse(mDirPath + "/" + item);
				RxBus.getInstance().post(rx_Photo);
			}
		});

	}
}
