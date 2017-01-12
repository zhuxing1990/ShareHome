package com.vunke.sharehome.utils;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.vunke.sharehome.R;

import android.content.Context;

public class ShareSDK_Utils {
	
	/**
	 * @param context 上下文
	 * @param ShareUrl 分享的链接
	 * @param text 链接前面的内容
	 */
	public static void Share(Context context,String ShareUrl,String text){
		ShareSDK.initSDK(context);
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
		
		oks.setTitle(context.getString(R.string.app_name));
//		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(ShareUrl);
//		// text是分享文本，所有平台都需要这个字段
//		oks.setText("想家视频电话:电视也能和手机视频聊天"+UrlClient.ShareUrl);
		oks.setText(text + ShareUrl);// 想家视频电话:电视也能和手机视频聊天，视频电话不要钱!下载地址:
//		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImageUrl("http://url.cn/41weNg2");// 确保SDcard下面存在此张图片
		
		
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(ShareUrl);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("请输入分享的内容");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(context.getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(ShareUrl);

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		oks.setCallback(new PlatformActionListener() {

			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				WorkLog.i("ShareSDK_Utils", "分享失败");
			}

			@Override
			public void onComplete(Platform arg0, int arg1,
					HashMap<String, Object> arg2) {

				WorkLog.i("ShareSDK_Utils", "分享成功");
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				WorkLog.i("ShareSDK_Utils", "分享取消");

			}
		});

		// 启动分享GUI
		oks.show(context);
	}
}
