package com.kxy.tl.div;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.kxy.ti.util.DensityUtil;
import com.kxy.tl.R;

/**
 * 转圈圈
 *
 */
public class CircleLoadingView extends CustomView {

	private Paint mLoadingPaint, mBgPaint;
	private RectF mLoadingArc = new RectF();
	private int radius;
	private int mViewWidth, mViewHeight;
	private int mBgColor = Color.WHITE;
	
	public CircleLoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLoadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mLoadingPaint.setColor(getResources().getColor(R.color.common_blue));
		mLoadingPaint.setStyle(Style.STROKE);
		mLoadingPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 3));
		radius = DensityUtil.dip2px(getContext(), 11);
		mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mBgPaint.setColor(mBgColor);
		setClickable(true);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHeight = h;
		mLoadingArc.set(mViewWidth / 2 - radius, mViewHeight / 2 - radius, mViewWidth / 2 + radius, mViewHeight / 2 + radius);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawCircle(mViewWidth / 2, mViewHeight / 2, radius * 2, mBgPaint);
		canvas.drawArc(mLoadingArc, mStartAngle, mSweepAngle, false, mLoadingPaint);
	}
	
	public void startLoading() {
		removeCallbacks(mLoadingAnimator);
		post(mLoadingAnimator);
	}

	private int mStartAngle = 0;
	private int mSweepAngle = 0;
	boolean isIncrease = true;
	private Runnable mLoadingAnimator = new Runnable() {
		public void run() {
			if (isIncrease && mSweepAngle > 270) {
				isIncrease = false;
			} else if (!isIncrease && mSweepAngle < 0) {
				isIncrease = true;
			}
			if (isIncrease) {
				mSweepAngle += 8;
				mStartAngle += 8;
			} else {
				mSweepAngle -= 8;
				mStartAngle += 16;
			}
			if (mStartAngle > 360) mStartAngle -= 360;
			invalidate();
			postDelayed(this, 16);
		}
	};
	
	public void setBgColor(int color) {
		mBgColor = color;
		mBgPaint.setColor(color);
	}
}
