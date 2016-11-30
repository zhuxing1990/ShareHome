package com.vunke.sharehome.activity;

import java.util.List;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.Button;

import com.huawei.rcs.contact.ContactSummary;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;

/**
 * 发送联系人到机顶盒  已弃用
 * @author Administrator
 *
 */
public class SendContactsActivity extends BaseActivity {
	private Button sendcontacts_back;// 返回键
	private List<String> letterList;// 这个就是字母的标签
	private List<ContactSummary> contacts;
	private static final int CONTACT_CODE = 1;// 设置设配器
	private LruCache<Long, Bitmap> photoCache;// 头像缓存

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_sendcontacts);
		init();
//		initContact();
	}

	private void initContact() {
		int photoCacheSize = Math.round(0.25f * Runtime.getRuntime()
				.maxMemory() / 1024);
		photoCache = new LruCache<Long, Bitmap>(photoCacheSize) {
			protected int sizeOf(Long key, Bitmap bitmap) {
				final int bitmapSize = getBitmapSize(bitmap) / 1024;
				return bitmapSize == 0 ? 1 : bitmapSize;
			}

			private int getBitmapSize(Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	private void init() {
		sendcontacts_back = (Button) findViewById(R.id.sendcontacts_back);
		SetOnClickListener(sendcontacts_back);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.sendcontacts_back:
			finish();
			break;

		default:
			break;
		}

	}

	/**
	 * 获得一个联系人的图标画像
	 */
	public Bitmap getPhoto(ContactSummary contactSummary) {
		if (photoCache == null)
			return null;
		// get the portrait icon in cache
		Bitmap bmp = photoCache.get(contactSummary.getContactId());
		if (bmp != null) {// didn't find photo in cache
			return bmp;
		} else {// get the photo by sdk interface
			bmp = contactSummary.getPhoto(mcontext);
			if (bmp != null) {
				photoCache.put(contactSummary.getContactId(), bmp);
			}
			return photoCache.get(contactSummary.getContactId());
		}
	}
}
