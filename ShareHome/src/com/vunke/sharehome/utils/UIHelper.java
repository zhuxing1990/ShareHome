package com.vunke.sharehome.utils;

import android.content.Context;
import android.content.DialogInterface;

import com.vunke.sharehome.R;

/**
 * 界面帮助类
 * 
 * LittleDY）
 * 
 * @version 创建时间：2014年10月10日 下午3:33:36
 * 
 */
public class UIHelper {

	/** 全局web样式 */
	// 链接样式文件，代码块高亮的处理
	public final static String linkCss = "<script type=\"text/javascript\" src=\"file:///android_asset/shCore.js\"></script>"
			+ "<script type=\"text/javascript\" src=\"file:///android_asset/brush.js\"></script>"
			+ "<script type=\"text/javascript\" src=\"file:///android_asset/client.js\"></script>"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shThemeDefault.css\">"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore.css\">"
			+ "<script type=\"text/javascript\">SyntaxHighlighter.all();</script>"
			+ "<script type=\"text/javascript\">function showImagePreview(var url){window.location.url= url;}</script>";
	public final static String WEB_STYLE = linkCss
			+ "<style>* {font-size:16px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;overflow: auto;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#cfc;color:#060;border-bottom:1px solid #B1D3EB;border-right:1px solid #B1D3EB;color:#3E6D8E;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;position:relative}</style>";

	public static final String WEB_LOAD_IMAGES = "<script type=\"text/javascript\"> var allImgUrls = getAllImgSrc(document.body.innerHTML);</script>";

	protected static final String SHOWIMAGE = "ima-api:action=showImage&data=";


	/**
	 * 发送App异常崩溃报告
	 * 
	 * @param cont
	 */
	public static void sendAppCrashReport(final Context context) {
		
	}

}
