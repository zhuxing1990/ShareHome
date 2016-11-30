package com.vunke.sharehome;

import com.vunke.sharehome.utils.WorkLog;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private Button button1;
	private static final int VIDEO_CAPTURE = 0;
	String filePath = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);// 限制录制时间10秒
				startActivityForResult(intent, VIDEO_CAPTURE);

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO Auto-generated method stub
		if (resultCode == Activity.RESULT_OK && requestCode == VIDEO_CAPTURE) {
			Uri uri = data.getData();
			Cursor cursor = this.getContentResolver().query(uri, null, null,
					null, null);
			if (cursor != null && cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(VideoColumns._ID));
				filePath = cursor.getString(cursor
						.getColumnIndex(VideoColumns.DATA));
				// Bitmap bitmap = Thumbnails.getThumbnail(getContentResolver(),
				// id, Thumbnails.MICRO_KIND,null);
				WorkLog.e(MainActivity.class.getSimpleName(), "保存的文件路径:"
						+ filePath);
				cursor.close();
			}
		}
	}
}
