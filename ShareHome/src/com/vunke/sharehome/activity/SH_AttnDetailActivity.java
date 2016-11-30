package com.vunke.sharehome.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;
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
import android.widget.TextView;

import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.Call.CallOut_Activity;
import com.vunke.sharehome.adapter.DialogListViewAdapter;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.rx.RxBus;
import com.vunke.sharehome.utils.ActionSheetDialog;
import com.vunke.sharehome.utils.ActionSheetDialog.OnSheetItemClickListener;
import com.vunke.sharehome.utils.MyScrollView;
import com.vunke.sharehome.utils.MyScrollView.OnScrollListener;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 联系人详情
 * 
 */
public class SH_AttnDetailActivity extends BaseActivity implements
		OnScrollListener {
	private Button sh_attndetail_back, sh_attndetail_addcontact,
			sh_attncall_number;
	private TextView sh_attndetail_name, sh_attndetail_number;
	private ImageView sh_attndetailicon, sh_attndetail_detail,
			sh_attndetail_calldate;
	private String PhoneNumber, Name, Pid;
	private List<String> list;
	private AlertDialog alertDialog;// 弹窗

	/**
	 * 自定义的MyScrollView
	 */
	private MyScrollView myScrollView;
	private LinearLayout sh_attnsearch02;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_sh_attndatail2);
		getExtras();
		init();
	}

	private void getExtras() {
		intent = getIntent();
		PhoneNumber = intent.getStringExtra("PhoneNumber");
		// WorkLog.e("SH_AttnDetailActivity", "号码详情" + PhoneNumber);
		Name = intent.getStringExtra("Name");
		if (intent.hasExtra("Pid")) {
			Pid = intent.getStringExtra("Pid");
		}
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		sh_attndetail_back = (Button) findViewById(R.id.sh_attndetail_back);
		sh_attndetail_addcontact = (Button) findViewById(R.id.sh_attndetail_addcontact);
		sh_attncall_number = (Button) findViewById(R.id.sh_attncall_number);
		sh_attndetail_name = (TextView) findViewById(R.id.sh_attndetail_name);
		sh_attndetail_number = (TextView) findViewById(R.id.sh_attndetail_number);
		sh_attndetailicon = (ImageView) findViewById(R.id.sh_attndetailicon);
		sh_attndetail_detail = (ImageView) findViewById(R.id.sh_attndetail_detail);
		sh_attndetail_calldate = (ImageView) findViewById(R.id.sh_attndetail_calldate);

		myScrollView = (MyScrollView) findViewById(R.id.sh_attnmyScrollView);
		myScrollView.setOnScrollListener(this);
		if (!TextUtils.isEmpty(PhoneNumber)) {
			if (PhoneNumber.contains(Config.CALL_BEFORE)) {
				if (PhoneNumber.length() > 8) {
					PhoneNumber = PhoneNumber
							.substring(8, PhoneNumber.length());
				}
			}
			if (PhoneNumber.startsWith("91") || PhoneNumber.startsWith("81")) {
				PhoneNumber = PhoneNumber.substring(1);
			}
			list = new ArrayList<String>();
			list.add(PhoneNumber);
			sh_attndetail_number.setText("号码:" + PhoneNumber);
		}
		// WorkLog.e("SH_AttnDetailActivity", "号码简化" + PhoneNumber);
		if (!TextUtils.isEmpty(Name)) {

			sh_attndetail_name.setText(Name);
		}
		SetOnClickListener(sh_attndetail_back, sh_attndetail_addcontact,
				sh_attndetailicon, sh_attndetail_detail,
				sh_attndetail_calldate, sh_attncall_number);

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.sh_attndetail_back:
			finish();
			break;
		case R.id.sh_attndetail_addcontact:// 编辑联系人
			if (TextUtils.isEmpty(Pid)) {// 判断 Pid是否存在
				if (PhoneNumber.length() > 11) {// 判断号码长度
					if (PhoneNumber.startsWith("8")
							|| PhoneNumber.startsWith("9")) {// 截取前面字符串
						PhoneNumber = PhoneNumber.substring(0);
					}
				}
				if (UiUtils.isMobileNO(PhoneNumber)) {// 判断手机号码是否正确
					addContacts();
				} else {
					showToast("手机号码格式错误,请确认该号码为手机号码");
				}

			} else {
				// setContacts();
				Config.intent = new Intent(mcontext,
						ModifyContactActivity.class);
				Config.intent.putExtra("name", Name);
				Config.intent.putExtra("number", PhoneNumber);
				Config.intent.putExtra("pid", Pid);
				startActivity(Config.intent);
				finish();
			}
			break;
		case R.id.sh_attndetailicon:

			break;
		case R.id.sh_attncall_number:
			// CallOut();
			// CallOut2(Config.EIGHT);
			CallNumber();
			break;

		default:
			break;
		}

	}

	private void CallNumber() {
		WorkLog.e("SH_AttnDetailActivity", "准备拨号" + PhoneNumber);
		if (isCameraCanUse()) {
			if (NetUtils.isNetConnected(mcontext)) {
				if (UiUtils.isMobileNO(PhoneNumber)) {
					Call(PhoneNumber);
				} else {
					if (PhoneNumber.length() > 11) {// 判断号码长度
						if (PhoneNumber.startsWith("8")
								|| PhoneNumber.startsWith("9")) {
							Call(PhoneNumber.substring(1));
						}
					}
				}
			} else {
				showToast("咦，好像网络出了问题");
			}
		} else {
			NoPermission();
		}
	}

	private void Call(final String number) {
		// WorkLog.e("SH_AttnDetailActivity", number);
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

	private void CallOut2(String type) {
		// PhoneNumber
		if (isCameraCanUse()) {
			if (NetUtils.isNetConnected(mcontext)) {
				if (UiUtils.isMobileNO(PhoneNumber) == true) {
					intent = new Intent(mcontext, CallOut_Activity.class);
					intent.putExtra("is_video_call", true);
					intent.putExtra("PhoneNumber", Config.CALL_BEFORE
							+ Config.EIGHT + PhoneNumber);
					startActivity(intent);
				} else {
					intent = new Intent(mcontext, CallOut_Activity.class);
					intent.putExtra("is_video_call", true);
					intent.putExtra("PhoneNumber", Config.CALL_BEFORE
							+ PhoneNumber);
					startActivity(intent);
				}
			} else {
				showToast("咦，好像网络出了问题");
			}
		} else {
			NoPermission();
		}
	}

	/**
	 * 添加联系人到想家
	 * */
	private void addContacts() {
		Builder builder = new AlertDialog.Builder(mcontext);
		builder.setTitle("添加联系人到想家");
		builder.setPositiveButton("添加联系人到想家", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Config.intent = new Intent(mcontext, AddAttnActivity.class);
				Config.intent.putExtra("number", PhoneNumber);
				startActivity(Config.intent);
				finish();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

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
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	/**
	 * 设置联系人
	 * */
	private void setContacts() {
		Builder builder = new AlertDialog.Builder(mcontext);
		builder.setTitle("编辑联系人");
		builder.setPositiveButton("修改联系人", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Config.intent = new Intent(mcontext,
						ModifyContactActivity.class);
				Config.intent.putExtra("name", Name);
				Config.intent.putExtra("number", PhoneNumber);
				Config.intent.putExtra("pid", Pid);
				startActivity(Config.intent);
				finish();
			}
		});
		builder.setNegativeButton("删除联系人", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// sql = new ContactsSqlite(mcontext);
				// sql.deleteContact(PhoneNumber);
				DbCore.getDaoSession().getContactDao()
						.deleteByKey(Long.parseLong(Pid));
				RxBus.getInstance().post(Config.Update_Contact);
				finish();
			}
		});
		builder.setNeutralButton("取消", new OnClickListener() {

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
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	/**
	 * 弹窗
	 * */
	@SuppressLint("NewApi")
	public void CallOut() {
		LayoutInflater inflater = LayoutInflater.from(mcontext);
		View view = inflater.inflate(R.layout.dialog_contactdetail, null);
		Builder builder = new AlertDialog.Builder(mcontext,
				AlertDialog.THEME_HOLO_LIGHT);//
		final AlertDialog dialog = builder.create();
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
					if (UiUtils.isMobileNO(list.get(position).toString().trim()) == true) {
						intent = new Intent(mcontext, CallOut_Activity.class);
						intent.putExtra("is_video_call", true);
						intent.putExtra("PhoneNumber", Config.CALL_BEFORE
								+ Config.EIGHT
								+ list.get(position).toString().trim());
						startActivity(intent);
					} else {
						intent = new Intent(mcontext, CallOut_Activity.class);
						intent.putExtra("is_video_call", true);
						intent.putExtra("PhoneNumber", Config.CALL_BEFORE
								+ list.get(position).toString().trim());
						startActivity(intent);
					}

					dialog.cancel();
				} else {
					dialog.cancel();
					NoPermission();
				}
			}
		});
	}

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
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	@Override
	public void onScroll(int scrollY) {

	}
}
