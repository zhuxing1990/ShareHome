package com.vunke.sharehome.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.rcs.contact.Contact;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.Phone;
import com.huawei.rcs.contact.Presence;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.Call.CallOut_Activity;
import com.vunke.sharehome.adapter.DialogListViewAdapter;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.utils.ActionSheetDialog;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.ActionSheetDialog.OnSheetItemClickListener;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 联系人详情
 * */
@SuppressLint("NewApi")
public class ContactDetailActivity extends BaseActivity {
	/**
	 *  号码详情，号码通话时间记录  头像
	 */
	private ImageView contactdetail_detail, contactdetail_calldate,// 
			contactdetail_icon;
	
	/**
	 *  返回键   编辑联系人，拨号
	 */
	private Button contactdetail_back, contactdetail_addcontact, call_number;
	
	private LinearLayout search01, search02;
	
	private RelativeLayout rlayout;
	private long contactid;// 联系人ID
	private Contact contact;// 华为SDK联系人
	private StringBuffer sb;
	private String DisplayName, Phone_number;// 姓名 ，号码
	private TextView contactdetail_name, contactdetail_number;// 姓名，号码
	private List<String> list;
	// private ListView listView;
	private AlertDialog alertDialog;// 弹窗

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_contactdetail);
		init();
		getExtra();
	}

	/**
	 * 获取意图
	 */
	private void getExtra() {
		Intent intent = getIntent();
		contactid = getIntent().getLongExtra("id", -1);// 联系人ID
		// WorkLog.e("ContactDetailActivity", "getcontactId>>>"+contactid);
	}

	@Override
	protected void onResume() {
		super.onResume();
		contact = ContactApi.getContact(contactid);
		loadDatas();
	}

	/**
	 * load the contact's information
	 */
	private void loadDatas() {
		Bitmap bmp = contact.getPhoto(getApplicationContext());
		if (bmp != null) {
			// contactdetail_icon .setImageBitmap(bmp);
		}
		contactdetail_name.setText(contact.getDisplayName());
		// contact.getId();
		// contact.getFirstName();
		// contact.getLastName();
		// contact.getOrganise();
		// contact.getOrganise_title();
		// contact.getEmails().toString();
		// contact.getRemarks();
		// contact.getStreet();
		sb = new StringBuffer();
		list = new ArrayList<String>();
		if (contact.getPhones().size() > 0) {
			for (Phone phone : contact.getPhones()) {
				sb.append("号码: ").append(phone.getNumber()).append("\n");
				list.add(phone.getNumber().toString().trim());
				if (phone.getPresence() != null) {
					sb.append("主页:")
							.append(phone.getPresence().getItemString(
									Presence.TYPE_HOMEPAGE))
							.append("\n")
							.append("昵称:")
							.append(phone.getPresence().getItemString(
									Presence.TYPE_NICKNAME))
							.append("\n")
							.append("备注:")
							.append(phone.getPresence().getItemString(
									Presence.TYPE_NOTE)).append("\n");
				}
			}
		}
		contactdetail_number.setText(sb.toString());
	}

	private void init() {
		contactdetail_detail = (ImageView) findViewById(R.id.contactdetail_detail);// 号码详情
		contactdetail_calldate = (ImageView) findViewById(R.id.contactdetail_calldate);// 号码通话时间记录
		contactdetail_icon = (ImageView) findViewById(R.id.contactdetail_icon);// 头像
		contactdetail_back = (Button) findViewById(R.id.contactdetail_back);// 返回键
		contactdetail_addcontact = (Button) findViewById(R.id.contactdetail_addcontact);// 编辑联系人
		call_number = (Button) findViewById(R.id.call_number);// 拨号
		search01 = (LinearLayout) findViewById(R.id.search01);
		search02 = (LinearLayout) findViewById(R.id.search02);
		rlayout = (RelativeLayout) findViewById(R.id.rlayout);
		contactdetail_name = (TextView) findViewById(R.id.contactdetail_name);// 联系人姓名
		contactdetail_number = (TextView) findViewById(R.id.contactdetail_number);// 联系人号码
		SetOnClickListener(contactdetail_detail, contactdetail_calldate,
				contactdetail_back, contactdetail_addcontact, call_number);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.contactdetail_back:// 返回键
			finish();
			break;
		case R.id.contactdetail_addcontact:// 编辑联系人
			intent = new Intent(Intent.ACTION_EDIT,
					Uri.parse("content://com.android.contacts/contacts/"
							+ contact.getId()));
			startActivity(intent);
			break;
		case R.id.contactdetail_detail:// 号码详情

			break;
		case R.id.contactdetail_calldate:// 号码通话时间记录

			break;
		case R.id.call_number:// 拨号
			// CallOut();
			CallOut();
			break;

		default:
			break;
		}

	}

	private void CallOut() {
		if (0 == list.size()) {
			showToast("没有号码");
			return;
		}
		for (String number : list) {
			if (list.size() == 1) {
				if (isCameraCanUse()) {
					if (NetUtils.isNetConnected(mcontext)) {
						/*
						 * intent = new Intent(mcontext,
						 * CallOut_Activity.class);
						 * intent.putExtra("is_video_call", true);
						 * intent.putExtra("PhoneNumber", Config.CALL_BEFORE +
						 * Config.EIGHT + number); startActivity(intent);
						 */
						Call(number);
						return;
					} else {
						showToast("咦，好像网络出了问题");
						return;
					}
				} else {
					NoPermission();
					return;
				}
			}
		}
		CallOut2();
	}

	/**
	 * 弹窗
	 * */
	public void CallOut2() {
		LayoutInflater inflater = LayoutInflater.from(mcontext);
		View view = inflater.inflate(R.layout.dialog_contactdetail, null);
		Builder builder = new AlertDialog.Builder(mcontext,
				AlertDialog.THEME_HOLO_LIGHT);//
		final AlertDialog dialog = builder.create();
		// dialog.setTitle("您确定要退出登录吗？");
		dialog.setView(view, 0, 0, 0, 0);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 1.0f;
		window.setAttributes(lp);
		dialog.setCanceledOnTouchOutside(true);// 点击外部取消dialog
		dialog.show();
		TextView dialog_title = (TextView) view.findViewById(R.id.dialog_title);
		dialog_title.setText("请选择号码！");
		ListView dialog_listview = (ListView) view
				.findViewById(R.id.dialig_listview);
		DialogListViewAdapter adapter = new DialogListViewAdapter(mcontext,
				list);
		dialog_listview.setAdapter(adapter);
		dialog_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isCameraCanUse()) {
					if (NetUtils.isNetConnected(mcontext)) {
						/*
						 * intent = new Intent(mcontext,
						 * CallOut_Activity.class);
						 * intent.putExtra("is_video_call", true);
						 * intent.putExtra("PhoneNumber", Config.CALL_BEFORE +
						 * Config.EIGHT + list.get(position).toString().trim());
						 * startActivity(intent);
						 */
						Call(list.get(position).toString().trim());
						dialog.cancel();
					} else {
						showToast("咦，好像网络出了问题");
					}
				} else {
					dialog.cancel();
					NoPermission();
				}
			}
		});
	}

	private void Call(final String number) {
		// WorkLog.e("ContactDetailActivity", "CallNumber:"+number);
		new ActionSheetDialog(mcontext)
				.builder()
				.setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.setTitle(number)
				.addSheetItem("拨打电视想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								intent = new Intent(mcontext,
										CallOut_Activity.class);
								intent.putExtra("is_video_call", true);
								intent.putExtra("PhoneNumber",
										Config.CALL_BEFORE + Config.EIGHT
												+ number);
								startActivity(intent);
							}
						})
				.addSheetItem("拨打手机想家", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								intent = new Intent(mcontext,
										CallOut_Activity.class);
								intent.putExtra("is_video_call", true);
								intent.putExtra("PhoneNumber",
										Config.CALL_BEFORE + Config.NINE
												+ number);
								startActivity(intent);
							}
						})
				.addSheetItem("拨打手机", ActionSheetDialog.SheetItemColor.Blue,
						new OnSheetItemClickListener() {

							@Override
							public void onClick(int which) {
								UiUtils.CallUserPhone(number, mcontext, 2);
							}
						}).show();
	}

	/**
	 * 提示无法打开摄像头权限
	 */
	public void NoPermission() {
		Builder builder = new AlertDialog.Builder(mcontext);
		builder.setTitle("温馨提示");
		builder.setMessage("想家没有权限打开你的摄像头 \n建议设置如下:\n1、请到“设置 - 权限管理”中打开想家权限\n2、其他应用程序正在占用摄像头,请先将摄像头关闭");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});
		builder.setCancelable(false);
		alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

}
