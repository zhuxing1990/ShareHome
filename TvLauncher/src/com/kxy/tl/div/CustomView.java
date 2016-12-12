package com.kxy.tl.div;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import com.kxy.ti.util.DensityUtil;

/**
 * 自定义View基类
 * @author Yu
 */
public class CustomView extends View{
	
	private ViewParent mParent;

	public CustomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomView(Context context) {
		super(context);
	}

	protected int dp(int dipValue) {
		return DensityUtil.dip2px(getContext(), dipValue);
	}
	
	protected int sp(int spValue) {
		return DensityUtil.dip2px(getContext(), spValue);
	}
	
	// scrollview 应对方案
	protected void attemptClaimDrag() {
		if (mParent == null) {
			mParent = getParent();
		}
		if (mParent != null) {
			mParent.requestDisallowInterceptTouchEvent(true);
		}
    }
	
	// 获取文字的高度
	public static int getTextHeight(Paint paint) {
		FontMetrics fontMetrics = paint.getFontMetrics(); 
		float fontHeight = fontMetrics.bottom - fontMetrics.top; 
		return (int) fontHeight;
	}
	
	// 获取文字的宽度
	public static int getTextWidth(Paint paint, String text) {
		return (int) paint.measureText(text);
	}
	
	// 获取decent
	public static float getTextDecent(Paint paint) {
		FontMetrics fm = paint.getFontMetrics();
		return fm.descent;
	}
	
	public static int getMeasurement(int measureSpec, int preferred) {
		int specSize = MeasureSpec.getSize(measureSpec);
		int measurement;
		switch (MeasureSpec.getMode(measureSpec)) {
		case MeasureSpec.EXACTLY:
			measurement = specSize;
			break;
		case MeasureSpec.AT_MOST:
			measurement = Math.min(preferred, specSize);
			break;
		default:
			measurement = preferred;
			break;
		}
		return measurement;
	}
}
