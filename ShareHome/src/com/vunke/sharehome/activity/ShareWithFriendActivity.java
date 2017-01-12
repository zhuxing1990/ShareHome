package com.vunke.sharehome.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.google.gson.Gson;
import com.hp.hpl.sparta.Text;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.base.BaseActivity;
import com.vunke.sharehome.updata.AppTVStoreUpdateInfo;
import com.vunke.sharehome.url.UrlClient;
import com.vunke.sharehome.utils.NetUtils;
import com.vunke.sharehome.utils.UiUtils;
import com.vunke.sharehome.utils.WorkLog;

/**
 * 分享联系人给朋友
 * 
 * @author Administrator
 * 
 */
public class ShareWithFriendActivity extends BaseActivity {
	private Button sharecontacts_back, sharewithfriend_but;// 返回 分享
	private String mAppUpdateJasonURL = UrlClient.ServerURL + "/update.html"; // 请求地址
	private List<AppTVStoreUpdateInfo> list;// App信息
	private String downloadURL;// 下载地址
	private static final String TAG = ShareWithFriendActivity.class
			.getSimpleName();

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_sharewithfriend);
		init();
		// initURL();
	}

	private void initURL() {
		if (!NetUtils.isNetConnected(mcontext)) {
			showToast("咦，貌似网络出了点问题");
			return;
		}
		OkHttpUtils.get(mAppUpdateJasonURL).tag(this)
				.execute(new StringCallback() {

					@Override
					public void onResponse(boolean isFromCache, String t,
							Request request, @Nullable Response response) {
						try {
							// WorkLog.i("ShareWithFriendActivity", t);
							JSONObject getjson = new JSONObject(t);
							String metaJson = getjson.getString("meta");
							JSONObject Json1 = new JSONObject(metaJson);
							// WorkLog.i("ShareWithFriendActivity", metaJson);

							String dataJson = getjson.getString("data");
							// WorkLog.i("ShareWithFriendActivity", dataJson);
							String message = Json1.getString("message");
							int Code = Json1.getInt("code");
							switch (Code) {
							case 200:
								Gson gson = new Gson();
								AppTVStoreUpdateInfo fromJson = gson.fromJson(
										dataJson, AppTVStoreUpdateInfo.class);
								// WorkLog.i("ShareWithFriendActivity",
								// fromJson.toString());
								list = new ArrayList<AppTVStoreUpdateInfo>();
								list.add(fromJson);
								// list = gson
								// .fromJson(
								// dataJson,
								// new TypeToken<List<AppTVStoreUpdateInfo>>() {
								// }.getType());
								if (list != null && list.size() != 0) {
									downloadURL = list.get(0).getUpdateUrl();
								}
								break;
							case 400:
								WorkLog.i("ShareWithFriendActivity", "请求失败"
										+ message);
								break;
							case 500:
								WorkLog.i("ShareWithFriendActivity", "请求超时"
										+ message);
								break;

							default:
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onError(boolean isFromCache, Call call,
							@Nullable Response response, @Nullable Exception e) {
						super.onError(isFromCache, call, response, e);
						showToast("请求错误,网络发送异常");
					}
				});

	}

	private void init() {
		sharecontacts_back = (Button) findViewById(R.id.sharecontacts_back);
		sharewithfriend_but = (Button) findViewById(R.id.sharewithfriend_but);
		SetOnClickListener(sharecontacts_back, sharewithfriend_but);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.sharecontacts_back:// 返回键
			finish();
			break;
		case R.id.sharewithfriend_but:// 分享
			showShare();
			break;
		default:
			break;
		}

	}

	private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		
//		oks.setTitle("分享标题--Title");
//		oks.setTitleUrl("http://mob.com");
//		oks.setText("http://mob.com");
//		oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
		
		oks.setTitle(getString(R.string.app_name));
//		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(UrlClient.ShareUrl);
//		// text是分享文本，所有平台都需要这个字段
//		oks.setText("想家视频电话:电视也能和手机视频聊天"+UrlClient.ShareUrl);
		oks.setText("想家视频电话:电视也能和手机视频聊天,下载地址:" + UrlClient.ShareUrl);// 想家视频电话:电视也能和手机视频聊天，视频电话不要钱!下载地址:
//		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImageUrl("http://url.cn/41weNg2");// 确保SDcard下面存在此张图片
		
		
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(UrlClient.ShareUrl);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("请输入分享的内容");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(UrlClient.MoreSurpris);

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		oks.setCallback(new PlatformActionListener() {

			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				WorkLog.i(TAG, "分享失败");
			}

			@Override
			public void onComplete(Platform arg0, int arg1,
					HashMap<String, Object> arg2) {

				WorkLog.i(TAG, "分享成功");
				GetLuckyMoney();
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				WorkLog.i(TAG, "分享取消");

			}
		});

		// 启动分享GUI
		oks.show(this);

	}

	protected void GetLuckyMoney() {
		SharedPreferences sp = getSharedPreferences("shareFriend", MODE_PRIVATE);
		String userName = sp.getString(UiUtils.GetUserName(mcontext), "");
		if (TextUtils.isEmpty(userName)) {
			WorkLog.i(TAG,"保存第一次分享信息");
			Config.intent = new Intent(mcontext, LuckyMoneyActivity.class);
			Config.intent.putExtra("luckyResource", "1");
			Config.intent.putExtra("extParam1", "1");
			startActivity(Config.intent);
			sp.edit().putString(UiUtils.GetUserName(mcontext), UiUtils.GetUserName(mcontext)).commit();
		}else {
			if (userName.equals( UiUtils.GetUserName(mcontext))) {
				WorkLog.i(TAG, "getuserName:"+userName +"\n userName:"+UiUtils.GetUserName(mcontext));
			}else {
				WorkLog.i(TAG, "保存分享信息");
				Config.intent = new Intent(mcontext, LuckyMoneyActivity.class);
				Config.intent.putExtra("luckyResource", "1");
				Config.intent.putExtra("extParam1", "1");
				startActivity(Config.intent);
				sp.edit().putString(UiUtils.GetUserName(mcontext), UiUtils.GetUserName(mcontext)).commit();
			}
		}
		
	}
}
