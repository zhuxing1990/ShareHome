package com.vunke.sharehome.fragment;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.rcs.login.LoginApi;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.AboutActivity;
import com.vunke.sharehome.activity.BuyTrafficActivity;
import com.vunke.sharehome.activity.LoginActivity2;
import com.vunke.sharehome.activity.LuckyMoneyListActivity;
import com.vunke.sharehome.activity.MoreSurprisActivity;
import com.vunke.sharehome.activity.ShareWithFriendActivity;
import com.vunke.sharehome.activity.VideoListActivity;
import com.vunke.sharehome.base.BaseFragment;
import com.vunke.sharehome.crop.Crop;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.FileUtils;
import com.vunke.sharehome.utils.ImageUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;
import com.vunke.sharehome.view.ActionSheetDialog;

/**
 * 更多界面
 * 
 * @date 2016年2月
 * 
 */
public class MoreFragment extends BaseFragment {
	private AlertDialog alertDialog;

	/**
	 * 我的信息，更多精彩，分享给朋友， 关于，退出登录，定向流量包，我的红包，视频录制
	 */
	private RelativeLayout more_myinfo, moer_surprised, share_with_friend,
			about, login_out, getDnwSmsCode, getLuckyMoney, videotape_layout;

	/**
	 * 活动窗口的线条
	 */
	private LinearLayout activity_lines;
	/**
	 * 用户帐号
	 */
	private TextView more_name;

	/**
	 * 用户头像
	 */
	public ImageView more_icon;

	/**
	 * 活动的标题
	 */
	private TextView activity_title;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_more, null);
		init(view);
		isPrepared = true;
		// initIcon();
		mTempDir = new File(Environment.getExternalStorageDirectory(), "Temp");
		if (!mTempDir.exists()) {
			mTempDir.mkdirs();
		}
		return view;
	}

	// 标志位，标志已经初始化完成。
	private boolean isPrepared;

	@Override
	protected void lazyLoad() {
		if (!isPrepared || !isVisible) {
			return;
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @param view
	 */
	private void init(View view) {
		more_myinfo = (RelativeLayout) view.findViewById(R.id.more_myinfo);
		moer_surprised = (RelativeLayout) view.findViewById(R.id.moer_surpris);
		share_with_friend = (RelativeLayout) view
				.findViewById(R.id.share_with_friend);
		about = (RelativeLayout) view.findViewById(R.id.about);
		login_out = (RelativeLayout) view.findViewById(R.id.login_out);
		getDnwSmsCode = (RelativeLayout) view.findViewById(R.id.getDnwSmsCode);
		getLuckyMoney = (RelativeLayout) view.findViewById(R.id.getLuckyMoney);
		videotape_layout = (RelativeLayout) view
				.findViewById(R.id.videotape_layout);
		activity_lines = (LinearLayout) view.findViewById(R.id.activity_lines);
		more_name = (TextView) view.findViewById(R.id.more_name);
		more_icon = (ImageView) view.findViewById(R.id.more_icon);
		activity_title = (TextView) view.findViewById(R.id.activity_title);
		initName();
		initIcon();
		initActivity();
		SetOnClickListener(more_myinfo, moer_surprised, share_with_friend,
				about, login_out, more_icon, getDnwSmsCode, getLuckyMoney,
				videotape_layout);// more_icon
	}

	/**
	 * 初始化用户的帐号
	 */
	private void initName() {
		String userName = UiUtils.GetUserName(getActivity());
		if (!TextUtils.isEmpty(userName)) {
			more_name.setText(userName.substring(1));
			
		}else {
			more_name.setText("获取用户信息失败");
		}
	}

	/**
	 * 初始化活动标题
	 */
	private void initActivity() {
		/**
		 * 如果活动页面为空，获取活动页面
		 */
		if (TextUtils.isEmpty(UrlClient.LuckyMoneyListUrl)) {
			try {

				OkHttpUtils
						.post(UrlClient.HttpUrl + UrlClient.ActivityInfo)
						.params("json",
								new JSONObject().put("activityId", "8")
										.toString())
						.execute(new StringCallback() {

							@Override
							public void onResponse(boolean isFromCache,
									String t, Request request,
									@Nullable Response response) {
								WorkLog.i("GetLuckyMoney", "获取数据" + t);
								try {
									JSONObject jsonobject = new JSONObject(t);
									if (jsonobject.has("code")) {
										int code = jsonobject.getInt("code");
										String message = jsonobject
												.getString("message");
										switch (code) {
										case 200:
											JSONObject getJsonUrl = new JSONObject(
													jsonobject
															.getString("locationUrl"));
											UrlClient.ActivityName = getJsonUrl
													.getString("activityName");
											UrlClient.LuckyMoneyUrl = getJsonUrl
													.getString("getLuckyUrl");
											UrlClient.LuckyMoneyListUrl = getJsonUrl
													.getString("listLuckyUrl");
											break;
										case 400:

											break;
										case 500:

											break;

										default:
											break;
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onError(boolean isFromCache, Call call,
									Response response, Exception e) {
								WorkLog.i("GetLuckyMoney", "OnError");
							};

							@Override
							public void onAfter(boolean isFromCache,
									@Nullable String t, Call call,
									@Nullable Response response,
									@Nullable Exception e) {
								super.onAfter(isFromCache, t, call, response, e);
								if (TextUtils.isEmpty(UrlClient.ActivityName)) {
									getLuckyMoney.setVisibility(View.GONE);
									activity_lines.setVisibility(View.GONE);
								} else {
									activity_title
											.setText(UrlClient.ActivityName);
									getLuckyMoney.setVisibility(View.VISIBLE);
									activity_lines.setVisibility(View.VISIBLE);
								}
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 初始化用户头像
	 */
	private void initIcon() {
		SharedPreferences sp = getActivity().getSharedPreferences(Config.HOME,
				getActivity().MODE_PRIVATE);
		String HasPhoto = sp.getString(Config.HasPhoto, null);
		if (!TextUtils.isEmpty(HasPhoto)) {
			// WorkLog.i("MoreFragment", "保存的路径" + HasPhoto);
			Uri uri = Uri.parse(HasPhoto);
			String path = uri.getPath();
			if (FileUtils.isFileExist(path)) {
				Bitmap scaledBitmap = ImageUtils.getScaledBitmap(uri.getPath(),
						200, 200);
				more_icon.setImageBitmap(scaledBitmap);
			} else {
				// WorkLog.i("MoreFragment", "图片不存在");
			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.login_out:// 注销
			LoginOut();
			break;
		case R.id.more_myinfo:
			// Config.intent = new
			// Intent(getActivity(),PickPhotoActivity.class);
			// startActivity(Config.intent);
			break;
		case R.id.getLuckyMoney:
			Config.intent = new Intent(getActivity(),
					LuckyMoneyListActivity.class);
			startActivity(Config.intent);
			break;
		case R.id.moer_surpris:// 更多精彩
			Config.intent = new Intent(getActivity(), MoreSurprisActivity.class);
			startActivity(Config.intent);
			break;
		case R.id.share_with_friend:// 分享给朋友
			Config.intent = new Intent(getActivity(),
					ShareWithFriendActivity.class);
			startActivity(Config.intent);
			break;
		case R.id.about:// 关于
			Config.intent = new Intent(getActivity(), AboutActivity.class);
			startActivity(Config.intent);
			break;
		case R.id.more_icon:// 更改头像
			// Config.intent = new Intent(getActivity(),
			// PickPhotoActivity.class);
			// startActivity(Config.intent);
			SetMoreIcon();
			break;
		case R.id.getDnwSmsCode:
			Config.intent = new Intent(getActivity(), BuyTrafficActivity.class);
			startActivity(Config.intent);
			break;
		case R.id.videotape_layout:
			Config.intent = new Intent(getActivity(), VideoListActivity.class);
			startActivity(Config.intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 注销
	 */
	public void LoginOut() {
		Builder dl = new AlertDialog.Builder(getActivity());
		dl.setTitle("注销登录!");
		dl.setMessage("你确定要注销登录并退出吗?");
		dl.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LoginApi.logout();
				SharedPreferences sp = getActivity().getSharedPreferences(
						Config.SP_NAME, getActivity().MODE_PRIVATE);
				sp.edit().putString(Config.LOGIN_PASSWORD, "").commit();
				sp = getActivity().getSharedPreferences(Config.UserName,
						getActivity().MODE_PRIVATE);
				sp.edit().putBoolean(Config.HasUserName, true).commit();
				DbCore.getDaoSession().getContactDao()
						.deleteByKey(sp.getLong(Config.LOOKHOME, 0));
				DbCore.getDaoSession().getContactDao().deleteAll();
				Config.intent = new Intent(getActivity(), LoginActivity2.class);
				startActivity(Config.intent);
				// LoginApi.cleanUserPassword(Config.CALL_BEFORE+UiUtils.GetUserName());
				getActivity().finish();
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});
		dl.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (alertDialog != null) {
					alertDialog.dismiss();
					alertDialog = null;
				}
			}
		});

		alertDialog = dl.create();
		alertDialog.show();
	}

	/**
	 * 更改头像
	 */
	public void SetMoreIcon() {
		new ActionSheetDialog(getActivity())
				.builder()
				.setTitle("更换头像")
				.setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("相册", ActionSheetDialog.SheetItemColor.Blue,
						new ActionSheetDialog.OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								// ImageUtils.openLocalImage(getActivity());
								Crop.pickImage(getActivity());
								// Config.intent = new Intent(getActivity(),
								// PickPhotoActivity.class);
								// startActivity(Config.intent);
							}
						})
				.addSheetItem("拍照", ActionSheetDialog.SheetItemColor.Blue,
						new ActionSheetDialog.OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								// ImageUtils.openCameraImage(getActivity());
								if (UiUtils.isCameraCanUse()) {
									getImageFromCamera();
								} else {
									NoPermission();
								}
							}
						}).show();
	}

	private File mTempDir;
	public String mCurrentPhotoPath;
	private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1458;

	/**
	 * 打开照相机拍照
	 */
	protected void getImageFromCamera() {
		// create Intent to take a picture and return control to the calling
		// application
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String fileName = "Temp_camera"
				+ String.valueOf(System.currentTimeMillis());
		File cropFile = new File(mTempDir, fileName);
		Uri fileUri = Uri.fromFile(cropFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
		// name
		mCurrentPhotoPath = fileUri.getPath();
		// start the image capture Intent
		getActivity().startActivityForResult(intent,
				REQUEST_CODE_CAPTURE_CAMEIA);
	}

	/**
	 * 提示 摄像头不可用
	 * */
	public void NoPermission() {
		Builder builder = new AlertDialog.Builder(getActivity());
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
	public void onDestroy() {
		super.onDestroy();
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}
}
